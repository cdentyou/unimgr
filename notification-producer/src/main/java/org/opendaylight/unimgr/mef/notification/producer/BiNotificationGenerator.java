package org.opendaylight.unimgr.mef.notification.producer;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.eventsource.BiEventSourceWrapper;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

public class BiNotificationGenerator implements Runnable {

    private BiEventSourceWrapper biEventSourceWrapper;
    private NotificationType notificationType;
    private DataContainerChild<?,?> payload = null;
    private String stringPayload = null;

    public BiNotificationGenerator(String eventSourceName, EventSourceRegistry eventSourceRegistry, Broker domBroker, DataContainerChild<?,?> payload, String stringPayload, String notificationPattern){
        this.payload = payload;
        this.stringPayload = stringPayload;
        NodeId nodeId = new NodeId(eventSourceName);
        biEventSourceWrapper = new BiEventSourceWrapper(nodeId, eventSourceRegistry, domBroker);
        notificationType = new NotificationType(notificationPattern);
        biEventSourceWrapper.add(notificationType);
    }

    @Override
    public void run() {
        if(payload==null){
            biEventSourceWrapper.putNotification(notificationType,stringPayload);
        } else {
            biEventSourceWrapper.putNotification(notificationType,payload);
        }
    }
}
