package org.opendaylight.unimgr.mef.notification.listener;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.user.agent.topic.rev150408.UserAgentTopicReadService;

/**
 * Created by root on 05.01.17.
 */
public interface EventSourceListener extends DOMNotificationListener, UserAgentTopicReadService, AutoCloseable {
}
