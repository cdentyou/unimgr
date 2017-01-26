package org.opendaylight.unimgr.mef.notification.message;

import com.google.common.base.Optional;
import javassist.ClassPool;
import org.opendaylight.unimgr.mef.notification.impl.TopicDOMNotification;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.EventSourceNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.EventSourceNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.api.rev150408.SourceIdentifier;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 17.01.17.
 */
public class NotificationCodec {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationCodec.class);
    private static final String XMLNS_ATTRIBUTE_KEY = "xmlns";
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final YangInstanceIdentifier.NodeIdentifier PAYLOAD_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "payload").intern());
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_NOTIFICATION_ARG = new YangInstanceIdentifier.NodeIdentifier(TopicNotification.QNAME);
    private static final YangInstanceIdentifier.NodeIdentifier EVENT_SOURCE_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "node-id").intern());
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_ID_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "topic-id").intern());

    private BindingNormalizedNodeCodecRegistry bindingNormalizedNodeCodecRegistry;
    private Set<Class<?>> moduleClasses;
    private Set<YangModuleInfo> moduleInfos;

    public static YangInstanceIdentifier.NodeIdentifier getTopicNotificationArg() {
        return TOPIC_NOTIFICATION_ARG;
    }

    public NotificationCodec(){
        moduleClasses = new HashSet<>();
        moduleInfos = new HashSet<>();

        initBindingNormalizedNodeCodecRegistry();
    }

    private void initBindingNormalizedNodeCodecRegistry(){
        JavassistUtils utils =
                JavassistUtils.forClassPool(ClassPool.getDefault());
        bindingNormalizedNodeCodecRegistry = new
                BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
    }

    public TopicDOMNotification createNotification(DataContainer dataContainer, String eventSourceIdent, String topicId, InstanceIdentifier instanceIdentifier){
        LOG.info("bindingNormalizedNodeCodecRegistry.toNormalizedNodeRpcData(dataContainer) - START");
        DataContainerChild<?, ?> dataContainerChild = toDataContainerChild(dataContainer,instanceIdentifier);
        LOG.info("bindingNormalizedNodeCodecRegistry.toNormalizedNodeRpcData(dataContainer) - STOP");
        final ContainerNode topicNotification = Builders.containerBuilder()
                .withNodeIdentifier(TOPIC_NOTIFICATION_ARG)
                .withChild(ImmutableNodes.leafNode(TOPIC_ID_ARG, new TopicId(topicId)))
                .withChild(ImmutableNodes.leafNode(EVENT_SOURCE_ARG, eventSourceIdent))
                .withChild(dataContainerChild)
                .build();


        return new TopicDOMNotification(topicNotification);
    }

    private DataContainerChild<?, ?> toDataContainerChild(DataContainer dataContainer, InstanceIdentifier instanceIdentifier){
        updateCodec(dataContainer);

        InstanceIdentifier<DataObject> ii = instanceIdentifier;
        DataObject dataObject = (DataObject) dataContainer;

        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = bindingNormalizedNodeCodecRegistry.toNormalizedNode(ii,dataObject);
        NormalizedNode<?, ?> normalizedNode = entry.getValue();

        DataContainerChild<?, ?> dataContainerChild = new DataContainerChild<YangInstanceIdentifier.PathArgument, Object>() {
            @Override
            public QName getNodeType() {
                return normalizedNode.getNodeType();
            }

            @Override
            public YangInstanceIdentifier.PathArgument getIdentifier() {
                return entry.getKey().getLastPathArgument();
            }

            @Override
            public Object getValue() {
                return normalizedNode.getValue();
            }
        };

        return dataContainerChild;
    }

    private void updateCodec(DataContainer dataContainer){
        Class<?> cls = dataContainer.getClass();

        if(!moduleClasses.contains(cls)){
            moduleClasses.add(cls);
            addModuleInfo(cls);
        }
    }

    private void addModuleInfo(Class<?> cls){
        YangModuleInfo moduleInfo = getModuleInfo(cls);
        if(!moduleInfos.contains(moduleInfo)){
            final ModuleInfoBackedContext moduleContext = ModuleInfoBackedContext.create();
            moduleInfos.add(moduleInfo);
            moduleContext.addModuleInfos(moduleInfos);
            SchemaContext context =  moduleContext.tryToCreateSchemaContext().get();
            BindingRuntimeContext bindingContext = BindingRuntimeContext.create(moduleContext, context);

            bindingNormalizedNodeCodecRegistry.onBindingRuntimeContextUpdated(bindingContext);
        }
    }

    private YangModuleInfo getModuleInfo(Class<?> cls){
        YangModuleInfo moduleInfo = null;
        try {
            moduleInfo = BindingReflections.getModuleInfo(cls);
        } catch (Exception e) {
            LOG.warn("Cannot find module info");
        }
        return moduleInfo;
    }


    public TopicDOMNotification createNotification(String message, String eventSourceIdent){
        EventSourceNotificationBuilder builder = new EventSourceNotificationBuilder();
        builder.setMessage(message);
        builder.setSourceId(new SourceIdentifier(eventSourceIdent));
        EventSourceNotification notification = builder.build();

        TopicDOMNotification topicNotification = createNotification(notification,eventSourceIdent);
        return topicNotification;
    }

    private TopicDOMNotification createNotification(EventSourceNotification notification, String eventSourceIdent){

        final ContainerNode topicNotification = Builders.containerBuilder()
                .withNodeIdentifier(TOPIC_NOTIFICATION_ARG)
                .withChild(ImmutableNodes.leafNode(EVENT_SOURCE_ARG, eventSourceIdent))
                .withChild(encapsulate(notification))
                .build();

        return new TopicDOMNotification(topicNotification);

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

}
