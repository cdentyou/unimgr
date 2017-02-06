package org.opendaylight.unimgr.mef.notification.model.listener;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.unimgr.mef.notification.model.reader.DomNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.unimgr.mef.notification.topic.TopicHandler;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.user.agent.topic.rev150408.ReadTopicInput;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Implementation of EventSourceListener.
 */
public class EventSourceListenerImpl implements EventSourceListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventSourceListenerImpl.class);

    private static final YangInstanceIdentifier.NodeIdentifier EVENT_SOURCE_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "node-id"));
    private static final YangInstanceIdentifier.NodeIdentifier PAYLOAD_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "payload"));
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_ID_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "topic-id"));

    private final Map<String,Map.Entry<NodeId,Notifications>> registeredTopic = new HashMap<>();
    private final TopicHandler topicHandler;
    private ListenerRegistration<EventSourceListenerImpl> listenerReg;
    private DomNotificationReader domNotificationReader;

    /**
     * @param eventAggregatorService Argument needed for creating topics in ODL.
     * @param notifyService Service needed to register current listener in ODL.
     * @param rpcRegistry Argument needed to add implementation to listener interface.
     * @param domNotificationReader Reader of the DomNotification. It could be different depending on what type of objects the client expects.
     */
    public EventSourceListenerImpl(EventAggregatorService eventAggregatorService, DOMNotificationService notifyService, RpcProviderRegistry rpcRegistry, DomNotificationReader domNotificationReader){
        topicHandler = new TopicHandler(eventAggregatorService);
        listenerReg = notifyService.registerNotificationListener(this, SchemaPath.create(true, TopicNotification.QNAME));
        this.domNotificationReader = domNotificationReader;
        rpcRegistry.addRpcImplementation(EventSourceListener.class,this);
    }

    /**
     * @param nodeId when null is passed - everything will be matched
     * @param notifications when null is passed - everything will be matched
     * @return topicId - its null if it is already registered
     */
    @Override
    public String readTopic(NodeId nodeId, Notifications notifications) {
        Map.Entry<NodeId,Notifications> topicEntry = new AbstractMap.SimpleEntry<NodeId,Notifications>(nodeId, notifications);
        String topicId = null;
        if(!registeredTopic.values().contains(topicEntry)){
            topicId = topicHandler.createTopic(nodeId,notifications);
            registeredTopic.put(topicId,topicEntry);
        }
        return topicId;
    }

    @Override
    public void close() throws Exception {
        listenerReg.close();
    }

    /**
     * Method is called when producer sends messages.
     * If nodeId and topicId are not null and topicId is registered in current listener, the message is being read with use of given reader.
     *
     * @param domNotification Incoming message.
     */
    @Override
    public void onNotification(@Nonnull DOMNotification domNotification) {
        String nodeName = null;
        TopicId topicId = null;
        ContainerNode body = domNotification.getBody();
        // get the nodeName (identifier of event source) from notification
        if(body.getChild(EVENT_SOURCE_ARG).isPresent()){
            nodeName = body.getChild(EVENT_SOURCE_ARG).get().getValue().toString();
        }
        // get the TopicId from notification
        if(body.getChild(TOPIC_ID_ARG).isPresent()){;
            topicId = (TopicId) body.getChild(TOPIC_ID_ARG).get().getValue();
        }
        if( nodeName != null && topicId != null ){
            // if nodeName and TopicId are present and TopicId has been requested to process (TopicId is in registeredTopic)
            // then notification is parsed and written into the file.
            if(registeredTopic.keySet().contains(topicId.getValue())){
                if(body.getChild(PAYLOAD_ARG).isPresent()){
                    domNotificationReader.read(domNotification);
                }
            }
        }
    }

    @Override
    public Future<RpcResult<Void>> readTopic(ReadTopicInput input) {
        String topicId = input.getTopicId().getValue();
        // if requested TopicId has not been requested before then it is added into to register
        if(registeredTopic.keySet().contains(topicId) == false){
            registeredTopic.put(topicId,null);
            LOG.info("UserAgent start read notification with TopicId {}", topicId);
        }
        return immediateFuture(RpcResultBuilder.success((Void) null).build());
    }
}
