package org.opendaylight.unimgr.mef.notification.api;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.unimgr.mef.notification.es.example.ExampleEventSource;
import org.opendaylight.unimgr.mef.notification.es.ovs.OvsEventSource;
import org.opendaylight.yang.gen.v1.urn.onf.core.network.module.rev160630.g_forwardingconstruct.FcPort;

/**
 * Service that will be injected to a choosen driver to initiate process of notification.
 *
 * @author marek.ryznar@amartus.com
 */
public interface EventSourceApi {

    ExampleEventSource generateExampleEventSource(String nodeName);
    OvsEventSource generateOvsEventSource(String nodeName, DataBroker dataBroker);
    OvsEventSource generateOvsEventSource(FcPort flowPoint, DataBroker dataBroker);
    void createTopicToEventSource(String nodeName);
    void createTopicToEventSource(EventSource eventSource);
    void createTopic(String nodePattern, String notificationPattern);
    void deleteEventSource(String nodeName);
    void deleteEventSource(EventSource eventSource);
    void destroyEventSourceTopics(String nodeName);
    void destroyTopic(String topicId);
}
