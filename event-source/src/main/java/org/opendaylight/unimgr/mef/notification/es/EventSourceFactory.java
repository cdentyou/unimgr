package org.opendaylight.unimgr.mef.notification.es;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.es.example.ExampleEventSource;
import org.opendaylight.unimgr.mef.notification.es.ovs.OvsEventSource;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;

/**
 * Class made to create event sources.
 *
 * @author marek.ryznar@amartus.com
 */
public class EventSourceFactory {

    private final Broker domBroker;
    private final EventSourceRegistry eventSourceRegistry;

    public EventSourceFactory(Broker domBroker, EventSourceRegistry eventSourceRegistry){
        this.domBroker = domBroker;
        this.eventSourceRegistry = eventSourceRegistry;
    }

    public EventSource getEventSource(String eventSourceName,String nodeName){
        EventSourceBIProvider providerBI = new EventSourceBIProvider();
        Broker.ProviderSession domCtx = domBroker.registerProvider(providerBI);
        DOMNotificationPublishService domPublish = domCtx.getService(DOMNotificationPublishService.class);

        Node node = getNewNode(nodeName);
        EventSource eventSource;
        switch (eventSourceName){
            case "Example" :
                eventSource = new ExampleEventSource(domPublish,node);
                break;
            case "Ovs":
                eventSource = new OvsEventSource(domPublish,node);
                break;
            default:
                return null;
        }
        eventSourceRegistry.registerEventSource(eventSource);
        return eventSource;
    }

    private Node getNewNode(String nodeIdent){
        NodeId nodeId = new NodeId(nodeIdent);
        NodeBuilder nb = new NodeBuilder();
        nb.setKey(new NodeKey(nodeId));
        return nb.build();
    }
}
