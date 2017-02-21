package org.opendaylight.unimgr.mef.notification.listener;

import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;

/**
 * Binding Aware Notification Listener interface.
 */
public interface BaNotificationListener extends NotificationListener, AutoCloseable {
    void onNotification(Notification notification);
}
