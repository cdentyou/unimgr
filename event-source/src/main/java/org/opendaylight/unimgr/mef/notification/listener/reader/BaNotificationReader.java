package org.opendaylight.unimgr.mef.notification.listener.reader;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by root on 16.02.17.
 */
public class BaNotificationReader implements DomNotificationReader {
    private static final Logger LOG = LoggerFactory.getLogger(BaNotificationReader.class);

    @Override
    public void read(DOMNotification notification) {
        LOG.info("Notification: {}",notification.getBody().getValue().toString());
    }
}
