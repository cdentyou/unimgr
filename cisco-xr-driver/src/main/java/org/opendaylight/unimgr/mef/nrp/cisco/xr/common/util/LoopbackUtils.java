/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.unimgr.mef.nrp.cisco.xr.common.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.onf.core.network.module.rev160630.g_forwardingconstruct.FcPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.unimgr.rev151012.LoopbackAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools designated to support operations on loopback interfaces data
 *
 * @author krzysztof.bijakowski@amartus.com
 */
public class LoopbackUtils {
    private static final Logger LOG = LoggerFactory.getLogger(LoopbackUtils.class);
    private static final String DEFAULT_LOOPBACK = "127.0.0.1";

    public static Ipv4AddressNoZone getIpv4Address(FcPort port, Optional<DataBroker> dataBrokerOptional) {
        NodeId nodeId = port.getNode();
        TopologyId topologyId = port.getTopology();
        InstanceIdentifier<Node> nodeInstanceId = getNodeIid(nodeId,topologyId);

        Node node = read(LogicalDatastoreType.CONFIGURATION,nodeInstanceId,dataBrokerOptional);

        LoopbackAugmentation la = node.getAugmentation(LoopbackAugmentation.class);
        String loopback = null;

        if(la!=null){
            loopback = la.getLoopbackAddress().getIpv4Address().getValue();
        }

        if (loopback == null) {
            LOG.warn("No loopback address found for {}", nodeId.getValue());
            loopback = DEFAULT_LOOPBACK;
        }

        return new Ipv4AddressNoZone(loopback);
    }

    public static <D extends org.opendaylight.yangtools.yang.binding.DataObject> D read(
            final LogicalDatastoreType store, final InstanceIdentifier<D> path, Optional<DataBroker> optBroker)  {
        D result = null;
        final ReadOnlyTransaction transaction = optBroker.get().newReadOnlyTransaction();
        Optional<D> optionalDataObject;
        CheckedFuture<Optional<D>, ReadFailedException> future = transaction.read(store, path);
        try {
            optionalDataObject = future.checkedGet();
            if (optionalDataObject.isPresent()) {
                result = optionalDataObject.get();
            } else {
                LOG.error("{}: Failed to read {}",
                        Thread.currentThread().getStackTrace()[1], path);
            }
        } catch (ReadFailedException e) {
            LOG.error("Failed to read {} ", path, e);
        }
        transaction.close();
        return result;
    }

    public static String getDefaultLoopback() {
        return DEFAULT_LOOPBACK;
    }

    public static InstanceIdentifier<Node> getNodeIid(NodeId nodeId, TopologyId topologyId){
        InstanceIdentifier<Node> nodeInstanceId = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(topologyId))
                .child(Node.class, new NodeKey(nodeId))
                .build();
        return nodeInstanceId;
    }
}
