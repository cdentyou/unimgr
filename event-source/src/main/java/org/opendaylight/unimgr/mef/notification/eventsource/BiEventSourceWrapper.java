package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that should be used by Publisher to create EventSource. Class have methods that allows notification
 * to add and delete notifications on which client will subscribe and send Binding Independent objects to subscriber(s).
 */
public class BiEventSourceWrapper extends AbstractEventSourceWrapper{
    private static final Logger LOG = LoggerFactory.getLogger(BiEventSourceWrapper.class);

    /**
     * @param nodeId Id of EventSource.
     * @param eventSourceRegistry Argument used to register EventSource.
     * @param domBroker Broker used to send messages.
     */
    public BiEventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry, Broker domBroker){
        super(nodeId,eventSourceRegistry,domBroker);
    }

    /**
     * Method send BI object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#putNotification(DOMNotification)}.
     */
    public void putNotification(NotificationType notificationType, DataContainerChild<?,?> dataContainerChild){
        sendNotification(notificationType,null,dataContainerChild,true,null,null);
    }

    /**
     * Method send BI object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#offerNotification(DOMNotification)}.
     */
    public void offerNotification(NotificationType notificationType, DataContainerChild<?,?> dataContainerChild){
        sendNotification(notificationType,null,dataContainerChild,false,null,null);
    }

    /**
     * Method send String object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#putNotification(DOMNotification)}.
     */
    public void putNotification(NotificationType notificationType, String message){
        sendNotification(notificationType,message,null,true,null,null);
    }

    /**
     * Method send String object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#offerNotification(DOMNotification)}.
     */
    public void offerNotification(NotificationType notificationType, String message){
        sendNotification(notificationType,message,null,false,null,null);
    }
}
