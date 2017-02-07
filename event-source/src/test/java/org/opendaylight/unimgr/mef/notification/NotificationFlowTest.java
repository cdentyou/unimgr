package org.opendaylight.unimgr.mef.notification;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationService;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper;
import org.opendaylight.unimgr.mef.notification.model.listener.EventSourceListenerImpl;
import org.opendaylight.unimgr.mef.notification.model.reader.BaObjectNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.reader.BiObjectNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.reader.DomNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.reader.StringMessageNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Class created to test sending notifications from {@link org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper}
 * and receiving them in {@link org.opendaylight.unimgr.mef.notification.model.listener.EventSourceListenerImpl}.
 */
@RunWith(PowerMockRunner.class)
public class NotificationFlowTest {

    private Broker brokerMock;
    private EventSourceRegistry eventSourceRegistryMock;
    private BindingAwareBroker bindingAwareBrokerMock;
    private EventAggregatorService eventAggregatorServiceMock;
    private DOMNotificationPublishService domPublishMock;
    private DOMNotificationService domNotificationServiceMock;
    private RpcProviderRegistry rpcProviderRegistryMock;
    private ListenerRegistration<EventSourceListenerImpl> listenerRegistrationMock;
    private BindingAwareBroker.ProviderContext bindingCtxMock;
    private Broker.ProviderSession domCtxMock;

    private String testNotificationName = "notType";
    private String testNotificationName2 = "test";
    private String testWrapperName = "Wrapper";
    private NotificationType notificationTypeForWrapper;
    private NotificationType notificationTypeForWrapper2;
    private NotificationType notificationTypeForListener;
    private NodeId nodeId;
    private EventSourceWrapper eventSourceWrapper;
    private Notifications notifications;

    public void mockAll(){
        //given
        brokerMock = mock(Broker.class);
        eventSourceRegistryMock = mock(EventSourceRegistry.class);
        bindingAwareBrokerMock = mock(BindingAwareBroker.class);

        bindingCtxMock = mock(BindingAwareBroker.ProviderContext.class);
        rpcProviderRegistryMock = mock(RpcProviderRegistry.class);
        eventAggregatorServiceMock = mock(EventAggregatorService.class);
        domCtxMock = mock(Broker.ProviderSession.class);
        domPublishMock = mock(DOMNotificationPublishService.class);
        domNotificationServiceMock = mock(DOMNotificationService.class);
        listenerRegistrationMock = (ListenerRegistration<EventSourceListenerImpl>) mock(ListenerRegistration.class);

        notificationTypeForWrapper = new NotificationType(testNotificationName);
        notificationTypeForWrapper2 = new NotificationType(testNotificationName2);
        notificationTypeForListener= new NotificationType(testNotificationName);
    }

    private void setEventSourceWrapper(){
        nodeId = new NodeId(testWrapperName);
        eventSourceWrapper = new EventSourceWrapper(nodeId, eventSourceRegistryMock,brokerMock);
        eventSourceWrapper.add(notificationTypeForWrapper);
        eventSourceWrapper.add(notificationTypeForWrapper2);
    }

    private EventSourceListenerImpl setListener(DomNotificationReader reader){
        notifications = new Notifications();
        notifications.add(notificationTypeForListener);
        EventSourceListenerImpl eventSourceListener = new EventSourceListenerImpl(eventAggregatorServiceMock,domNotificationServiceMock,rpcProviderRegistryMock, reader);
        return eventSourceListener;
    }

    @Before
    public void beforeTest() {
        mockAll();

        //EventSourceWrapper constructor calls mock
        when(rpcProviderRegistryMock.getRpcService(EventAggregatorService.class)).thenReturn(eventAggregatorServiceMock);
        when(bindingCtxMock.getSALService(RpcProviderRegistry.class)).thenReturn(rpcProviderRegistryMock);
        when(bindingAwareBrokerMock.registerProvider(any())).thenReturn(bindingCtxMock);
        when(domCtxMock.getService(DOMNotificationPublishService.class)).thenReturn(domPublishMock);
        when(brokerMock.registerProvider(any())).thenReturn(domCtxMock);

        //EventSourceListenerImpl constructor calls mock
        when(domNotificationServiceMock.registerNotificationListener(mock(EventSourceListenerImpl.class), SchemaPath.create(true, TopicNotification.QNAME))).thenReturn(listenerRegistrationMock);
        when(eventAggregatorServiceMock.createTopic(any())).thenReturn(EventSourceTestUtils.createTopicMock());

        setEventSourceWrapper();
    }

