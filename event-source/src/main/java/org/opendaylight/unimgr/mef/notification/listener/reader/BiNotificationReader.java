package org.opendaylight.unimgr.mef.notification.listener.reader;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

import java.util.LinkedList;
import java.util.List;

/**
 * The easiest (Test) implementation of DomNotificationReader that expect Binding Independent objects.
 */
public class BiNotificationReader implements DomNotificationReader {

    private List<DataContainerChild> biObjects = new LinkedList<>();

    @Override
    public void read(DOMNotification notification) {
        DataContainerChild dataContainerChild = parsePayload(notification);
        biObjects.add(dataContainerChild);
    }

    private DataContainerChild<?,?> parsePayload(DOMNotification notification){
        DataContainerChild<?, ?> dataContainerChild = (DataContainerChild<?, ?>) notification.getBody().getChild(PAYLOAD_ARG).get().getValue();
        return dataContainerChild;
    }

    public List<DataContainerChild> getBiObjects(){
        return biObjects;
    }
}