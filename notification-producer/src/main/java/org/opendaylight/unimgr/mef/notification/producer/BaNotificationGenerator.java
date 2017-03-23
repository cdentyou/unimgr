package org.opendaylight.unimgr.mef.notification.producer;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceWrapper;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by root on 22.02.17.
 */
public class BaNotificationGenerator implements Runnable {

    private BaEventSourceWrapper baEventSourceWrapper;
    private DataObject payload = null;
    private NotificationType notificationType;
    private InstanceIdentifier instanceIdentifier;

    public BaNotificationGenerator(String eventSourceName, EventSourceRegistry eventSourceRegistry, DataObject payload, String notificationPattern, Broker domBroker, InstanceIdentifier instanceIdentifier){
        this.payload = payload;
        this.instanceIdentifier = instanceIdentifier;

        NodeId nodeId = new NodeId(eventSourceName);
        baEventSourceWrapper = new BaEventSourceWrapper(nodeId,eventSourceRegistry,domBroker);
        notificationType = new NotificationType(notificationPattern);
        baEventSourceWrapper.add(notificationType);
    }

    @Override
    public void run() {
        baEventSourceWrapper.putNotification(notificationType,payload, instanceIdentifier);
    }

    public void setPayload(DataObject payload){
        this.payload = payload;
    }

}
