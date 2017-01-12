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
import org.opendaylight.unimgr.mef.nrp.cisco.xr.l2vpn.helper.TapiHelper;
import org.opendaylight.unimgr.utils.CapabilitiesService;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730.InterfaceConfigurations;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730._interface.configurations.InterfaceConfiguration;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730._interface.configurations.InterfaceConfigurationKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.network.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev150608.Node1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev150608.Node1Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev150608.TpId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev150608.network.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev150608.network.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev150608.network.node.TerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
    private static final String XR_NODE = "xr-node";
    private static final String PRESTO_TOPO = "mef:presto-topology";

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

        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.network.Node node = TapiHelper.node(XR_NODE);

        tx.put(LogicalDatastoreType.OPERATIONAL, TapiHelper.tapiNodeId(XR_NODE), node);
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void result) {
                log.info("Node {} created", XR_NODE);
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



    Function<DataObjectModification<Node>, Node> addedNode = mod -> mod.getModificationType() == DataObjectModification.ModificationType.WRITE ?
            mod.getDataAfter() : null;

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {

        List<Node> addedNodes = changes.stream().map(DataTreeModification::getRootNode)
                .map(addedNode::apply)
                .filter(n -> {
                    if (n == null) return false;
                    return capabilitiesService.node(n).isSupporting(AND, NETCONF, NETCONF_CISCO_IOX_IFMGR, NETCONF_CISCO_IOX_L2VPN);
        }).collect(Collectors.toList());
        try {
            onAddedNodes(addedNodes);
        } catch(Exception e) {
            //TODO improve error handling
            log.error("error while processing new Cisco nodes", e);
        }
    }

    private void onAddedNodes(@Nonnull Collection<Node> added) throws ReadFailedException {
        if(added.isEmpty()) return;
        log.debug("found {} added XR nodes", added.size());

        final ReadWriteTransaction topoTx = dataBroker.newReadWriteTransaction();

        NodeId nodeId = new NodeId(XR_NODE);

        Optional<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.network.Node> xrNode = topoTx.read(LogicalDatastoreType.OPERATIONAL, TapiHelper.tapiNodeId(XR_NODE)).checkedGet();
        if(! xrNode.isPresent()) {
            log.warn("XR node not present in TAPI topology");
        }
        Node1 nodeAugmentation = xrNode.get().getAugmentation(Node1.class);
        Node1Builder tpList = nodeAugmentation == null ? new Node1Builder() : new Node1Builder(nodeAugmentation);

        TreeSet<TerminationPoint> terminationPoints = new TreeSet<>(Comparator.comparing((TerminationPoint o) -> o.getTpId().getValue()));

        terminationPoints.addAll(toTp(added));
        if(tpList.getTerminationPoint() != null)
            terminationPoints.addAll(tpList.getTerminationPoint());

        tpList.setTerminationPoint(new LinkedList<>(terminationPoints));
        NodeBuilder xrNodeBuilder = new NodeBuilder(xrNode.get()).addAugmentation(Node1.class, tpList.build());
        topoTx.put(LogicalDatastoreType.OPERATIONAL, TapiHelper.tapiNodeId(XR_NODE),  xrNodeBuilder.build());

        Futures.addCallback(topoTx.submit(), new FutureCallback<Void>() {

            @Override
            public void onSuccess(@Nullable Void result) {
                log.info("TAPI node upadate successful");
            }

            @Override
            public void onFailure(Throwable t) {
                log.warn("TAPI node upadate failed due to an error", t);
            }
        });
    }

    //simplyfied version of selecting
    private Pattern gbPort = Pattern.compile(".*GigabitEthernet[^.]+$");

    final Predicate<InterfaceConfiguration> isNep = ic -> {
        final String name = ic.getKey().getInterfaceName().getValue();
        return gbPort.matcher(name).matches();
    };

    private List<TerminationPoint> toTp(Collection<Node> nodes) {
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        return nodes.stream().flatMap(cn -> {
            final NodeKey key = cn.getKey();
            try {
                KeyedInstanceIdentifier<Node, NodeKey> id = mountIds.get(key);
                Optional<MountPoint> mountPoint = mountService.getMountPoint(id);
                if(mountPoint.isPresent()) {
                    DataBroker deviceBroker = mountPoint.get().getService(DataBroker.class).get();
                    log.debug(deviceBroker.toString());
                    List<TerminationPoint> tps;
                    try(ReadOnlyTransaction tx = deviceBroker.newReadOnlyTransaction()) {
                        tps = ports(tx)
                                .filter(i -> {
                                    boolean shutdown = i != null && i.isShutdown() != null && i.isShutdown();
                                    return !shutdown;
                                })
                                .filter(isNep::test)
                                .map(i -> {
                                    InterfaceConfigurationKey ikey = i.getKey();
                                    log.debug("found {} interface", ikey);
                                    TpId tpId = new TpId(cn.getNodeId().getValue() + ":" + ikey.getInterfaceName().getValue());
                                    return tpBuilder
                                            .setTpId(tpId)
                                            .setKey(new TerminationPointKey(tpId))
                                            .build();
                                }).collect(Collectors.toList());
                    }

                    return tps.stream();

                } else {
                    log.warn("no mount point for {}", key);
                }

            } catch (Exception e) {
                log.warn("error while processing " + key, e);
            }
            return Stream.empty();
        }).collect(Collectors.toList());
    }

    private Stream<InterfaceConfiguration> ports(ReadOnlyTransaction tx) throws ReadFailedException {
        Optional<InterfaceConfigurations> interfaces = tx.read(LogicalDatastoreType.OPERATIONAL, InterfaceHelper.getInterfaceConfigurationsId()).checkedGet();
        if(interfaces.isPresent()) {
            return interfaces.get().getInterfaceConfiguration().stream();
        }

        return Stream.empty();
    }

}
