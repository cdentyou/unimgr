package org.opendaylight.unimgr.mef.notification.message;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.List;

/**
 * Interface made for @see org.opendaylight.unimgr.mef.notification.message.MessageGenerator to be able to update messages send to topics.
 *
 * @author marek.ryznar@amartus.com
 */
public interface EventSourceMessenger {
    /**
     * Create message including information from all notifications from current event source.
     *
     * @return message
     */
    String getMessage();
    /**
     * Message will include information about given notifications.
     *
     * @param schemaPaths notifications
     * @return message
     */
    String getMessage(List<SchemaPath> schemaPaths);
}
