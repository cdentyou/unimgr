package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.model.types.BiNotification;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.unimgr.mef.notification.utils.NotificationCreator;
import org.opendaylight.unimgr.mef.notification.utils.Util;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Abstract class that define common part of every EventSourceWrapper.
 */
public class AbstractEventSourceWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventSourceWrapper.class);
    private final EventSourceImpl eventSourceImpl;
    private final DOMNotificationPublishService domPublish;
    protected final String eventSourceIndent;
    private Notifications notifications = new Notifications();
    private NotificationCreator notificationCreator;

    public AbstractEventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry, Broker domBroker){
        BiEventSourceProvider providerBI = new BiEventSourceProvider();
        Broker.ProviderSession domCtx = domBroker.registerProvider(providerBI);
        domPublish = domCtx.getService(DOMNotificationPublishService.class);

        eventSourceIndent = nodeId.getValue();
        Node node = Util.getNewNode(eventSourceIndent);
        eventSourceImpl = new EventSourceImpl(node);
        eventSourceRegistry.registerEventSource(eventSourceImpl);
        notificationCreator = new NotificationCreator();
    }

    public void add(NotificationType notificationType){
        eventSourceImpl.addSchemaPatch(notificationType.getSchemaPath());
        notifications.add(notificationType);
    }

    public void del(NotificationType notificationType){
        eventSourceImpl.delSchemaPatch(notificationType.getSchemaPath());
        notifications.del(notificationType);
    }

    protected void sendNotification(NotificationType notificationType, String message, DataContainerChild dataContainerChild, boolean put){
        Map<TopicId,List<SchemaPath>> mapAcceptedTopics = getEventSourceImpl().getMapAcceptedTopics();

        LOG.info("mapAcceptedTopics.size: {}",mapAcceptedTopics.size());
        for(Map.Entry<TopicId,List<SchemaPath>> topic: mapAcceptedTopics.entrySet()){
            LOG.info("Loop start - topicId: {} || schemapaths: {}",topic.getKey(), topic.getValue());
            if(topic.getValue().contains(notificationType.getSchemaPath())){
                BiNotification biNotification = notificationCreator.createNotification(eventSourceIndent,topic.getKey().getValue(),message,dataContainerChild);
                sendNotification(biNotification,put);
            }
        }
    }

    private void sendNotification(BiNotification topicNotification, boolean put){
        try {
            if(put){
                LOG.info("putNotification: {}",topicNotification.toString());
                domPublish.putNotification(topicNotification);
            } else {
                LOG.info("offerNotification: {}",topicNotification.toString());
                domPublish.offerNotification(topicNotification);
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
        LOG.info("Notification has been sent.");
    }

    public Notifications getNotifications(){
        return notifications;
    }

    public EventSourceImpl getEventSourceImpl() {
        return eventSourceImpl;
    }
}
