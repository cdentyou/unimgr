package org.opendaylight.unimgr.mef.notification;

import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.unimgr.mef.notification.topic.TopicHandler;
import org.opendaylight.unimgr.mef.notification.utils.Util;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.CreateTopicOutput;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.CreateTopicOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.NotificationPattern;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventsource.rev141202.JoinTopicInput;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventsource.rev141202.JoinTopicInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * Created by root on 11.01.17.
 */
public class EventSourceTestUtils {

    public static Node prepareTestNode(){

        TerminationPointBuilder terminationPointBuilder = new TerminationPointBuilder();
        terminationPointBuilder.setTpId(new TpId("TestTP"));
        terminationPointBuilder.setKey(new TerminationPointKey(new TpId("TestTP2")));
        TerminationPoint terminationPoint = terminationPointBuilder.build();
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setNodeId(new NodeId("TestNode"));
        List<TerminationPoint> tps = new LinkedList<>();
        tps.add(terminationPoint);
        nodeBuilder.setTerminationPoint(tps);

        return nodeBuilder.build();
    }

    public static InstanceIdentifier prepareTestNodeInstanceIdentifier(NodeId nodeId){
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> nodeIid = InstanceIdentifier
                .builder(NetworkTopology.class)
                .child(Topology.class,
                        new TopologyKey(new TopologyId(new Uri("ovsdb:1"))))
                .child(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class,
                        new NodeKey(nodeId))
                .build();
        return nodeIid;
    }

    public static LeafNode<String> prepareTestLeafNode(){
        QName ipAddressQname = QName.create(Node.QNAME, "ip-address");
        String nodeName = "node:1";
        String ipAddress = "192.168.1.1";
        LeafNode<String> nodeIpValue = ImmutableNodes.leafNode(ipAddressQname, ipAddress);
        return nodeIpValue;
    }

    public static JoinTopicInput createJoinTopicInput(TopicId topicId, Notifications notifications){
        JoinTopicInputBuilder joinTopicInputBuilder = new JoinTopicInputBuilder();
        joinTopicInputBuilder.setTopicId(topicId);

        String path = TopicHandler.createNotificationPatternName(notifications);
        NotificationPattern notificationPattern = new NotificationPattern(path);
        joinTopicInputBuilder.setNotificationPattern(notificationPattern);
        return joinTopicInputBuilder.build();
    }

    public static Future<RpcResult<CreateTopicOutput>> createTopicMock(){
        CreateTopicOutputBuilder createTopicOutputBuilder = new CreateTopicOutputBuilder();
        Random generator = new Random();
        String id = Integer.toString(generator.nextInt(Integer.SIZE - 1));
        TopicId topicId = new TopicId(id);
        createTopicOutputBuilder.setTopicId(topicId);
        final CreateTopicOutput cto = createTopicOutputBuilder.build();
        Future<RpcResult<CreateTopicOutput>> result = Util.resultRpcSuccessFor(cto);
        return result;
    }


}
