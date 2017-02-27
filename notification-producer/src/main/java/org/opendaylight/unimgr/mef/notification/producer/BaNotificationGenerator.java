package org.opendaylight.unimgr.mef.notification.producer;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Created by root on 22.02.17.
 */
public class BaNotificationGenerator implements Runnable {

    private BaEventSourceWrapper baEventSourceWrapper;
    private DataObject payload = null;
    private NotificationType notificationType;

    public BaNotificationGenerator(String eventSourceName,EventSourceRegistry eventSourceRegistry, BindingAwareBroker broker, DataObject payload, String notificationPattern){
        this.payload = payload;
        NodeId nodeId = new NodeId(eventSourceName);
        baEventSourceWrapper = new BaEventSourceWrapper(nodeId,eventSourceRegistry,broker);
        notificationType = new NotificationType(notificationPattern);
        baEventSourceWrapper.add(notificationType);
    }

    @Override
    public void run() {
        baEventSourceWrapper.offerNotification(notificationType,payload);
    }

    public void setPayload(DataObject payload){
        this.payload = payload;
    }

}
