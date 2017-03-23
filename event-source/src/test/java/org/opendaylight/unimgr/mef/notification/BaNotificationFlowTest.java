package org.opendaylight.unimgr.mef.notification;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Class created to test sending notifications from {@link org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper}
 * and receiving them in {@link org.opendaylight.unimgr.mef.notification.listener.NotificationListenerImpl}.
 */
@RunWith(PowerMockRunner.class)
public class BaNotificationFlowTest extends AbstractNotificationFlowTest {

//    private BindingAwareBroker bindingAwareBrokerMock = mock(BindingAwareBroker.class);
//    private NotificationPublishService notificationPublishServiceMock = mock(NotificationPublishService.class);
//    private NotificationService notificationServiceMock = mock(NotificationService.class);
//
//    //payload
//    private Node node = EventSourceTestUtils.prepareTestNode("TestNodeId",true);
//    private Node node2 = EventSourceTestUtils.prepareTestNode("TestNodeId2",false);
//
//    @Before
//    public void beforeTest() {
//        BindingAwareBroker.ProviderContext providerContext = mock(BindingAwareBroker.ProviderContext.class);
//        when(providerContext.getSALService(NotificationPublishService.class)).thenReturn(notificationPublishServiceMock);
//        when(bindingAwareBrokerMock.registerProvider(any(BaEventSourceProvider.class))).thenReturn(providerContext);
//
//        ListenerRegistration<BaNotificationListenerImpl> listenerRegistration = mock(ListenerRegistration.class);
//        when(eventAggregatorServiceMock.createTopic(any())).thenReturn(EventSourceTestUtils.createTopicMock());
//        when(notificationServiceMock.registerNotificationListener(any(BaNotificationListenerImpl.class))).thenReturn(listenerRegistration);
//    }
//
//    /**
//     * Testcase checks sending multiple notifications from single Event Source to single Listener
//     * Testcase scenario:
//     *  1. {@link org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper} sends BA Objects via offer method (to different NotificationTypes to which Listener subscribe to).
//     *  2. BI objects are received in {@link org.opendaylight.unimgr.mef.notification.listener.BaNotificationListenerImpl}
//     *  4. Offer method calls are verified.
//     *  5. Received BA object are compared to the sent ones.
//     */
//    @Test
//    public void testSendAndReceiveBaNotifications(){
//        //given
//        //event source
//        BaEventSourceWrapper baEventSourceWrapper = setBaEventSourceWrapper(nodeId);
//        baEventSourceWrapper.add(notificationTypeForWrapper2);
//
//        //listener
//        BaNotificationReader baNotificationReader = new BaNotificationReader<Node>();
//        BaNotificationListenerImpl baNotificationListener = new BaNotificationListenerImpl(rpcProviderRegistryMock, notificationServiceMock, baNotificationReader);
//        notifications.add(notificationTypeForListener);
//        notifications.add(notificationTypeForListener2);
//
//        //when
//        String topicId = baNotificationListener.readTopic(nodeId,notifications);
//        baEventSourceWrapper.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),baEventSourceWrapper.getNotifications()));
//
//        NotificationAnswer<ListenableFuture> answer = new NotificationAnswer<>(baNotificationListener,null);
//        when(notificationPublishServiceMock.offerNotification(any()))
//                .thenAnswer(answer)
//                .thenAnswer(answer);
//        baEventSourceWrapper.offerNotification(notificationTypeForWrapper,node);
//        baEventSourceWrapper.offerNotification(notificationTypeForWrapper2,node2);
//
//        //then
//        verify(notificationPublishServiceMock,times(2)).offerNotification(any());
//        List<Node> nodeList = baNotificationReader.getReceivedNotifications();
//        assertEquals(2,nodeList.size());
//        assertEquals(node,nodeList.get(0));
//        assertEquals(node2,nodeList.get(1));
//    }
//
//    /**
//     * Testcase check sending single notifications from single Event Source to two Listener .
//     * Testcase scenario:
//     * 1. Two {@link org.opendaylight.unimgr.mef.notification.listener.BaNotificationListenerImpl} creates topic to subscribe to single {@link org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper} .
//     * 2. {@link org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper} sends message.
//     * 3. Put call is verified.
//     * 4. Received BA Object (in both {@link org.opendaylight.unimgr.mef.notification.listener.BaNotificationListenerImpl}) are compared to the sent one.
//     */
//    @Test
//    public void sendAndReciveBaNotificationsOnMultipleListeners(){
//        //given
//        BaEventSourceWrapper baEventSourceWrapper = setBaEventSourceWrapper(nodeId);
//
//        BaNotificationReader baNotificationReader = new BaNotificationReader<Node>();
//        BaNotificationReader baNotificationReader2 = new BaNotificationReader<Node>();
//        BaNotificationListenerImpl baNotificationListener = new BaNotificationListenerImpl(rpcProviderRegistryMock, notificationServiceMock, baNotificationReader);
//        BaNotificationListenerImpl baNotificationListener2 = new BaNotificationListenerImpl(rpcProviderRegistryMock, notificationServiceMock, baNotificationReader2);
//        notifications.add(notificationTypeForListener);
//
//        //when
//        String topicId = baNotificationListener.readTopic(nodeId,notifications);
//        baEventSourceWrapper.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),baEventSourceWrapper.getNotifications()));
//        topicId = baNotificationListener2.readTopic(nodeId,notifications);
//        baEventSourceWrapper.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),baEventSourceWrapper.getNotifications()));
//
//        try {
//            doAnswer(new NotificationAnswer(baNotificationListener,baNotificationListener2)).when(notificationPublishServiceMock).putNotification(any());
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//        baEventSourceWrapper.putNotification(notificationTypeForWrapper,node);
//
//        //then
//        verifyPutCall(1);
//        List<Node> nodeList = baNotificationReader.getReceivedNotifications();
//        List<Node> nodeList2 = baNotificationReader2.getReceivedNotifications();
//        assertEquals(1, nodeList.size());
//        assertEquals(1, nodeList2.size());
//        assertEquals(node,nodeList.get(0));
//        assertEquals(node,nodeList2.get(0));
//    }
//
//    /**
//     * Testcase checks sending 2 notifications from 2 Event Source to single Listener (String objects are payload of notifications).
//     * Testcase scenario:
//     * 1. Two {@link org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper} are created with slightly different name and with the same {@link org.opendaylight.unimgr.mef.notification.model.types.NotificationType} (one wrapper has one NotificationType extra).
//     * 2. Two different BA Objects are sent from two different {@link org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper}.
//     * 3. Offer call is verified.
//     * 4. Received BA Objects are compared to the sent ones.
//     */
//    @Test
//    public void sendFromMultipleSourcesAndReciveBaNotifications(){
//        //given
//        BaEventSourceWrapper baEventSourceWrapper = setBaEventSourceWrapper(nodeId);
//        baEventSourceWrapper.add(notificationTypeForWrapper2);
//        BaEventSourceWrapper baEventSourceWrapper2 = setBaEventSourceWrapper(new NodeId("Wrapper2"));
//
//        BaNotificationReader baNotificationReader = new BaNotificationReader<Node>();
//        BaNotificationListenerImpl baNotificationListener = new BaNotificationListenerImpl(rpcProviderRegistryMock, notificationServiceMock, baNotificationReader);
//        notifications.add(notificationTypeForListener);
//        notifications.add(notificationTypeForListener2);
//
//        //when
//        String topicId = baNotificationListener.readTopic(nodeId,notifications);
//        baEventSourceWrapper.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),baEventSourceWrapper.getNotifications()));
//        baEventSourceWrapper2.getEventSourceImpl().joinTopic(EventSourceTestUtils.createJoinTopicInput(new TopicId(topicId),baEventSourceWrapper2.getNotifications()));
//
//        NotificationAnswer<ListenableFuture> answer = new NotificationAnswer<>(baNotificationListener,null);
//        when(notificationPublishServiceMock.offerNotification(any()))
//                .thenAnswer(answer)
//                .thenAnswer(answer);
//        baEventSourceWrapper.offerNotification(notificationTypeForWrapper2,node2);
//        baEventSourceWrapper2.offerNotification(notificationTypeForWrapper,node);
//
//        //then
//        verify(notificationPublishServiceMock,times(2)).offerNotification(any());
//        List<Node> nodeList = baNotificationReader.getReceivedNotifications();
//        assertEquals(2,nodeList.size());
//        assertEquals(node2,nodeList.get(0));
//        assertEquals(node,nodeList.get(1));
//    }
//
//    private BaEventSourceWrapper setBaEventSourceWrapper(NodeId nodeId){
//        BaEventSourceWrapper baEventSourceWrapper = null;//new BaEventSourceWrapper(nodeId,eventSourceRegistryMock,bindingAwareBrokerMock,null);
//        baEventSourceWrapper.add(notificationTypeForWrapper);
//        return baEventSourceWrapper;
//    }
//
//    private void verifyPutCall(int times){
//        try {
//            verify(notificationPublishServiceMock,times(times)).putNotification(any());
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    private class NotificationAnswer<T> implements Answer<T> {
//        private BaNotificationListenerImpl baNotificationListener;
//        private BaNotificationListenerImpl baNotificationListener2;
//
//        public NotificationAnswer(BaNotificationListenerImpl baNotificationListener, BaNotificationListenerImpl baNotificationListener2){
//            this.baNotificationListener = baNotificationListener;
//            this.baNotificationListener2 = baNotificationListener2;
//        }
//
//        @Override
//        public T answer(InvocationOnMock invocation) throws Throwable {
//            Notification notification = invocation.getArgumentAt(0, Notification.class);
//            baNotificationListener.onNotification(notification);
//            if(baNotificationListener2!=null){
//                baNotificationListener2.onNotification(notification);
//            }
//            return null;
//        }
//    }
}
