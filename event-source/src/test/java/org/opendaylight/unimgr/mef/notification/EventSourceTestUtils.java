package org.opendaylight.unimgr.mef.notification;

import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.unimgr.mef.notification.utils.TopicHandler;
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
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Class that delivers methods utilized in Tests in event-source module.
 */
public class EventSourceTestUtils {

    public static Node prepareTestNode(String nodeId,boolean single){
        List<TerminationPoint> tps = new LinkedList<>();
        if(single){
            TerminationPoint terminationPoint = buildTerminationPoint("TestTpId","TestTpKey");
            tps.add(terminationPoint);
        } else {
            TerminationPoint terminationPoint2 = EventSourceTestUtils.buildTerminationPoint("TestTpId2","TestTpKey2");
            TerminationPoint terminationPoint3 = EventSourceTestUtils.buildTerminationPoint("TestTpId3","TestTpKey3");
            tps.add(terminationPoint2);
            tps.add(terminationPoint3);
        }
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setNodeId(new NodeId(nodeId));
        nodeBuilder.setTerminationPoint(tps);
        return nodeBuilder.build();
    }

    private static TerminationPoint buildTerminationPoint(String tpId, String tpKey){
        TerminationPointBuilder terminationPointBuilder = new TerminationPointBuilder();
        terminationPointBuilder.setTpId(new TpId(tpId));
        terminationPointBuilder.setKey(new TerminationPointKey(new TpId(tpKey)));
        TerminationPoint terminationPoint = terminationPointBuilder.build();
        return terminationPoint;
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

    public static void checkMessages(List<String> receivedMessages,String esw1, String firstMessage, String esw2, String secondMessage){
        esw1 = addXmlWrapper(esw1,false);
        esw2 = addXmlWrapper(esw2,false);
        firstMessage = addXmlWrapper(firstMessage,true);
        secondMessage = addXmlWrapper(secondMessage,true);
        for(String message:receivedMessages){
            if(message.contains(esw1)){
                assertTrue(message.contains(firstMessage));
            } else if (message.contains(esw2)){
                assertTrue(message.contains(secondMessage));
            } else {
                fail();
            }
        }
    }

    private static String addXmlWrapper(String s, boolean message){
        if(message){
            return "<Message>"+s+"</Message>";
        } else {
            return "<Source>"+s+"</Source>";
        }
    }

    public static void checkPassedBaObjects(List<DataContainer> baObjects, Node node, Node node2){
        assertEquals(2,baObjects.size());
        for(DataContainer dataContainer:baObjects){
            Node nodeX = (Node) dataContainer;
            if(nodeX.getTerminationPoint().size()>1){
                compareNodes(node2,nodeX);
            } else {
                compareNodes(node,nodeX);
            }
        }
    }

    private static void compareNodes(Node actual, Node expected){
        assertEquals(actual.getKey().getNodeId(),expected.getKey().getNodeId());
        assertEquals(actual.getTerminationPoint().size(),expected.getTerminationPoint().size());
    }

    public static void checkPassedBiObject(List<DataContainerChild> receivedBiObjects, LeafNode<String> leafNode){
        long leafs = receivedBiObjects.stream()
                .filter( x -> leafNode.equals((LeafNode<String>) x))
                .count();
        assertTrue(leafs>0);
    }
}
