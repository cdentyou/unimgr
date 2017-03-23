package org.opendaylight.unimgr.mef.notification;

import org.junit.runner.RunWith;
import org.opendaylight.unimgr.mef.notification.listener.NotificationListener;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Class created to test sending notifications from {@link org.opendaylight.unimgr.mef.notification.eventsource.BiEventSourceWrapper}
 * and receiving them in {@link NotificationListener}.
 */
@RunWith(PowerMockRunner.class)
public class BiNotificationFlowTest extends AbstractNotificationFlowTest{

//    private Broker brokerMock = mock(Broker.class);
//    private DOMNotificationPublishService domPublishMock = mock(DOMNotificationPublishService.class);
//    private DOMNotificationService domNotificationServiceMock = mock(DOMNotificationService.class);
//
//    @Before
//    public void beforeTest() {
//        ListenerRegistration<NotificationListenerImpl> listenerRegistrationMock = (ListenerRegistration<NotificationListenerImpl>) mock(ListenerRegistration.class);
//        Broker.ProviderSession domCtxMock = mock(Broker.ProviderSession.class);
//
//        when(rpcProviderRegistryMock.getRpcService(EventAggregatorService.class)).thenReturn(eventAggregatorServiceMock);
//        when(domCtxMock.getService(DOMNotificationPublishService.class)).thenReturn(domPublishMock);
//        when(brokerMock.registerProvider(any())).thenReturn(domCtxMock);
//
//        //EventSourceListenerImpl constructor calls mock
//        when(domNotificationServiceMock.registerNotificationListener(mock(NotificationListenerImpl.class), SchemaPath.create(true, TopicNotification.QNAME))).thenReturn(listenerRegistrationMock);
//        when(eventAggregatorServiceMock.createTopic(any())).thenReturn(EventSourceTestUtils.createTopicMock());
//    }
//
//    /**
//     * Testcase checks sending 2 notifications from 2 Event Source to single Listener (String wrapped into Bi Objects are payload of notifications).
//     * Testcase scenario:
//     * 1. Two {@link org.opendaylight.unimgr.mef.notification.eventsource.BiEventSourceWrapper} are created with slightly different name and with the same {@link org.opendaylight.unimgr.mef.notification.model.types.NotificationType} (one wrapper has one NotificationType extra).
//     * 2. Topic is created via {@link org.opendaylight.unimgr.mef.notification.utils.TopicHandler} to match both previously created {@link org.opendaylight.unimgr.mef.notification.eventsource.BiEventSourceWrapper}.
//     * 3. Two messages are sent from two different {@link org.opendaylight.unimgr.mef.notification.eventsource.BiEventSourceWrapper}.
//     * 4. Put and offer calls are verified.
//     * 5. Received Strings are compared to the sent ones.
//     */
//    @Test
//    public void testSendAndReceiveStringNotification(){
//        //given
//        String firstTestMessage = "First test message.";
//        String secondTestMessage = "Second test message.";
//        String testWrapperName2 = "Wrapper2";
//        StringMessageNotificationReader stringMessageNotificationReader = new StringMessageNotificationReader();
//        NotificationListenerImpl biNotificationListener = new NotificationListenerImpl(rpcProviderRegistryMock, domNotificationServiceMock,stringMessageNotificationReader);
//
//        BiEventSourceWrapper biEventSourceWrapper = setBiEventSourceWrapper(nodeId);
//        BiEventSourceWrapper biEventSourceWrapper2 = setBiEventSourceWrapper(new NodeId(testWrapperName2));
//        notifications.add(notificationTypeForListener);
//
//        NodeId nodeWithPattern = new NodeId("Wrapper*");
//
//        //when
//        String topicId = biNotificationListener.readTopic(nodeWithPattern,notifications);
//        biEventSourceWrapper.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),biEventSourceWrapper.getNotifications()));
//        biEventSourceWrapper2.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),biEventSourceWrapper2.getNotifications()));
//
//        mockNotificationReceive(biNotificationListener,null);
//        when(domPublishMock.offerNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(biNotificationListener,null));
//        biEventSourceWrapper.putNotification(notificationTypeForWrapper,firstTestMessage);
//        biEventSourceWrapper2.offerNotification(notificationTypeForWrapper,secondTestMessage);
//
//        //then
//        verifyPutCall(1);
//        verify(domPublishMock,times(1)).offerNotification(any());
//        List<String> receivedMessages = stringMessageNotificationReader.getReceivedMessages();
//        EventSourceTestUtils.checkMessages(receivedMessages,testWrapperName,firstTestMessage,testWrapperName2,secondTestMessage);
//    }
//
//    /**
//     * Testcase check sending single notifications from single Event Source to two Listener (BI objects are payload of notifications).
//     * Testcase scenario:
//     * 1. Two {@link NotificationListenerImpl} creates topic to subscribe to single {@link org.opendaylight.unimgr.mef.notification.eventsource.BiEventSourceWrapper}.
//     * 2. {@link org.opendaylight.unimgr.mef.notification.eventsource.BiEventSourceWrapper} sends message.
//     * 3. Put call is verified.
//     * 4. Received BI Object (in both {@link NotificationListenerImpl}) are compared to the sent one.
//     */
//    @Test
//    public void testSendAndReceiveBiNotification(){
//        //given
//        BiEventSourceWrapper biEventSourceWrapper = setBiEventSourceWrapper(nodeId);
//        LeafNode<String> leafNode = EventSourceTestUtils.prepareTestLeafNode();
//        BiNotificationReader biObjectNotificationReader = new BiNotificationReader();
//        BiNotificationReader biObjectNotificationReader2 = new BiNotificationReader();
//        NotificationListenerImpl biNotificationListener = new NotificationListenerImpl(rpcProviderRegistryMock, domNotificationServiceMock,biObjectNotificationReader);
//        NotificationListenerImpl biNotificationListener2 = new NotificationListenerImpl(rpcProviderRegistryMock, domNotificationServiceMock,biObjectNotificationReader2);
//        notifications.add(notificationTypeForListener);
//
//        //when
//        String topicId = biNotificationListener.readTopic(nodeId,notifications);
//        biNotificationListener2.readTopic(nodeId,notifications);
//        biEventSourceWrapper.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),biEventSourceWrapper.getNotifications()));
//
//        mockNotificationReceive(biNotificationListener,biNotificationListener2);
//        biEventSourceWrapper.putNotification(notificationTypeForWrapper,leafNode);
//
//        //then
//        verifyPutCall(1);
//        List<DataContainerChild> receivedBiObjects = biObjectNotificationReader.getBiObjects();
//        List<DataContainerChild> receivedBiObjects2 = biObjectNotificationReader2.getBiObjects();
//        EventSourceTestUtils.checkPassedBiObject(receivedBiObjects,leafNode);
//        EventSourceTestUtils.checkPassedBiObject(receivedBiObjects2,leafNode);
//    }
//
//    private BiEventSourceWrapper setBiEventSourceWrapper(NodeId nodeId){
//        BiEventSourceWrapper biEventSourceWrapper = new BiEventSourceWrapper(nodeId,eventSourceRegistryMock,brokerMock);
//        biEventSourceWrapper.add(notificationTypeForWrapper);
//        return  biEventSourceWrapper;
//    }
//
//    private void mockNotificationReceive(NotificationListenerImpl biNotificationListener, NotificationListenerImpl biNotificationListener2){
//        try {
//            when(domPublishMock.putNotification(any())).thenAnswer(new NotificationAnswer<ListenableFuture>(biNotificationListener,biNotificationListener2));
//        } catch (InterruptedException e) {
//            fail("InterruptException");
//        }
//    }
//
//    private void verifyPutCall(int times){
//        try {
//            verify(domPublishMock,times(times)).putNotification(any());
//        } catch (InterruptedException e) {
//            fail("InterruptException");
//        }
//    }
//
//    private class NotificationAnswer<T> implements Answer<T> {
//        private NotificationListenerImpl biNotificationListener;
//        private NotificationListenerImpl biNotificationListener2 = null;
//
//        public NotificationAnswer(NotificationListenerImpl biNotificationListener, NotificationListenerImpl biNotificationListener2){
//            this.biNotificationListener = biNotificationListener;
//            this.biNotificationListener2 = biNotificationListener2;
//        }
//
//        @Override
//        public T answer(InvocationOnMock invocation) throws Throwable {
//            DOMNotification notification = invocation.getArgumentAt(0, DOMNotification.class);
//            biNotificationListener.onNotification(notification);
//            if(biNotificationListener2!=null){
//                biNotificationListener2.onNotification(notification);
//            }
//            return null;
//        }
//    }
}
