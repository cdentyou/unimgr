package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.utils.NotificationCodec;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Class that should be used by Publisher to create EventSource. Class have methods that allows notification
 * to add and delete notifications on which client will subscribe and send Binding Aware objects to subscriber(s).
 */
public class BaEventSourceWrapper extends AbstractEventSourceWrapper{
    private NotificationCodec notificationCodec;

    public BaEventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry,Broker domBroker){
        super(nodeId,eventSourceRegistry,domBroker);
        notificationCodec = NotificationCodec.getInstance();
    }

    /**
     * Method send BA object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.binding.api.NotificationPublishService#putNotification(Notification)}.
     */
    public void putNotification(NotificationType notificationType, DataContainer payload, InstanceIdentifier instanceIdentifier){
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> bi = notificationCodec.toDataContainerChild(payload,instanceIdentifier);
        sendNotification(notificationType,null,bi.getValue(),true,bi.getKey(),payload.getClass().getName());
    }

    /**
     * Method send BA object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.binding.api.NotificationPublishService#offerNotification(Notification)}.
     */
    public void offerNotification(NotificationType notificationType, DataContainer payload, InstanceIdentifier instanceIdentifier){
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> bi = notificationCodec.toDataContainerChild(payload,instanceIdentifier);
        sendNotification(notificationType,null,bi.getValue(),false,bi.getKey(),payload.getClass().getName());
    }
}
