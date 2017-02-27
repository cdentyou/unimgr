package org.opendaylight.unimgr.mef.notification.listener;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.unimgr.mef.notification.topic.TopicHandler;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.topic.rev150408.ReadTopicInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.topic.rev150408.TopicReadService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Created by root on 15.02.17.
 */
public class AbstractTopicReadService implements TopicReadService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTopicReadService.class);
    private final Map<String,Map.Entry<NodeId,Notifications>> registeredTopic = new HashMap<>();
    private TopicHandler topicHandler;

    public AbstractTopicReadService(RpcProviderRegistry rpcProviderRegistry){
        rpcProviderRegistry.addRpcImplementation(TopicReadService.class,this);
        EventAggregatorService eventAggregatorService = rpcProviderRegistry.getRpcService(EventAggregatorService.class);
        this.topicHandler = new TopicHandler(eventAggregatorService);
    }

    @Override
    public Future<RpcResult<Void>> readTopic(ReadTopicInput input) {
        String topicId = input.getTopicId().getValue();
        // if requested TopicId has not been requested before then it is added into to register
        if(registeredTopic.keySet().contains(topicId) == false){
            registeredTopic.put(topicId,null);
            LOG.info("Listener start read notification with TopicId {}", topicId);
        }
        return immediateFuture(RpcResultBuilder.success((Void) null).build());
    }

    /**
     * Method to create the topic from NodeId and List of Notifications.
     *
     * @param nodeId when null is passed - everything will be matched
     * @param notifications when null is passed - everything will be matched
     * @return topicId - its null if it is already registered
     */
    public String readTopic(NodeId nodeId, Notifications notifications) {
        Map.Entry<NodeId,Notifications> topicEntry = new AbstractMap.SimpleEntry<NodeId,Notifications>(nodeId, notifications);
        String topicId = null;
        if(!registeredTopic.values().contains(topicEntry)){
            topicId = topicHandler.createTopic(nodeId,notifications);
            registeredTopic.put(topicId,topicEntry);
            LOG.info("Listener create topic from nodeID: {}, and notifications: {}", nodeId.getValue().toString(),notifications.getNotifications().toString());
            LOG.info("Listener start read notification with TopicId {}", topicId);
        }
        return topicId;
    }

    public Map<String,Map.Entry<NodeId,Notifications>> getRegisteredTopic(){
        return registeredTopic;
    }
}
