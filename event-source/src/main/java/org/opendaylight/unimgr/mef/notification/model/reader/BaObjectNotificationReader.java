package org.opendaylight.unimgr.mef.notification.model.reader;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.unimgr.mef.notification.message.NotificationCodec;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

import java.util.LinkedList;
import java.util.List;

/**
 * The easiest (Test) implementation of DomNotificationReader that expect Binding Aware objects.
 */
public class BaObjectNotificationReader implements DomNotificationReader {
    private List<DataContainer> baObjects = new LinkedList<>();
    private NotificationCodec notificationCodec;

    public BaObjectNotificationReader(){
        notificationCodec = NotificationCodec.getInstance();
    }

    @Override
    public void read(DOMNotification notification) {
        DataContainer dataContainer = parsePayload(notification);
        if(dataContainer!=null){
            baObjects.add(dataContainer);
        }
    }

    private DataContainer parsePayload(DOMNotification notification){
        ContainerNode body = notification.getBody();
        return notificationCodec.fromNotification(body);
    }

    public List<DataContainer> getBaObjects() {
        return baObjects;
    }
}
