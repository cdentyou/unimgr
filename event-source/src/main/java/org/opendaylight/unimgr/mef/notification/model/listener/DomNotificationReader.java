package org.opendaylight.unimgr.mef.notification.model.listener;

import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 * Created by root on 27.01.17.
 */
public interface DomNotificationReader {
    void read(DataContainerChild<?, ?> dataContainerChild);
}
