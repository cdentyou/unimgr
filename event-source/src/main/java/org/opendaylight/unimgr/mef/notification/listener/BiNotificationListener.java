package org.opendaylight.unimgr.mef.notification.listener;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationListener;

/**
 * Binding Independent Notification Listener interface.
 */
public interface BiNotificationListener extends DOMNotificationListener, AutoCloseable {
}
