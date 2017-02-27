package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.model.message.BiNotification;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.utils.NotificationCreator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Class that should be used by Publisher to create EventSource. Class have methods that allows notification
 * to add and delete notifications on which client will subscribe and send Binding Independent objects to subscriber(s).
 */
public class BiEventSourceWrapper  extends AbstractEventSourceWrapper{
    private static final Logger LOG = LoggerFactory.getLogger(BiEventSourceWrapper.class);
    private final DOMNotificationPublishService domPublish;
    private NotificationCreator notificationCreator;

    /**
     * @param nodeId Id of EventSource.
     * @param eventSourceRegistry Argument used to register EventSource.
     * @param domBroker Broker used to send messages.
     */
    public BiEventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry, Broker domBroker){
        super(nodeId,eventSourceRegistry);
        LOG.info("Binding Independent Wrapper started");
        BiEventSourceProvider providerBI = new BiEventSourceProvider();
        Broker.ProviderSession domCtx = domBroker.registerProvider(providerBI);
        domPublish = domCtx.getService(DOMNotificationPublishService.class);
        notificationCreator = new NotificationCreator();
    }

    /**
     * Method send BI object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#putNotification(DOMNotification)}.
     */
    public void putNotification(NotificationType notificationType, DataContainerChild<?,?> dataContainerChild){
        createAndSendNotification(notificationType,null,dataContainerChild,true);
    }

    /**
     * Method send BI object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#offerNotification(DOMNotification)}.
     */
    public void offerNotification(NotificationType notificationType, DataContainerChild<?,?> dataContainerChild){
        createAndSendNotification(notificationType,null,dataContainerChild,false);
    }

    /**
     * Method send String object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#putNotification(DOMNotification)}.
     */
    public void putNotification(NotificationType notificationType, String message){
        createAndSendNotification(notificationType,message,null,true);
    }

    /**
     * Method send String object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#offerNotification(DOMNotification)}.
     */
    public void offerNotification(NotificationType notificationType, String message){
        createAndSendNotification(notificationType,message,null,false);
    }

    private void createAndSendNotification(NotificationType notificationType, String message, DataContainerChild<?,?> dataContainerChild, boolean put){
        Map<TopicId,List<SchemaPath>> mapAcceptedTopics = getEventSourceImpl().getMapAcceptedTopics();

        LOG.info("mapAcceptedTopics.size: {}",mapAcceptedTopics.size());
        for(Map.Entry<TopicId,List<SchemaPath>> topic: mapAcceptedTopics.entrySet()){
            LOG.info("Loop start - topicId: {} || schemapaths: {}",topic.getKey(), topic.getValue());
            if(topic.getValue().contains(notificationType.getSchemaPath())){
                BiNotification biNotification = createNotification(message, dataContainerChild,topic.getKey().getValue());
                sendNotification(biNotification,put);
            }
        }
    }

    private BiNotification createNotification(String message, DataContainerChild<?,?> dataContainerChild, String topicId){
        String eventSourceIndent = getEventSourceImpl().getSourceNodeKey().getNodeId().getValue();
        BiNotification topicNotification;
        if(message!=null){
            topicNotification = notificationCreator.createNotification(message,eventSourceIndent,topicId);
        } else if(dataContainerChild!=null){
            topicNotification = notificationCreator.createNotification(dataContainerChild,eventSourceIndent,topicId);
        } else {
            LOG.warn("Payload of the message can not be null.");
            return null;
        }
        return  topicNotification;
    }

    private void sendNotification(BiNotification topicNotification, boolean put){
        try {
            if(put){
                domPublish.putNotification(topicNotification);
            } else {
                domPublish.offerNotification(topicNotification);
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }
}
