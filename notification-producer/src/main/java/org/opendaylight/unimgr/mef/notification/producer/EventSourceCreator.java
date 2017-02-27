package org.opendaylight.unimgr.mef.notification.producer;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 22.02.17.
 */
public class EventSourceCreator {

    private Broker domBroker;
    private EventSourceRegistry eventSourceRegistry;
    private BindingAwareBroker broker;
    private long messageGeneratePeriod = 5;

    public EventSourceCreator(Broker domBroker, EventSourceRegistry eventSourceRegistry, BindingAwareBroker broker){
        this.domBroker = domBroker;
        this.eventSourceRegistry = eventSourceRegistry;
        this.broker = broker;

    }

    public void startUp(){
        createBaNodeEventSource();
        createBiNodeEventSource();
        createStringEventSource();
    }

    private void createBaNodeEventSource(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Node node = PayloadCreator.prepareTestNode("BaNode");
        BaNotificationGenerator baNotificationGenerator = new BaNotificationGenerator("BaEventSource",eventSourceRegistry,broker,node,"binding-aware-pattern");
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
