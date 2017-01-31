package org.opendaylight.unimgr.mef.notification.model.eventsource;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.message.TopicDOMNotification;
import org.opendaylight.unimgr.mef.notification.utils.Util;
import org.opendaylight.unimgr.mef.notification.message.NotificationCreator;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Class that should be used by Publisher to create EventSource. Class have methods that unable producer
 * to add and delete notifications on which client will subscribe and send messages to subscriber(s).
 */
public class EventSourceWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(EventSourceWrapper.class);
    private EventSourceImpl eventSourceImpl;
    private Notifications notifications = new Notifications();
    private final DOMNotificationPublishService domPublish;
    private NotificationCreator notificationCreator;

    /**
     * @param nodeId Id of EventSource.
     * @param eventSourceRegistry Argument used to register EventSource.
     * @param domBroker Broker used to send messages.
     */
    public EventSourceWrapper(NodeId nodeId, EventSourceRegistry eventSourceRegistry, Broker domBroker){
        LOG.info("Wrapper started");
        EventSourceBIProvider providerBI = new EventSourceBIProvider();
        Broker.ProviderSession domCtx = domBroker.registerProvider(providerBI);
        //final BindingAwareTest bindingAwareTest = new BindingAwareTest();
        //Broker.ProviderSession domCtx = domBroker.registerProvider(domCtx);
        domPublish = domCtx.getService(DOMNotificationPublishService.class);

        String nodeName = nodeId.getValue();
        Node node = Util.getNewNode(nodeName);
        eventSourceImpl = new EventSourceImpl(node);
        eventSourceRegistry.registerEventSource(eventSourceImpl);

        notificationCreator = new NotificationCreator();
        LOG.info("Wrapper finished!");
    }

    public void add(NotificationType notificationType){
        eventSourceImpl.addSchemaPatch(notificationType.getSchemaPath());
        notifications.add(notificationType);
        LOG.info("SchemaPath added. List size: {}",eventSourceImpl.getAvailableNotifications().size());
    }

    public void del(NotificationType notificationType){
        eventSourceImpl.delSchemaPatch(notificationType.getSchemaPath());
        notifications.del(notificationType);
    }

    public Notifications getNotifications(){
        return notifications;
    }

    /**
     * Method use to send BA object.
     * @param offer If set to true, message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#offerNotification(DOMNotification)}
     *              If it is false, method {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#putNotification(DOMNotification)} is used
     */
    public void putMsg(NotificationType notificationType,DataContainer dataContainer, InstanceIdentifier instanceIdentifier, boolean offer){
        putMessage(notificationType,dataContainer,instanceIdentifier,null,null,offer);
    }

    /**
     * Method use to send BI object.
     * @param offer If set to true, message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#offerNotification(DOMNotification)}
     *              If it is false, method {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#putNotification(DOMNotification)} is used
     */
    public void putMsg(NotificationType notificationType, ContainerNode containerNode, boolean offer){
        putMessage(notificationType,null,null,null,containerNode,offer);
    }

    /**
     * Method use to send String object.
     * @param offer If set to true, message is sent via {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#offerNotification(DOMNotification)}
     *              If it is false, method {@link org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService#putNotification(DOMNotification)} is used

     */
    public void putMsg(NotificationType notificationType, String message, boolean offer){
        putMessage(notificationType,null,null,message,null,offer);
    }

    public EventSource getEventSource(){
        return eventSourceImpl;
    }

    private void putMessage(NotificationType notificationType,DataContainer dataContainer, InstanceIdentifier instanceIdentifier, String message, ContainerNode containerNode, boolean offer){
        Map<TopicId,List<SchemaPath>> mapAcceptedTopics = eventSourceImpl.getMapAcceptedTopics();

        LOG.info("topicmapsize: {}",mapAcceptedTopics.size());
        for(Map.Entry<TopicId,List<SchemaPath>> topic: mapAcceptedTopics.entrySet()){
            LOG.info("Loop start - topicId: {} || schemapaths: {}",topic.getKey(), topic.getValue());
            //check if notification is subscribed
            if(topic.getValue().contains(notificationType.getSchemaPath())){
                sendMessage(topic.getKey().getValue(),dataContainer, instanceIdentifier,message, containerNode,offer);
            }
        }
    }

    private void sendMessage(String topicId,DataContainer dataContainer, InstanceIdentifier instanceIdentifier, String message, ContainerNode containerNode, boolean offer){
        String eventSourceIndent = eventSourceImpl.getSourceNodeKey().getNodeId().getValue();
        TopicDOMNotification topicNotification;
        if(message!=null){
            topicNotification = notificationCreator.createNotification(message,eventSourceIndent,topicId);
        } else if(containerNode!=null){
            topicNotification = notificationCreator.createNotification(containerNode,eventSourceIndent,topicId);
        } else if(dataContainer!=null && instanceIdentifier!=null){
            topicNotification = notificationCreator.createNotification(dataContainer,instanceIdentifier,eventSourceIndent,topicId);
        } else {
            LOG.warn("Payload of the message can not be null.");
            return ;
        }

        try {
            if(offer){
                domPublish.offerNotification(topicNotification);
            } else {
                domPublish.putNotification(topicNotification);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
