package org.opendaylight.unimgr.mef.notification.model.listener;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationListener;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.user.agent.topic.rev150408.UserAgentTopicReadService;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.concurrent.Future;

/**
 * Interface that implement all needed interfaces for listening in publish - subscriber mode.
 */
public interface EventSourceListener extends DOMNotificationListener, UserAgentTopicReadService, AutoCloseable {

    /**
     * Method to create the topic from NodeId and List of Notifications.
     *
     * @param nodeId
     * @param notifications
     * @return
     */
    Future<RpcResult<Void>> readTopic(NodeId nodeId, Notifications notifications);
}
