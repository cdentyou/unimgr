package org.opendaylight.unimgr.mef.notification.model.types;

import java.util.LinkedList;
import java.util.List;

/**
 * Container for NotificationType.
 */
public class Notifications {
    private final List<NotificationType> notifications = new LinkedList<>();

    public void add(NotificationType notificationType){
        notifications.add(notificationType);
    }

    public void del(NotificationType notificationType){
        notifications.remove(notificationType);
    }

    public List<NotificationType> getNotifications(){
        return notifications;
    }
}