    /**
     * Testcase checks sending 2 notifications from 2 Event Source to single Listener (String objects are payload of notifications).
     * Testcase scenario:
     * 1. Two {@link org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper} are created with slightly different name and with the same {@link org.opendaylight.unimgr.mef.notification.model.types.NotificationType} (one wrapper has one NotificationType extra).
     * 2. Topic is created via {@link org.opendaylight.unimgr.mef.notification.topic.TopicHandler} to match both previously created {@link org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper}.
     * 3. Two messages are sent from two different {@link org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper}.
     * 4. Put call is verified.
     * 5. Received Strings are compared to the sent ones.
     */
    @Test
    public void testSendAndReceiveStringNotification(){
        //given
        String firstTestMessage = "First test message.";
        String secondTestMessage = "Second test message.";
        StringMessageNotificationReader stringMessageNotificationReader = new StringMessageNotificationReader();
        EventSourceListenerImpl eventSourceListener = setListener(stringMessageNotificationReader);

        String testWrapperName2 = "Wrapper2";
        NodeId nodeId2 = new NodeId(testWrapperName2);
        EventSourceWrapper eventSourceWrapper2 = new EventSourceWrapper(nodeId2, eventSourceRegistryMock,brokerMock);
        eventSourceWrapper2.add(notificationTypeForWrapper);

        NodeId nodeWithPattern = new NodeId("Wrapper*");

        //when
        String topicId = eventSourceListener.readTopic(nodeWithPattern,notifications);
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper.getNotifications()));
        eventSourceWrapper2.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper2.getNotifications()));


        mockNotificationReceive(eventSourceListener);
        eventSourceWrapper.putMsg(notificationTypeForWrapper,firstTestMessage);
        eventSourceWrapper2.putMsg(notificationTypeForWrapper,secondTestMessage);

        //then
        verifyCall(true,2);
        List<String> receivedMessages = stringMessageNotificationReader.getReceivedMessages();
        EventSourceTestUtils.checkMessages(receivedMessages,testWrapperName,firstTestMessage,testWrapperName2,secondTestMessage);
    }

    /**
     * Testcase checks sending multiple notifications from single Event Source to single Listener (BA objects are payload of notifications).
     * Testcase scenario:
     *  1. {@link org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper} sends BA Objects via put and offer method (encoded to BI by {@link org.opendaylight.unimgr.mef.notification.message.NotificationCodec}).
     *  2. BI object is received in {@link org.opendaylight.unimgr.mef.notification.model.listener.EventSourceListenerImpl}
     *  3. Received BI object is decoded to BA Object by {@link org.opendaylight.unimgr.mef.notification.message.NotificationCodec}.
     *  4. Put and Offer method calls are verified.
     *  5. Received BA object are compared to the sent ones.
     */
    @Test
    public void testSendAndReceiveBaNotification(){
        //given
        Node node = EventSourceTestUtils.prepareTestNode("TestNodeId",true);
        Node node2 = EventSourceTestUtils.prepareTestNode("TestNodeId2",false);
        InstanceIdentifier instanceIdentifier = EventSourceTestUtils.prepareTestNodeInstanceIdentifier(node.getNodeId());
        InstanceIdentifier instanceIdentifier2 = EventSourceTestUtils.prepareTestNodeInstanceIdentifier(node2.getNodeId());

        BaObjectNotificationReader baObjectNotificationReader = new BaObjectNotificationReader();
        EventSourceListenerImpl eventSourceListener = setListener(baObjectNotificationReader);

        //when
        String topicId = eventSourceListener.readTopic(nodeId,notifications);
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper.getNotifications()));

        //send 2 notification - one by put and one by offer
        mockNotificationReceive(eventSourceListener);
        eventSourceWrapper.putMsg(notificationTypeForWrapper,node,instanceIdentifier);
        when(domPublishMock.offerNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener));
        eventSourceWrapper.offerMsg(notificationTypeForWrapper,node2,instanceIdentifier2);

        //then
        verifyCall(true,1);
        verifyCall(false,1);
        EventSourceTestUtils.checkPassedBaObjects(baObjectNotificationReader.getBaObjects(),node,node2);
    }


    /**
     * Testcase check sending single notifications from single Event Source to two Listener (BI objects are payload of notifications).
     * Testcase scenario:
     * 1. Two {@link org.opendaylight.unimgr.mef.notification.model.listener.EventSourceListenerImpl} creates topic to subscribe to single {@link org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper}.
     * 2. {@link org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper} sends message.
     * 3. Put call is verified.
     * 4. Received BI Object (in both {@link org.opendaylight.unimgr.mef.notification.model.listener.EventSourceListenerImpl}) is compared to the sent one.
     */
    @Test
    public void testSendAndReceiveBiNotification(){
        //given
        LeafNode<String> leafNode = EventSourceTestUtils.prepareTestLeafNode();
        BiObjectNotificationReader biObjectNotificationReader = new BiObjectNotificationReader();
        BiObjectNotificationReader biObjectNotificationReader2 = new BiObjectNotificationReader();
        EventSourceListenerImpl eventSourceListener = setListener(biObjectNotificationReader);
        EventSourceListenerImpl eventSourceListener2 = setListener(biObjectNotificationReader2);

        //when
        String topicId = eventSourceListener.readTopic(nodeId,notifications);
        String topicId2 = eventSourceListener2.readTopic(nodeId,notifications);
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper.getNotifications()));
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId2),eventSourceWrapper.getNotifications()));

        mockNotificationReceive(eventSourceListener,eventSourceListener2);
        eventSourceWrapper.putMsg(notificationTypeForWrapper,leafNode);

        //then
        verifyCall(true,1);
        List<DataContainerChild> receivedBiObjects = biObjectNotificationReader.getBiObjects();
        List<DataContainerChild> receivedBiObjects2 = biObjectNotificationReader2.getBiObjects();
        EventSourceTestUtils.checkPassedBiObject(receivedBiObjects,leafNode);
        EventSourceTestUtils.checkPassedBiObject(receivedBiObjects2,leafNode);
    }

    private void verifyCall(boolean put,int times){
        if(put){
            verifyPutCall(times);
        } else {
            verify(domPublishMock,times(times)).offerNotification(any());
        }
    }

    //mock notification receiving
    private void mockNotificationReceive(EventSourceListenerImpl eventSourceListener){
        try {
            when(domPublishMock.putNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener))
                    .thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener));
        } catch (InterruptedException e) {
            fail("InterruptException");
        }
    }

    private void mockNotificationReceive(EventSourceListenerImpl eventSourceListener, EventSourceListenerImpl eventSourceListener2){
        try {
            when(domPublishMock.putNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener,eventSourceListener2));
        } catch (InterruptedException e) {
            fail("InterruptException");
        }
    }

    private void verifyPutCall(int times){
        try {
            verify(domPublishMock,times(times)).putNotification(any());
        } catch (InterruptedException e) {
            fail("InterruptException");
        }
    }

    private class NotificationAnswer<T> implements Answer<T> {
        private EventSourceListenerImpl eventSourceListener;
        private EventSourceListenerImpl eventSourceListener2=null;

        public NotificationAnswer(EventSourceListenerImpl eventSourceListener){
            this.eventSourceListener = eventSourceListener;
        }

        public NotificationAnswer(EventSourceListenerImpl eventSourceListener, EventSourceListenerImpl eventSourceListener2){
            this.eventSourceListener = eventSourceListener;
            this.eventSourceListener2 = eventSourceListener2;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            DOMNotification notification = invocation.getArgumentAt(0, DOMNotification.class);
            eventSourceListener.onNotification(notification);
            if(eventSourceListener2!=null){
                eventSourceListener2.onNotification(notification);
            }
            return null;
        }
    }
}