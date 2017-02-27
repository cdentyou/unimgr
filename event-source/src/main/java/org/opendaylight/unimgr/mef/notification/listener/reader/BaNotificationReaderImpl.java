package org.opendaylight.unimgr.mef.notification.listener.reader;

import org.opendaylight.unimgr.mef.notification.model.message.BaNotification;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by root on 16.02.17.
 */
public class BaNotificationReaderImpl<T extends DataObject> implements BaNotificationReader {
    private static final Logger LOG = LoggerFactory.getLogger(BaNotificationReaderImpl.class);
    private List<T> receivedNotifications = new LinkedList<T>();

    @Override
    public void read(Notification notification) {
        BaNotification baNotification = (BaNotification) notification;
        T object = (T) baNotification.getPayload();
        LOG.info("StringMessageNotificationReader.read(): {}",object);
        receivedNotifications.add(object);
    }

    public List<T> getReceivedNotifications() {
        return receivedNotifications;
    }
}
