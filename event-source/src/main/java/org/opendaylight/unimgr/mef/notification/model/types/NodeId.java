package org.opendaylight.unimgr.mef.notification.model.types;

/**
 * Represents id of EventSource.
 */
public class NodeId {
    private final String value;

    public NodeId(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
