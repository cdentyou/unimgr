package org.opendaylight.unimgr.mef.notification.model.reader;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Interface used for read DomNotification messages. The Subscriber defines
 * how the payload will be read by implementing this interface.
 */
public interface DomNotificationReader {
    YangInstanceIdentifier.NodeIdentifier PAYLOAD_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "payload"));
    void read(DOMNotification notification);
}
