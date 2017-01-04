package org.opendaylight.unimgr.mef.notification.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.unimgr.mef.nrp.common.ResourceNotAvailableException;
import org.opendaylight.unimgr.utils.MdsalUtils;
import org.opendaylight.unimgr.utils.NullAwareDatastoreGetter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class copied from ovs-driver module, because using dependency cause cyclic dependency error
 * TODO: Reconsider where to move TopologyTransaction class (probably impl module will be the best solution)
 */
public class TopologyTransaction {
    private static final String NODE_NOT_FOUND_ERROR_MESSAGE = "Node with port '%s' not found in OPERATIONAL data store.";
    private static final Logger LOG = LoggerFactory.getLogger(TopologyTransaction.class);
    private DataBroker dataBroker;

    public TopologyTransaction(DataBroker dataBroker){
        this.dataBroker = dataBroker;
    }

    /**
     * Returns openflow node containing port portName
     *
     * @param portName node's port name
     * @return node
     * @throws ResourceNotAvailableException if node for the specified port name was not found
     */
    public Node readNode(String portName) throws ResourceNotAvailableException {
        for(NullAwareDatastoreGetter<Node> node : readNodes()) {
            if(node.get().isPresent()){
                for(NodeConnector nodeConnector:node.get().get().getNodeConnector()) {
                    FlowCapableNodeConnector flowCapableNodeConnector
                            = nodeConnector.getAugmentation(FlowCapableNodeConnector.class);
                    if (portName.equals(flowCapableNodeConnector.getName())) {
                        return node.get().get();
                    }
                }
            }
        }

        LOG.warn(String.format(NODE_NOT_FOUND_ERROR_MESSAGE, portName));
        throw new ResourceNotAvailableException(String.format(NODE_NOT_FOUND_ERROR_MESSAGE, portName));
    }


    /**
     * Returns list of nodes in openflow topology
     *
     * @return list of nodes
     */
    public List<NullAwareDatastoreGetter<Node>> readNodes() {
        return new NullAwareDatastoreGetter<Nodes>(MdsalUtils.readOptional(dataBroker, LogicalDatastoreType.OPERATIONAL, getNodesInstanceId())).collectMany(x -> x::getNode);
    }

    private InstanceIdentifier<Nodes> getNodesInstanceId() {
        return InstanceIdentifier.builder(Nodes.class).build();
    }

    public com.google.common.base.Optional<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> getOVSDBNode(NodeId nodeId) {
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> nodeIid = InstanceIdentifier
                .builder(NetworkTopology.class)
                //.create(NetworkTopology.class)
                .child(Topology.class,
                        new TopologyKey(new TopologyId(new Uri("ovsdb:1"))))
                .child(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class,
                        new NodeKey(nodeId))
                .build();

        com.google.common.base.Optional<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> node = MdsalUtils.readNode(dataBroker,
                LogicalDatastoreType.OPERATIONAL,
                nodeIid);
        return node;
    }
}
