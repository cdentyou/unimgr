package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.utils.NotificationCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that should be used by Publisher to create EventSource. Class have methods that allows notification
 * to add and delete notifications on which client will subscribe and send Binding Aware objects to subscriber(s).
 */
public class BaEventSourceWrapper extends AbstractEventSourceWrapper{
    private static final Logger LOG = LoggerFactory.getLogger(BaEventSourceWrapper.class);
    private NotificationCodec notificationCodec;

    public BaEventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry,Broker domBroker){
        super(nodeId,eventSourceRegistry,domBroker);
        notificationCodec = NotificationCodec.getInstance();
        LOG.info("Binding Aware Wrapper started");
    }

    /**
     * Method send BA object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.binding.api.NotificationPublishService#putNotification(Notification)}.
     */
    public void putNotification(NotificationType notificationType, DataObject payload, InstanceIdentifier instanceIdentifier){
        DataContainerChild dataContainerChild = notificationCodec.toDataContainerChild(payload,instanceIdentifier);
        sendNotification(notificationType,null,dataContainerChild,true);
    }

    /**
     * Method send BA object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.binding.api.NotificationPublishService#offerNotification(Notification)}.
     */
    public void offerNotification(NotificationType notificationType, DataObject payload, InstanceIdentifier instanceIdentifier){
        DataContainerChild dataContainerChild = notificationCodec.toDataContainerChild(payload,instanceIdentifier);
        sendNotification(notificationType,null,dataContainerChild,false);
    }

    public void putNotification(NotificationType notificationType, Notification notification){
        ContainerNode containerNode = notificationCodec.toContainerNode(notification);
        sendNotification(notificationType,null,containerNode,true);
    }

    public void offerNotification(NotificationType notificationType, Notification notification){
        ContainerNode containerNode = notificationCodec.toContainerNode(notification);
        sendNotification(notificationType,null,containerNode,false);
    }
}
