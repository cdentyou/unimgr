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
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Class created to test sending messages from @see org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper
 * and receiving them in @see org.opendaylight.unimgr.mef.notification.model.listener.EventSourceListenerImpl
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

    @Test
    public void testPutAndReceiveStringMessage(){
        testSendAndReceiveStringMessage(true);
    }

    @Test
    public void testOfferAndReceiveStringMessage(){
        testSendAndReceiveStringMessage(false);
    }

    @Test
    public void testPutAndReceiveBiMessage(){
        testSendAndReceiveBiMessage(true);
    }

    @Test
    public void testOfferAndReceiveBiMessage(){
        testSendAndReceiveBiMessage(false);
    }

    @Test
    public void testPutAndReceiveBaMessage(){
        testSendAndReceiveBaMessage(true);
    }
//
//    @Test
//    public void testOfferAndReceiveBaMessage(){
//        testSendAndReceiveBaMessage(false);
//    }

    public void testSendAndReceiveBaMessage(boolean put){
        //given
        Node node = EventSourceTestUtils.prepareTestNode();
        InstanceIdentifier instanceIdentifier = EventSourceTestUtils.prepareTestNodeInstanceIdentifier(node.getNodeId());
        BaObjectNotificationReader baObjectNotificationReader = new BaObjectNotificationReader();
        EventSourceListenerImpl eventSourceListener = setListener(baObjectNotificationReader);

        //when
        String topicId = eventSourceListener.readTopic(nodeId,notifications);
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper.getNotifications()));

        if(put){
            mockNotificationReceive(eventSourceListener);
            eventSourceWrapper.putMsg(notificationTypeForWrapper,node,instanceIdentifier);
        } else {
            when(domPublishMock.offerNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener));
            eventSourceWrapper.offerMsg(notificationTypeForWrapper,node,instanceIdentifier);
        }

        //then
        verifyCall(put);
        checkPassedBAObject(baObjectNotificationReader.getBaObjects(),node);
    }

    private void testSendAndReceiveStringMessage(boolean put){
        //given
        String testMessage = "This is test message.";
        StringMessageNotificationReader stringMessageNotificationReader = new StringMessageNotificationReader();
        EventSourceListenerImpl eventSourceListener = setListener(stringMessageNotificationReader);

        //when
        String topicId = eventSourceListener.readTopic(nodeId,notifications);
        //we have to simulate this call
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper.getNotifications()));

        if(put){
            mockNotificationReceive(eventSourceListener);
            eventSourceWrapper.putMsg(notificationTypeForWrapper,testMessage);
        } else {
            when(domPublishMock.offerNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener));
            eventSourceWrapper.offerMsg(notificationTypeForWrapper,testMessage);
        }

        //then
        verifyCall(put);
        String receivedMessage = stringMessageNotificationReader.getMessages();
        checkMessage(receivedMessage,testMessage);
    }

    private void testSendAndReceiveBiMessage(boolean put){
        //given
        LeafNode<String> leafNode = EventSourceTestUtils.prepareTestLeafNode();
        BiObjectNotificationReader biObjectNotificationReader = new BiObjectNotificationReader();
        EventSourceListenerImpl eventSourceListener = setListener(biObjectNotificationReader);

        //when
        String topicId = eventSourceListener.readTopic(nodeId,notifications);
        eventSourceWrapper.getEventSource().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),eventSourceWrapper.getNotifications()));

        if(put){
            mockNotificationReceive(eventSourceListener);
            eventSourceWrapper.putMsg(notificationTypeForWrapper,leafNode);
        } else {
            when(domPublishMock.offerNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener));
            eventSourceWrapper.offerMsg(notificationTypeForWrapper,leafNode);
        }
        //then
        verifyCall(put);
        List<DataContainerChild> receivedBiObjects = biObjectNotificationReader.getBiObjects();
        checkbiObject(receivedBiObjects,leafNode);
    }

    private void verifyCall(boolean put){
        if(put){
            verifyPutCall();
        } else {
            verify(domPublishMock).offerNotification(any());
        }
    }

    private void checkbiObject(List<DataContainerChild> receivedBiObjects, LeafNode<String> leafNode){
        long leafs = receivedBiObjects.stream()
                .filter( x -> leafNode.equals((LeafNode<String>) x))
                .count();
        assertTrue(leafs>0);
    }

    private void checkMessage(String message,String passedMessage){
        System.out.println(message);
        assertTrue(message.contains(passedMessage));
        assertTrue(message.contains(testWrapperName));
    }

    private void checkPassedBAObject(List<DataContainer> baObjects,Node node){
        assertTrue(baObjects.contains((DataContainer) node));
    }

    //mock notification receiving
    private void mockNotificationReceive(EventSourceListenerImpl eventSourceListener){
        try {
            when(domPublishMock.putNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener))
                .thenAnswer(new NotificationAnswer<ListenableFuture>(eventSourceListener));;
        } catch (InterruptedException e) {
            fail("InterruptException");
        }
    }

    private void verifyPutCall(){
        try {
            verify(domPublishMock).putNotification(any());
        } catch (InterruptedException e) {
            fail("InterruptException");
        }
    }

    private class NotificationAnswer<T> implements Answer<T> {
        private EventSourceListenerImpl eventSourceListener;

        public NotificationAnswer(EventSourceListenerImpl eventSourceListener){
            this.eventSourceListener = eventSourceListener;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            DOMNotification notification = invocation.getArgumentAt(0, DOMNotification.class);
            eventSourceListener.onNotification(notification);
            return null;
        }
    }
}
