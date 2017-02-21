package org.opendaylight.unimgr.mef.notification.model.message;

import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Created by root on 15.02.17.
 */
public class BaNotification implements Notification {

    private String topicId = null;
    private DataObject payload = null;

    public BaNotification(DataObject payload, String topicId){
        this.payload = payload;
        this.topicId = topicId;
    }

    @Override
    public Class<? extends DataContainer> getImplementedInterface() {
        return null;
    }

    public DataObject getPayload(){
        return payload;
    }

    public String getTopicId() {
        return topicId;
    }
}
