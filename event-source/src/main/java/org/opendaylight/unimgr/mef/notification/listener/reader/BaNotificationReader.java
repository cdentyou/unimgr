package org.opendaylight.unimgr.mef.notification.listener.reader;

import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Created by root on 16.02.17.
 */
public interface BaNotificationReader {
    public void read(Notification notification);
}
