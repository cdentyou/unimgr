package org.opendaylight.unimgr.mef.notification.producer;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventSourceCreator {

    private Broker domBroker;
    private EventSourceRegistry eventSourceRegistry;
    private long messageGeneratePeriod = 10;

    public EventSourceCreator(Broker domBroker, EventSourceRegistry eventSourceRegistry){
        this.domBroker = domBroker;
        this.eventSourceRegistry = eventSourceRegistry;
    }

    public void startUp(){
        createBaNodeEventSource();
        createBiNodeEventSource();
        createStringEventSource();
    }

    private void createBaNodeEventSource(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        String nodeId = "BaNode";
        Node node = PayloadCreator.prepareTestNode(nodeId);
        InstanceIdentifier instanceIdentifier = PayloadCreator.prepareTestNodeInstanceIdentifier(new NodeId(nodeId));
        BaNotificationGenerator baNotificationGenerator = new BaNotificationGenerator("BaEventSource",eventSourceRegistry,node,"binding-aware-pattern",domBroker,instanceIdentifier);
        scheduler.scheduleAtFixedRate(baNotificationGenerator, messageGeneratePeriod, messageGeneratePeriod, TimeUnit.SECONDS);
    }

    private void createBiNodeEventSource(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        LeafNode<String> node = PayloadCreator.prepareTestLeafNode();
        BiNotificationGenerator biNotificationGenerator = new BiNotificationGenerator("BiEventSource",eventSourceRegistry,domBroker,node,null,"binding-independent-pattern");
        scheduler.scheduleAtFixedRate(biNotificationGenerator, messageGeneratePeriod, messageGeneratePeriod, TimeUnit.SECONDS);
    }

    private void createStringEventSource(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        String message = "Event Source Test Message.";
        BiNotificationGenerator biNotificationGenerator = new BiNotificationGenerator("StringEventSource",eventSourceRegistry,domBroker,null,message,"string-pattern");
        scheduler.scheduleAtFixedRate(biNotificationGenerator, messageGeneratePeriod, messageGeneratePeriod, TimeUnit.SECONDS);

    }
}
