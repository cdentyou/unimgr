package org.opendaylight.unimgr.mef.notification.model.eventsource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.EventSourceTestUtils;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.topic.TopicHandler;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class EventSourceWrapperTest {
    private Broker brokerMock;
    private EventSourceRegistry eventSourceRegistry;
    private BindingAwareBroker bindingAwareBrokerMock;
    private EventAggregatorService eventAggregatorServiceMock;
    private DOMNotificationPublishService domPublishMock;
    private RpcProviderRegistry rpcProviderRegistryMock;

    @Before
    public void setUp(){
        //given
        brokerMock = mock(Broker.class);
        eventSourceRegistry = mock(EventSourceRegistry.class);
        bindingAwareBrokerMock = mock(BindingAwareBroker.class);

        BindingAwareBroker.ProviderContext bindingCtxMock = mock(BindingAwareBroker.ProviderContext.class);
        rpcProviderRegistryMock = mock(RpcProviderRegistry.class);
        eventAggregatorServiceMock = mock(EventAggregatorService.class);
        Broker.ProviderSession domCtxMock = mock(Broker.ProviderSession.class);
        domPublishMock = mock(DOMNotificationPublishService.class);

        when(rpcProviderRegistryMock.getRpcService(EventAggregatorService.class)).thenReturn(eventAggregatorServiceMock);
        when(bindingCtxMock.getSALService(RpcProviderRegistry.class)).thenReturn(rpcProviderRegistryMock);
        when(bindingAwareBrokerMock.registerProvider(any())).thenReturn(bindingCtxMock);
        when(domCtxMock.getService(DOMNotificationPublishService.class)).thenReturn(domPublishMock);
        when(brokerMock.registerProvider(any())).thenReturn(domCtxMock);
    }

    @Test
    public void testPutMsg(){
        //given
        when(eventAggregatorServiceMock.createTopic(any()))
                .thenReturn(EventSourceTestUtils.createTopicMock());

        NodeId nodeId = new NodeId("Wrapper");
        EventSourceWrapper eventSourceWrapper = new EventSourceWrapper(nodeId,eventSourceRegistry,brokerMock);
        eventSourceWrapper.add(new NotificationType("notType"));
        eventSourceWrapper.add(new NotificationType("testets"));

        TopicHandler topicHandler = new TopicHandler(eventAggregatorServiceMock);
        String topicId = topicHandler.createTopic(nodeId,eventSourceWrapper.getNotifications());
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper.getNotifications()));

        Node node = EventSourceTestUtils.prepareTestNode("TestNode",true);
        InstanceIdentifier instanceIdentifier = EventSourceTestUtils.prepareTestNodeInstanceIdentifier(node.getNodeId());
        DataContainer dataContainer = node;

        //when
        eventSourceWrapper.putMsg(new NotificationType("notType"),dataContainer,instanceIdentifier);

        //then
        List<SchemaPath> notifications = eventSourceWrapper.getEventSource().getAvailableNotifications();
        assertEquals(2,notifications.size());
        try {
            verify(domPublishMock).putNotification(any());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
