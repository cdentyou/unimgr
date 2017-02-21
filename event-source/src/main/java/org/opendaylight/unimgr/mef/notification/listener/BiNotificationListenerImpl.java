package org.opendaylight.unimgr.mef.notification.listener;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.unimgr.mef.notification.listener.reader.DomNotificationReader;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.topic.rev150408.TopicReadService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Created by root on 15.02.17.
 */
public class BiNotificationListenerImpl extends AbstractTopicReadService implements BiNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(BiNotificationListenerImpl.class);

    private static final YangInstanceIdentifier.NodeIdentifier EVENT_SOURCE_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "node-id"));
    private static final YangInstanceIdentifier.NodeIdentifier PAYLOAD_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "payload"));
    private static final YangInstanceIdentifier.NodeIdentifier TOPIC_ID_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "topic-id"));

    private ListenerRegistration<BiNotificationListenerImpl> listenerReg;
    private DomNotificationReader domNotificationReader;

    /**
     * @param eventAggregatorService Argument needed for creating topics in ODL.
     * @param notifyService Service needed to register current listener in ODL.
     * @param rpcRegistry Argument needed to add implementation to listener interface.
     * @param domNotificationReader Reader of the DomNotification. It could be different depending on what type of objects the client expects.
     */
    public BiNotificationListenerImpl(EventAggregatorService eventAggregatorService, RpcProviderRegistry rpcRegistry, DOMNotificationService notifyService, DomNotificationReader domNotificationReader){
        super(eventAggregatorService);
        listenerReg = notifyService.registerNotificationListener(this, SchemaPath.create(true, TopicNotification.QNAME));
        this.domNotificationReader = domNotificationReader;
        rpcRegistry.addRpcImplementation(TopicReadService.class,this);
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
        if(domNotification==null){
            LOG.warn("Payload is null");
            return;
        }
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
            // then notification is parsed and processed as reader defines
            if(getRegisteredTopic().keySet().contains(topicId.getValue())){
                if(body.getChild(PAYLOAD_ARG).isPresent()){
                    domNotificationReader.read(domNotification);
                }
            }
        }
    }
}
