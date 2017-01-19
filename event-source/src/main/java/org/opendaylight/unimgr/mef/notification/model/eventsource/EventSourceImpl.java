package org.opendaylight.unimgr.mef.notification.model.eventsource;

import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.unimgr.mef.notification.impl.Util;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.NotificationPattern;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventsource.rev141202.*;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Implementation of EventSource used in @see org.opendaylight.unimgr.mef.notification.model.eventsource.EventSourceWrapper.
 */
public class EventSourceImpl implements EventSource {
    private static final Logger LOG = LoggerFactory.getLogger(EventSourceImpl.class);

    protected final Node sourceNode;
    protected final List<SchemaPath> listSchemaPaths = new ArrayList<>();
    private final Map<TopicId,List<SchemaPath>> mapAcceptedTopics = new HashMap<>();

    public EventSourceImpl(Node node){
        sourceNode = node;
    }

    @Override
    public NodeKey getSourceNodeKey() {
        return sourceNode.getKey();
    }

    @Override
    public List<SchemaPath> getAvailableNotifications() {
        return listSchemaPaths;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public Future<RpcResult<JoinTopicOutput>> joinTopic(JoinTopicInput joinTopicInput) {
        LOG.info("Start join Topic {} {}",getSourceNodeKey().getNodeId().getValue(), joinTopicInput.getTopicId().getValue());
        final NotificationPattern notificationPattern = joinTopicInput.getNotificationPattern();
        // obtaining list of SchamePath of notifications which match with notification pattern
        final List<SchemaPath> matchingNotifications = getMatchingNotifications(notificationPattern);
        LOG.info("JoinTopic matching notifications: {}",matchingNotifications.toString());
        JoinTopicStatus joinTopicStatus = JoinTopicStatus.Down;
        if(Util.isNullOrEmpty(matchingNotifications) == false){
            // if there is at least one SchemaPath matched with NotificationPattern then topic is add into the list
            LOG.info("Node {} Join topic {}", sourceNode.getNodeId().getValue(), joinTopicInput.getTopicId().getValue());
            mapAcceptedTopics.put(joinTopicInput.getTopicId(),matchingNotifications);
            joinTopicStatus = JoinTopicStatus.Up;
        }
        final JoinTopicOutput output = new JoinTopicOutputBuilder().setStatus(joinTopicStatus).build();
        return immediateFuture(RpcResultBuilder.success(output).build());
    }

    @Override
    public Future<RpcResult<Void>> disJoinTopic(DisJoinTopicInput disJoinTopicInput) {
        return null;
    }

    /*
     * Method return list of SchemaPath matched by notificationPattern
     */
    private List<SchemaPath> getMatchingNotifications(NotificationPattern notificationPattern){
        // wildcard notification pattern is converted into regex pattern
        // notification pattern could be changed into regex syntax in the future
        LOG.info("getMatchingNotifications notification: {}",notificationPattern.getValue());
        final String regex = Util.wildcardToRegex(notificationPattern.getValue());
        LOG.info("getMatchingNotifications regex: {}",regex);
        final Pattern pattern = Pattern.compile(regex);

        return Util.selectSchemaPath(getAvailableNotifications(), pattern);
    }

    public void addSchemaPatch(SchemaPath schemaPath){
        listSchemaPaths.add(schemaPath);
    }

    public void delSchemaPatch(SchemaPath schemaPath){
        listSchemaPaths.remove(schemaPath);
    }

    public Map<TopicId,List<SchemaPath>> getMapAcceptedTopics(){
        return mapAcceptedTopics;
    }
}
