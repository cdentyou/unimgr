/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.mef.nrp.cisco.xr;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.unimgr.mef.nrp.cisco.xr.common.helper.InterfaceHelper;
import org.opendaylight.unimgr.utils.CapabilitiesService;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730.InterfaceConfigurations;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730._interface.configurations.InterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.opendaylight.unimgr.utils.CapabilitiesService.Capability.Mode.AND;
import static org.opendaylight.unimgr.utils.CapabilitiesService.NodeContext.NodeCapability.NETCONF;
import static org.opendaylight.unimgr.utils.CapabilitiesService.NodeContext.NodeCapability.NETCONF_CISCO_IOX_IFMGR;
import static org.opendaylight.unimgr.utils.CapabilitiesService.NodeContext.NodeCapability.NETCONF_CISCO_IOX_L2VPN;

/**
 * @author bartosz.michalik@amartus.com
 */
public class TopologyHandler implements DataTreeChangeListener<Node> {
    private static final Logger log = LoggerFactory.getLogger(TopologyHandler.class);
    public static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier
                    .create(NetworkTopology.class)
                    .child(Topology.class,
                            new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    LoadingCache<NodeKey, KeyedInstanceIdentifier<Node, NodeKey>> mountIds = CacheBuilder.newBuilder()
            .maximumSize(20)
            .build(
                    new CacheLoader<NodeKey, KeyedInstanceIdentifier<Node, NodeKey>>() {
                        public KeyedInstanceIdentifier<Node, NodeKey> load(final NodeKey key) {
                            return NETCONF_TOPO_IID.child(Node.class, key);
                        }
                    });


    private final DataBroker dataBroker;
    private final MountPointService mountService;

    private static final String PRESTO_TOPO = "mef:presto-topology";
    private ListenerRegistration<TopologyHandler> registration;
    private CapabilitiesService capabilitiesService;


    public TopologyHandler(DataBroker dataBroker, MountPointService mountService) {
        Objects.requireNonNull(dataBroker);
        Objects.requireNonNull(mountService);
        this.dataBroker = dataBroker;
        this.mountService = mountService;
    }

    public void init() {
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        NodeId nodeId = new NodeId("xr-node");
        Node node = new NodeBuilder()
                .setNodeId(new NodeId("xr-node"))
                .build();
        tx.put(LogicalDatastoreType.OPERATIONAL, node(nodeId), node);
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void result) {
                log.info("Node {} created",  nodeId);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("No node created due to the error", t);
            }
        });

        capabilitiesService = new CapabilitiesService(dataBroker);

        registerNetconfTreeListener();


    }

    public void close() {
        if(registration != null) {
            log.info("closing netconf tree listener");
            registration.close();
        }

    }

    private void registerNetconfTreeListener() {

        InstanceIdentifier<Node> nodeId = NETCONF_TOPO_IID.child(Node.class);

        registration = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, nodeId), this);
        log.info("netconf tree listener registered");
    }

    private static InstanceIdentifier<Node> node(NodeId nodeId) {
        return InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class,
                        new TopologyKey(new TopologyId(PRESTO_TOPO)))
                .child(Node.class, new NodeKey(nodeId));
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {

        List<DataObjectModification<Node>> ciscoNodes = changes.stream().map(DataTreeModification::getRootNode).filter(n -> {
            Node node;
            switch (n.getModificationType()) {
                case WRITE:
                    node = n.getDataAfter();
                    break;
                case DELETE:
                default:
                    node = n.getDataBefore();
            }
            if (node == null) return false;
            return capabilitiesService.node(node).isSupporting(AND, NETCONF, NETCONF_CISCO_IOX_IFMGR, NETCONF_CISCO_IOX_L2VPN);
        }).collect(Collectors.toList());

        if(ciscoNodes.isEmpty()) return;
        log.debug("found {} XR nodes", ciscoNodes.size());


        ciscoNodes.forEach(cn -> {
            final NodeKey key = getKey(cn.getIdentifier());
            try {
                KeyedInstanceIdentifier<Node, NodeKey> id = mountIds.get(key);
                Optional<MountPoint> mountPoint = mountService.getMountPoint(id);
                if(mountPoint.isPresent()) {
                    DataBroker deviceBroker = mountPoint.get().getService(DataBroker.class).get();
                    log.debug(deviceBroker.toString());

                    try(ReadOnlyTransaction tx = deviceBroker.newReadOnlyTransaction()) {
                        ports(tx)
                                .filter(i -> !i.isShutdown())
                                .forEach(i -> log.debug("found {} interface", i.getKey()));
                    }



                } else {
                    log.warn("no mount point for {}", key);
                }

            } catch (Exception e) {
                log.warn("error while processing " + key, e);
            }
        });


//        mountIds.get()

    }

    private Stream<InterfaceConfiguration> ports(ReadOnlyTransaction tx) throws ReadFailedException {
        Optional<InterfaceConfigurations> interfaces = tx.read(LogicalDatastoreType.OPERATIONAL, InterfaceHelper.getInterfaceConfigurationsId()).checkedGet();
        if(interfaces.isPresent()) {
//            System.out.println(interfaces.get());
            return interfaces.get().getInterfaceConfiguration().stream();
        }

        return Stream.empty();
    }

    private NodeKey getKey(InstanceIdentifier.PathArgument arg) {
        if(arg instanceof InstanceIdentifier.IdentifiableItem) {
            return ((NodeKey)((InstanceIdentifier.IdentifiableItem) arg).getKey());
        }
        return null;
    }
}
