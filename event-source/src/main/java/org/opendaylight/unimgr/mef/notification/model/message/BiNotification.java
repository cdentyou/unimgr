package org.opendaylight.unimgr.mef.notification.model.message;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class BiNotification implements DOMNotification {

    private static final SchemaPath TOPIC_NOTIFICATION_ID = SchemaPath.create(true, TopicNotification.QNAME);
    private final ContainerNode body;

    public BiNotification(final ContainerNode body) {
        this.body = body;
    }

    @Override
    public SchemaPath getType() {
        return TOPIC_NOTIFICATION_ID;
    }

    @Override
    public ContainerNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "BiNotification [body=" + body + "]";
    }
}