package org.opendaylight.unimgr.mef.notification.utils;

import com.google.common.base.Optional;
import org.opendaylight.unimgr.mef.notification.model.types.BiNotification;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.EventSourceNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.EventSourceNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.SourceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

/**
 * Class that facilitate creating @see org.opendaylight.unimgr.mef.notification.model.types.BiNotification
 */
public class NotificationCreator {

    private static final YangInstanceIdentifier.NodeIdentifier PAYLOAD_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "payload").intern());
    private static final String XMLNS_ATTRIBUTE_KEY = "xmlns";
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_NOTIFICATION_ARG = new YangInstanceIdentifier.NodeIdentifier(TopicNotification.QNAME);
    private static final YangInstanceIdentifier.NodeIdentifier EVENT_SOURCE_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "node-id").intern());
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_ID_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "topic-id").intern());
    private static final YangInstanceIdentifier.NodeIdentifier YANG_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "yang-name").intern());
    private static final YangInstanceIdentifier.NodeIdentifier CLASS_NAME_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "class-name").intern());

    public BiNotification createNotification(String eventSourceIdent, String topicId, String message, NormalizedNode<?,?> normalizedNode, YangInstanceIdentifier yangInstanceIdentifier, String className){
        if(message==null){
            return createNotification(normalizedNode, eventSourceIdent, topicId, yangInstanceIdentifier, className);
        } else {
            return createNotification(message, eventSourceIdent, topicId);
        }
    }

    /**
     * Method encapsulate String message into BiNotification.
     *
     * @param message String message
     * @param eventSourceIdent Event source ID
     * @param topicId Topic ID
     * @return BiNotification
     */
    private BiNotification createNotification(String message, String eventSourceIdent, String topicId){
        EventSourceNotificationBuilder builder = new EventSourceNotificationBuilder();
        builder.setMessage(message);
        builder.setSourceId(new SourceIdentifier(eventSourceIdent));
        EventSourceNotification notification = builder.build();

        DataContainerChild<?, ?> dataContainerChild = encapsulate(notification);
        BiNotification topicNotification = prepareNotification(dataContainerChild,topicId,eventSourceIdent,null,null);
        return topicNotification;
    }

    /**
     * Method encapsulate BI object into BiNotification.
     *
     * @param normalizedNode BI object
     * @param eventSourceIdent Event source ID
     * @param topicId Topic ID
     * @return BiNotification
     */
    private BiNotification createNotification(NormalizedNode<?,?> normalizedNode, String eventSourceIdent, String topicId, YangInstanceIdentifier yangInstanceIdentifier, String className){
        BiNotification topicNotification = prepareNotification(normalizedNode,topicId,eventSourceIdent, yangInstanceIdentifier, className);
        return topicNotification;
    }

    private BiNotification prepareNotification(NormalizedNode<?, ?> normalizedNode, String topicId, String eventSourceIdent, YangInstanceIdentifier yangInstanceIdentifier, String className){
        DataContainerNodeAttrBuilder dataContainerNodeAttrBuilder = Builders.containerBuilder()
                .withNodeIdentifier(TOPIC_NOTIFICATION_ARG)
                .withChild(ImmutableNodes.leafNode(TOPIC_ID_ARG, new TopicId(topicId)))
                .withChild(ImmutableNodes.leafNode(EVENT_SOURCE_ARG, eventSourceIdent))
                .withChild(ImmutableNodes.leafNode(PAYLOAD_ARG, normalizedNode));
        if(yangInstanceIdentifier!=null && className!=null){
            dataContainerNodeAttrBuilder
                    .withChild(ImmutableNodes.leafNode(CLASS_NAME_ARG, className))
                    .withChild(ImmutableNodes.leafNode(YANG_ARG, yangInstanceIdentifier));

        }
        final ContainerNode topicNotification =
                (ContainerNode) dataContainerNodeAttrBuilder.build();
        return new BiNotification(topicNotification);
    }

    private AnyXmlNode encapsulate(EventSourceNotification notification){
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;

        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Can not create XML DocumentBuilder");
        }

        Document doc = docBuilder.newDocument();
        final Optional<String> namespace = Optional.of(PAYLOAD_ARG.getNodeType().getNamespace().toString());
        final Element rootElement = createElement(doc , "payload", namespace);

        final Element notifElement = doc.createElement("EventSourceNotification");
        rootElement.appendChild(notifElement);

        final Element sourceElement = doc.createElement("Source");
        sourceElement.appendChild(doc.createTextNode(notification.getSourceId().getValue()));
        notifElement.appendChild(sourceElement);

        final Element messageElement = doc.createElement("Message");
        messageElement.appendChild(doc.createTextNode(notification.getMessage()));
        notifElement.appendChild(messageElement);

        return Builders.anyXmlBuilder().withNodeIdentifier(PAYLOAD_ARG)
                .withValue(new DOMSource(rootElement))
                .build();
    }

    // Helper to create root XML element with correct namespace and attribute
    private Element createElement(final Document document, final String qName, final Optional<String> namespaceURI) {
        if(namespaceURI.isPresent()) {
            final Element element = document.createElementNS(namespaceURI.get(), qName);
            String name = XMLNS_ATTRIBUTE_KEY;
            if(element.getPrefix() != null) {
                name += ":" + element.getPrefix();
            }
            element.setAttributeNS(XMLNS_URI, name, namespaceURI.get());
            return element;
        }
        return document.createElement(qName);
    }

    public static YangInstanceIdentifier.NodeIdentifier getTopicNotificationArg() {
        return TOPIC_NOTIFICATION_ARG;
    }
}
