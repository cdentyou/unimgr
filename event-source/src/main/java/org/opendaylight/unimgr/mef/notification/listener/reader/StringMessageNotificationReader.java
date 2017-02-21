package org.opendaylight.unimgr.mef.notification.listener.reader;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * The easiest (Test) implementation of DomNotificationReader that expect String message.
 */
public class StringMessageNotificationReader implements DomNotificationReader {
    private static final Logger LOG = LoggerFactory.getLogger(StringMessageNotificationReader.class);
    private final List<String> receivedMessages = new LinkedList<>();

    @Override
    public void read(DOMNotification notification) {
        String payload = parsePayLoad(notification);
        receivedMessages.add(payload);
        LOG.trace("String message payload: {}",payload);
    }

    private String parsePayLoad(DOMNotification notification){

        final AnyXmlNode encapData = (AnyXmlNode) notification.getBody().getChild(PAYLOAD_ARG).get().getValue();
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final Transformer transformer = tf.newTransformer();
            transformer.transform(encapData.getValue(), result);
        } catch (TransformerException e) {
            LOG.error("Can not parse PayLoad data", e);
            return null;
        }
        writer.flush();
        return writer.toString();
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }
}
