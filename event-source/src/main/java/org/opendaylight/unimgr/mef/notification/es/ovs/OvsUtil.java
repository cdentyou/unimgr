package org.opendaylight.unimgr.mef.notification.es.ovs;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.unimgr.mef.nrp.common.ResourceNotAvailableException;
import org.opendaylight.unimgr.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
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
import java.util.Optional;

/**
 * Class created to assist with the operations on Open vSwitch Operational DataStore.
 *
 * @author marek.ryznar@amartus.com
 */
public class OvsUtil {
    private static final Logger LOG = LoggerFactory.getLogger(OvsUtil.class);
    private static final String FLOW_TABLE_NOT_PRESENT_ERROR_MESSAGE = "Flow table is not present in node '%s'.";
    private static final String NODE_NOT_AUGMENTED_ERROR_MESSAGE = "Node '%s' does not have '%s' augmentation.";
    private static final Short FLOW_TABLE_ID = 0;
    private static final String NEWLINE_AND_INDENT = "\n    ";

    /**
     * Returns flow table for the specified node
     *
     * @param node openflow node
     * @return flow table
     * @throws ResourceNotAvailableException if node is not augmented with FlowCapableNode class or flow table is not present in node
     */
    public static Table getTable(Node node) throws ResourceNotAvailableException {
        String nodeId = node.getId().getValue();
        FlowCapableNode flowCapableNode = node.getAugmentation(FlowCapableNode.class);
        if (flowCapableNode == null) {
            LOG.warn(String.format(NODE_NOT_AUGMENTED_ERROR_MESSAGE, nodeId, FlowCapableNode.class.toString()));
            throw new ResourceNotAvailableException(String.format(NODE_NOT_AUGMENTED_ERROR_MESSAGE, nodeId, FlowCapableNode.class.toString()));
        }

        Optional<Table> flowTable = flowCapableNode.getTable()
                .stream()
                .filter(table -> table.getId().equals(FLOW_TABLE_ID))
                .findFirst();
        if (!flowTable.isPresent()) {
            LOG.warn(String.format(FLOW_TABLE_NOT_PRESENT_ERROR_MESSAGE, nodeId));
            throw new ResourceNotAvailableException(String.format(FLOW_TABLE_NOT_PRESENT_ERROR_MESSAGE, nodeId));
        }

        return flowTable.get();
    }

    /**
     * Method convert given openflow table to user friendly text used in notification message.
     *
     * @param table OpenFlow table
     * @return table in text form
     */
    public static String tableToString(Table table){
        StringBuilder message = new StringBuilder();
        LOG.info("tableToString method started");
        if(table.getFlow()!=null){
            flowsToString(table.getFlow(),message);
        } else {
            message.append("No flows here.");
        }
        LOG.info("tableToString method finished");

        return message.toString();
    }

    private static void flowsToString(List<Flow> flows, StringBuilder message){
        LOG.info("flowsToString method started");
        for(Flow flow : flows){
            message.append("\n  ");
            message.append("FlowId: ");
            LOG.info("FlowID: {}",flow.getId().getValue());
            message.append(flow.getId().getValue());
            message.append(NEWLINE_AND_INDENT);
            message.append("Priority: ");
            LOG.info("Priority: {}",flow.getPriority());
            message.append(flow.getPriority());
            getFlowStatistic(flow,message);
            getMatch(flow.getMatch(),message);
            getActions(flow.getInstructions(),message);
        }
    }

    private static void getMatch(Match match, StringBuilder message){
        LOG.info("getMatch - start");
        if(match!=null){
            message.append(NEWLINE_AND_INDENT);
            message.append("Match patterns:");
            MatchUtil.getMatch(match,message);
        }
        LOG.info("getMatch - end");
    }

    private static void getActions(Instructions instructions, StringBuilder message){
        LOG.info("getAction - start");
        if(instructions!=null){
            message.append(NEWLINE_AND_INDENT);
            message.append("Actions:");
            ActionUtil.getActions(instructions,message);
        }
        LOG.info("getAction - end");
    }

    private static void getFlowStatistic(Flow flow,StringBuilder message){
        LOG.info("Flow statistic - start");
        FlowStatistics flowStatistics = flow.getAugmentation(FlowStatisticsData.class).getFlowStatistics();
        message.append(NEWLINE_AND_INDENT);
        message.append("Byte count: ");
        message.append(flowStatistics.getByteCount().getValue());
        message.append(NEWLINE_AND_INDENT);
        message.append("Packet count: ");
        message.append(flowStatistics.getPacketCount().getValue());
        LOG.info("Flow statistic - end");
    }

    /**
     * Reads OVSDB node from operational Data Store.
     *
     * @param nodeId
     * @param dataBroker
     * @return
     */
    public static com.google.common.base.Optional<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> getOVSDBNode(NodeId nodeId, DataBroker dataBroker) {
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> nodeIid = InstanceIdentifier
                .builder(NetworkTopology.class)
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