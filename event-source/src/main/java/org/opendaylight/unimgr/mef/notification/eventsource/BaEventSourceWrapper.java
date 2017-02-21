package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.unimgr.mef.notification.model.message.BaNotification;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Class that should be used by Publisher to create EventSource. Class have methods that allows producer
 * to add and delete notifications on which client will subscribe and send Binding Aware objects to subscriber(s).
 */
public class BaEventSourceWrapper extends AbstractEventSourceWrapper{
    private static final Logger LOG = LoggerFactory.getLogger(BaEventSourceWrapper.class);
    private NotificationPublishService notificationPublishService = null;

    public BaEventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry, BindingAwareBroker bindingAwareBroker){
        super(nodeId,eventSourceRegistry);
        LOG.info("Binding Aware Wrapper started");
        BaEventSourceProvider providerBA = new BaEventSourceProvider();
        BindingAwareBroker.ProviderContext context = bindingAwareBroker.registerProvider(providerBA);
        notificationPublishService = context.getSALService(NotificationPublishService.class);
    }

    /**
     * Method send BA object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.binding.api.NotificationPublishService#putNotification(Notification)}.
     */
    public void putNotification(NotificationType notificationType, DataObject payload){
        createAndSendNotification(notificationType,payload,true);
    }

    /**
     * Method send BA object to subscribers.
     * Message is sent via {@link org.opendaylight.controller.md.sal.binding.api.NotificationPublishService#offerNotification(Notification)}.
     */
    public void offerNotification(NotificationType notificationType, DataObject payload){
        createAndSendNotification(notificationType,payload,false);
    }

    private void createAndSendNotification(NotificationType notificationType, DataObject payload, boolean put){
        Map<TopicId,List<SchemaPath>> mapAcceptedTopics = getEventSourceImpl().getMapAcceptedTopics();

        LOG.info("topicmapsize: {}",mapAcceptedTopics.size());
        for(Map.Entry<TopicId,List<SchemaPath>> topic: mapAcceptedTopics.entrySet()){
            LOG.info("Loop start - topicId: {} || schemapaths: {}",topic.getKey(), topic.getValue());
            //check if notification is subscribed
            if(topic.getValue().contains(notificationType.getSchemaPath())){
                BaNotification baNotification = new BaNotification(payload,topic.getKey().getValue());
                sendNotification(baNotification,put);
            }
        }
    }

    private void sendNotification(Notification notification, boolean put){
        if(put){
            try {
                notificationPublishService.putNotification(notification);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        } else {
            notificationPublishService.offerNotification(notification);
        }
    }
}
