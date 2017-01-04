package org.opendaylight.unimgr.mef.notification.es;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.unimgr.mef.notification.impl.Util;
import org.opendaylight.unimgr.mef.notification.message.MessageGenerator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.NotificationPattern;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventsource.rev141202.*;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Abstract class implementing functionality used in all EventSources in current module.
 *
 * @author marek.ryznar@amartus.com
 */
public abstract class AbstractEventSource implements EventSource {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventSource.class);

    protected Short messageGeneratePeriod = 10;
    protected final ScheduledExecutorService scheduler;
    protected final DOMNotificationPublishService domPublish;
    protected final List<SchemaPath> listSchemaPaths = new ArrayList<>();
    protected MessageGenerator messageGenerator;

    protected final Node sourceNode;
    protected String messageText = "Text message!";

    public AbstractEventSource(DOMNotificationPublishService domPublish, Node node){
        LOG.info("AbstractEventSource constructor started.");
        this.sourceNode = node;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.domPublish = domPublish;

        LOG.info("AbstractEventSource constructor finished.");
    }

    @Override
    public NodeKey getSourceNodeKey() {
        return sourceNode.getKey();
    }

    @Override
    public List<SchemaPath> getAvailableNotifications() {
        LOG.info("getAvailableNotifications: {}",this.listSchemaPaths.toString());
        return Collections.unmodifiableList(this.listSchemaPaths);
    }

    @Override
    public void close() throws Exception {
        this.scheduler.shutdown();
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
            messageGenerator.addTopic(joinTopicInput.getTopicId(),matchingNotifications);
            joinTopicStatus = JoinTopicStatus.Up;
        }
        final JoinTopicOutput output = new JoinTopicOutputBuilder().setStatus(joinTopicStatus).build();
        return immediateFuture(RpcResultBuilder.success(output).build());
    }

    @Override
    public Future<RpcResult<Void>> disJoinTopic(DisJoinTopicInput disJoinTopicInput) {
        messageGenerator.removeTopic(disJoinTopicInput.getTopicId());
        return immediateFuture(RpcResultBuilder.success((Void) null).build());
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
}
