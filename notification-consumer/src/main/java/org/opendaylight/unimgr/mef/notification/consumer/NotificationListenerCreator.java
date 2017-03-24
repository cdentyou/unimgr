package org.opendaylight.unimgr.mef.notification.consumer;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.listener.NotificationListenerImpl;
import org.opendaylight.unimgr.mef.notification.listener.reader.BaNotificationReader;
import org.opendaylight.unimgr.mef.notification.listener.reader.BiNotificationReader;
import org.opendaylight.unimgr.mef.notification.listener.reader.StringMessageNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationListenerCreator {

    final private BindingAwareBroker bindingAwareBroker;
    private Broker broker;

    public NotificationListenerCreator(BindingAwareBroker bindingAwareBroker, Broker broker){
        this.bindingAwareBroker = bindingAwareBroker;
        this.broker = broker;
    }

    public void startUp(){
        createBiNotificationListener();
        createBaNotificationListener();
        createStringNotificationListener();
    }

    private void createBaNotificationListener(){
        BaNotificationReader baNotificationReader = new BaNotificationReader();
        NotificationListenerImpl notificationListener = new NotificationListenerImpl(bindingAwareBroker,broker,baNotificationReader);
        NodeId nodeId = new NodeId("BaEventSource");
        Notifications notifications = createNotifications("binding-aware-pattern");
        notificationListener.readTopic(nodeId,notifications);
    }

    private void createBiNotificationListener(){
        BiNotificationReader biNotificationReader = new BiNotificationReader();
        NotificationListenerImpl biNotificationListener = new NotificationListenerImpl(bindingAwareBroker,broker,biNotificationReader);
        NodeId nodeId = new NodeId("BiEventSource");
        Notifications notifications = createNotifications("binding-independent-pattern");
        biNotificationListener.readTopic(nodeId,notifications);
    }

    private void createStringNotificationListener(){
        StringMessageNotificationReader stringMessageNotificationReader = new StringMessageNotificationReader();
        NotificationListenerImpl biNotificationListener = new NotificationListenerImpl(bindingAwareBroker,broker,stringMessageNotificationReader);
        NodeId nodeId = new NodeId("StringEventSource");
        Notifications notifications = createNotifications("string-pattern");
        biNotificationListener.readTopic(nodeId,notifications);
    }

    private Notifications createNotifications(String notificationName){
        NotificationType notificationType = new NotificationType(notificationName);
        Notifications notifications = new Notifications();
        notifications.add(notificationType);
        return notifications;
    }
}
