package org.opendaylight.unimgr.mef.notification.model.types;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class BiNotification implements DOMNotification {
    private final ContainerNode body;
    private final SchemaPath type;

    public BiNotification(final ContainerNode body) {
        this.body = body;
        this.type = SchemaPath.create(true, TopicNotification.QNAME);
    }

    public BiNotification(final ContainerNode body, SchemaPath type){
        this.body = body;
        this.type = type;
    }

    @Override
    public SchemaPath getType() {
        return type;
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