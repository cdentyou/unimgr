package org.opendaylight.unimgr.mef.notification.model.eventsource;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.es.EventSourceBIProvider;
import org.opendaylight.unimgr.mef.notification.impl.TopicDOMNotification;
import org.opendaylight.unimgr.mef.notification.impl.Util;
import org.opendaylight.unimgr.mef.notification.message.NotificationCodec;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by root on 10.01.17.
 */
public class EventSourceWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(EventSourceWrapper.class);
    private final Broker domBroker;
    private EventSourceImpl eventSourceImpl;
    private final DOMNotificationPublishService domPublish;
    private NotificationCodec notificationCodec;


    public EventSourceWrapper(String nodeName, EventSourceRegistry eventSourceRegistry, Broker domBroker){
        LOG.info("Wrapper started");
        EventSourceBIProvider providerBI = new EventSourceBIProvider();
        Broker.ProviderSession domCtx = domBroker.registerProvider(providerBI);
        domPublish = domCtx.getService(DOMNotificationPublishService.class);
        this.domBroker = domBroker;

        Node node = Util.getNewNode(nodeName);
        eventSourceImpl = new EventSourceImpl(node);
        eventSourceRegistry.registerEventSource(eventSourceImpl);

        notificationCodec = new NotificationCodec();
        LOG.info("Wrapper finished!");
    }


    public void add(NotificationType notificationType){
        eventSourceImpl.addSchemaPatch(notificationType.getSchemaPath());
        LOG.info("SchemaPath added. List size: {}",eventSourceImpl.getAvailableNotifications().size());
    }

    public void del(NotificationType notificationType){
        eventSourceImpl.delSchemaPatch(notificationType.getSchemaPath());
    }

    public void putMsg(NotificationType notificationType,DataContainer dataContainer, InstanceIdentifier instanceIdentifier){
        putMessage(notificationType,dataContainer,instanceIdentifier,null);
    }

    public void putMsg(NotificationType notificationType, String message){
        putMessage(notificationType,null,null,message);
    }

    public EventSource getEventSource(){
        return eventSourceImpl;
    }

    private void putMessage(NotificationType notificationType,DataContainer dataContainer, InstanceIdentifier instanceIdentifier, String message){
        Map<TopicId,List<SchemaPath>> mapAcceptedTopics = eventSourceImpl.getMapAcceptedTopics();

        LOG.info("topicmapsize: {}",mapAcceptedTopics.size());
        for(Map.Entry<TopicId,List<SchemaPath>> topic: mapAcceptedTopics.entrySet()){
            LOG.info("Loop start - topicId: {} || schemapaths: {}",topic.getKey(), topic.getValue());
            if(topic.getValue().contains(notificationType.getSchemaPath())){
                sendMessage(topic.getKey().getValue(),dataContainer, instanceIdentifier,message);
            }
        }
    }

    private void sendMessage(String topicId,DataContainer dataContainer, InstanceIdentifier instanceIdentifier, String message){
        String eventSourceIndent = eventSourceImpl.getSourceNodeKey().getNodeId().getValue();
        TopicDOMNotification topicNotification;
        if(message==null){
            topicNotification = notificationCodec.createNotification(dataContainer,eventSourceIndent,topicId,instanceIdentifier);
        } else{
            topicNotification = notificationCodec.createNotification(message,eventSourceIndent);
        }

        try {
            domPublish.putNotification(topicNotification);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
