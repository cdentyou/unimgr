package org.opendaylight.unimgr.mef.notification.topic;

import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.*;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Class created to handle topics.
 */
public class TopicHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TopicHandler.class);

    private static final String MATCH_ALL = ".*";
    private EventAggregatorService eventAggregatorService;

    public TopicHandler(EventAggregatorService eventAggregatorService){
        this.eventAggregatorService = eventAggregatorService;
    }

    public String createTopic(NodeId nodeId, Notifications notifications){
        CreateTopicInput topicInput = createTopicInput(nodeId,notifications);
        Future<RpcResult<CreateTopicOutput>> topicOutput = eventAggregatorService.createTopic(topicInput);
        TopicId topicId = null;
        try {
            topicId = topicOutput.get().getResult().getTopicId();
        } catch (InterruptedException e) {
            LOG.error("Computation error: {}",e.toString());
        } catch (ExecutionException e) {
            LOG.error("Current thread was interrupted: {}",e.toString());
        }

        return topicId.getValue();
    }

    private CreateTopicInput createTopicInput(NodeId nodeId,  Notifications notifications){
        String nodeName;
        String notificationPatternName;

        if(nodeId == null){
            nodeName = MATCH_ALL;
        } else {
            nodeName = nodeId.getValue();
        }

        if(notifications == null){
            notificationPatternName = MATCH_ALL;
        } else {
            notificationPatternName = createNotificationPatternName(notifications);
        }

        CreateTopicInputBuilder createTopicInputBuilder = new CreateTopicInputBuilder();
        Pattern pattern = new Pattern(nodeName);
        createTopicInputBuilder.setNodeIdPattern(pattern);
        NotificationPattern notificationPattern = new NotificationPattern(notificationPatternName);
        createTopicInputBuilder.setNotificationPattern(notificationPattern);
        return createTopicInputBuilder.build();
    }

    /**
     * Method creates regex pattern from list of notifications.
     *
     * @param notifications List of notifications.
     * @return regex that match all given notifications.
     */
    public static String createNotificationPatternName(Notifications notifications){
        StringBuilder pattern = new StringBuilder();
        boolean appendPipe = false;
        for(NotificationType notificationType: notifications.getNotifications()){
            if(appendPipe){
                pattern.append("|");
            }
            SchemaPath schemaPath = notificationType.getSchemaPath();
            String path = schemaPath.getLastComponent().getNamespace().toString();
            pattern.append(path);
            appendPipe = true;
        }
        return pattern.toString();
    }
}
