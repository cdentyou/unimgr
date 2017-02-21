package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.unimgr.mef.notification.utils.Util;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

/**
 * Abstract class that define common part of every EventSourceWrapper.
 */
public class AbstractEventSourceWrapper {
    private final EventSourceImpl eventSourceImpl;
    private Notifications notifications = new Notifications();


    public AbstractEventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry){
        String nodeName = nodeId.getValue();
        Node node = Util.getNewNode(nodeName);
        eventSourceImpl = new EventSourceImpl(node);
        eventSourceRegistry.registerEventSource(eventSourceImpl);
    }

    public void add(NotificationType notificationType){
        eventSourceImpl.addSchemaPatch(notificationType.getSchemaPath());
        notifications.add(notificationType);
    }

    public void del(NotificationType notificationType){
        eventSourceImpl.delSchemaPatch(notificationType.getSchemaPath());
        notifications.del(notificationType);
    }

    public Notifications getNotifications(){
        return notifications;
    }

    public EventSourceImpl getEventSourceImpl() {
        return eventSourceImpl;
    }
}
