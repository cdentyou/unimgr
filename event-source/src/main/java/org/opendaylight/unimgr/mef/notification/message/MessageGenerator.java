package org.opendaylight.unimgr.mef.notification.message;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.unimgr.mef.notification.impl.TopicDOMNotification;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.EventSourceNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.EventSourceNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.SourceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import java.util.*;

/**
 * This class is responsible to generate messages in given interval and publish notification if an topic has been joined
 * Text of message is composed by messageText taken from event source and time. Time is added to simulate
 * changes of message content.
 *
 * @author marek.ryznar@amartus.com
 */
public class MessageGenerator implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MessageGenerator.class);
    public static final String XMLNS_ATTRIBUTE_KEY = "xmlns";
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_NOTIFICATION_ARG = new YangInstanceIdentifier.NodeIdentifier(TopicNotification.QNAME);
    private static final YangInstanceIdentifier.NodeIdentifier EVENT_SOURCE_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "node-id").intern());
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_ID_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "topic-id").intern());
    private static final YangInstanceIdentifier.NodeIdentifier PAYLOAD_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "payload").intern());

    private final String eventSourceIdent;
    private final List<TopicId> listAcceptedTopics = new ArrayList<>();
    private final Map<TopicId,List<SchemaPath>> mapAcceptedTopics = new HashMap<>();
    private final DOMNotificationPublishService domPublish;
    private final EventSourceMessenger eventSourceMessenger;
    private String messageText;

    public MessageGenerator(String EventSourceIdent, String messageText, DOMNotificationPublishService domPublish, EventSourceMessenger eventSourceMessenger) {
        this.eventSourceIdent = EventSourceIdent;
        this.domPublish = domPublish;
        this.eventSourceMessenger = eventSourceMessenger;
        this.messageText = messageText;
        LOG.info("MessageGenerator constructor with values: {} {}",messageText, eventSourceIdent);
    }

    /*
     * Method is run periodically (see method startMessageGenerator in EventSource classes)
     * Create messages and publish notification
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        LOG.info("MessageGenerator({}).run acceptedTopics: {} ",eventSourceIdent,mapAcceptedTopics.entrySet());
        //LOG.info("Message: {}",messageText);
        for(Map.Entry<TopicId,List<SchemaPath>> topic: mapAcceptedTopics.entrySet()){
            TopicId joinTopic = topic.getKey();
            // notification is published for each accepted topic
            // if there is no accepted topic, no notification will publish

            //Get message text from event source
            String message = eventSourceMessenger.getMessage(topic.getValue()) + " [" + Calendar.getInstance().getTime().toString() +"]";
            LOG.info("Message generated: {}",message);

            EventSourceNotificationBuilder builder = new EventSourceNotificationBuilder();
            builder.setMessage(message);
            builder.setSourceId(new SourceIdentifier(this.eventSourceIdent));
            EventSourceNotification notification = builder.build();

            final String topicId = joinTopic.getValue();

            // notification is encapsulated into TopicDOMNotification and publish via DOMNotificationPublisherService
            TopicDOMNotification topicNotification = createNotification(notification,this.eventSourceIdent,topicId);

            ListenableFuture<? extends Object> notifFuture;
            try {
                notifFuture = domPublish.putNotification(topicNotification);
                Futures.addCallback(notifFuture, new FutureCallback<Object>(){

                    @Override
                    public void onSuccess(Object result) {
                        LOG.info("Message published for topic [TopicId: {}]",topicId);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.error("Message has not published for topic [TopicId: {}], Exception: {}",topicId,t);
                    }
                });
            } catch (InterruptedException e) {
                LOG.error("Message has not published for topic [TopicId: {}], Exception: {}",topicId,e);
            }
        }
    }

    /*
     * Method encapsulates EventSourceNotification into TopicDOMNotification
     * TopicDOMNotification carries next information
     *   - TopicId
     *   - identifier of event source
     *   - EventSourceNotification encapsulated into XML form (see AnyXmlNode encapsulate(...))
     */
    private TopicDOMNotification createNotification(EventSourceNotification notification, String eventSourceIdent, String topicId){

        final ContainerNode topicNotification = Builders.containerBuilder()
                .withNodeIdentifier(TOPIC_NOTIFICATION_ARG)
                .withChild(ImmutableNodes.leafNode(TOPIC_ID_ARG, new TopicId(topicId)))
                .withChild(ImmutableNodes.leafNode(EVENT_SOURCE_ARG, eventSourceIdent))
                .withChild(encapsulate(notification))
                .build();
        return new TopicDOMNotification(topicNotification);

    }

    /*
     * Result of this method is encapsulated SampleEventSourceNotification into AnyXMLNode
     * SampleEventSourceNotification is XML fragment in output
     */
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

    public void addTopic(TopicId topicId, List<SchemaPath> schemaPaths) {
        mapAcceptedTopics.put(topicId,schemaPaths);
    }

    public void removeTopic(TopicId topicId){
        mapAcceptedTopics.remove(topicId);
    }

}