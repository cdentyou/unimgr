package org.opendaylight.unimgr.mef.notification.model.listener;

import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 * Interface used for read DomNotification messages. In our implementation all incoming messages
 * contains DataContainerChild which represents payload of the message. The Subscriber defines
 * how the payload will be read by implementing this interface.
 */
public interface DomNotificationReader {
    void read(DataContainerChild<?, ?> dataContainerChild);
}
