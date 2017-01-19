package org.opendaylight.unimgr.mef.notification;

import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.unimgr.mef.notification.api.EventSourceApiImpl;
import org.opendaylight.unimgr.mef.notification.es.example.ExampleEventSource;
import org.opendaylight.unimgr.mef.notification.impl.Util;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

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

    public static InstanceIdentifier prepareNodeInstanceIdentifier(NodeId nodeId){
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> nodeIid = InstanceIdentifier
                .builder(NetworkTopology.class)
                .child(Topology.class,
                        new TopologyKey(new TopologyId(new Uri("ovsdb:1"))))
                .child(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class,
                        new NodeKey(nodeId))

                //ew nastepne childy
                .build();
        return nodeIid;
    }



    /**
     * Method made to call {@link org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventsource.rev141202.EventSourceService#joinTopic(JoinTopicInput)} manually.
     * In normal situation it is called automatically when topic is created, but in test @see org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService is not mocked enough.
     *
     * @param eventSource
     */
    public static void joinTopicsToEventSource(EventSource eventSource, EventSourceApiImpl eventSourceApi){
        eventSourceApi.getTopicsPerNotifications().entrySet()
                .stream()
                .filter(topic -> eventSource.getAvailableNotifications().contains(topic.getValue()))
                .forEach(topic -> eventSource.joinTopic(createJoinTopicInput(topic,eventSourceApi)));
    }

    public static JoinTopicInput createJoinTopicInput(Map.Entry<TopicId, SchemaPath> topic, EventSourceApiImpl eventSourceApi){
        JoinTopicInputBuilder joinTopicInputBuilder = new JoinTopicInputBuilder();
        joinTopicInputBuilder.setTopicId(topic.getKey());

        NotificationPattern notificationPattern = new NotificationPattern(eventSourceApi.getSchemaPathString(topic.getValue()));
        joinTopicInputBuilder.setNotificationPattern(notificationPattern);

        return joinTopicInputBuilder.build();
    }

    public static Future<RpcResult<CreateTopicOutput>> createTopicMock(EventSourceApiImpl eventSourceApi){
        CreateTopicOutputBuilder createTopicOutputBuilder = new CreateTopicOutputBuilder();
        Random generator = new Random();
        String id = Integer.toString(generator.nextInt(Integer.SIZE - 1));
        while (checkUniqueness(id,eventSourceApi)){
            id = Integer.toString(generator.nextInt(Integer.SIZE - 1));
        }
        TopicId topicId = new TopicId(id);
        createTopicOutputBuilder.setTopicId(topicId);
        final CreateTopicOutput cto = createTopicOutputBuilder.build();
        Future<RpcResult<CreateTopicOutput>> result = Util.resultRpcSuccessFor(cto);
        return result;
    }

    public static boolean checkUniqueness(String nodeName, EventSourceApiImpl eventSourceApi){
        return eventSourceApi.getTopicsPerNotifications().keySet().stream()
                .anyMatch(topicId -> topicId.getValue().equals(nodeName));
    }

    public static void checkExampleEventSource(ExampleEventSource exampleEventSource, String nodeName,EventSourceApiImpl eventSourceApi){
        NodeKey nodeKey = exampleEventSource.getSourceNodeKey();
        assertEquals(nodeName,nodeKey.getNodeId().getValue());

        List<SchemaPath> notifications = exampleEventSource.getAvailableNotifications();
        checkNotifications(notifications,eventSourceApi);
    }

    public static void checkNotifications(List<SchemaPath> notifications, EventSourceApiImpl eventSourceApi){
        assertEquals("urn:opendaylight:unimgr:mef:notification:es:example:notification",eventSourceApi.getSchemaPathString(notifications.get(0)));
        assertEquals("urn:opendaylight:unimgr:mef:notification:es:test:notification",eventSourceApi.getSchemaPathString(notifications.get(1)));
        assertEquals("ovs",eventSourceApi.getSchemaPathString(notifications.get(2)));
    }
}
