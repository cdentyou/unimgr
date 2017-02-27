package org.opendaylight.unimgr.mef.notification.consumer;

import javassist.ClassPool;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMNotificationServiceAdapter;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.unimgr.mef.notification.eventsource.BaEventSourceProvider;
import org.opendaylight.unimgr.mef.notification.listener.BaNotificationListenerImpl;
import org.opendaylight.unimgr.mef.notification.listener.BiNotificationListenerImpl;
import org.opendaylight.unimgr.mef.notification.listener.reader.BaNotificationReaderImpl;
import org.opendaylight.unimgr.mef.notification.listener.reader.BiNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;

/**
 * Created by root on 22.02.17.
 */
public class NotificationListenerCreator {

    final private BindingAwareBroker bindingAwareBroker;
    //private EventAggregatorService eventAggregatorService;
    final private RpcProviderRegistry rpcRegistry;
    final BindingAwareBroker.ProviderContext bindingCtx;
    private NotificationService notificationService;
    private DOMNotificationService domNotificationService;
    private Broker broker;
    private BindingNormalizedNodeCodecRegistry bindingNormalizedNodeCodecRegistry;

    public NotificationListenerCreator(BindingAwareBroker bindingAwareBroker, Broker broker){
        this.bindingAwareBroker = bindingAwareBroker;
        this.broker = broker;
        bindingCtx = this.bindingAwareBroker.registerProvider(new BaEventSourceProvider());
        rpcRegistry = bindingCtx.getSALService(RpcProviderRegistry.class);
        //eventAggregatorService = rpcRegistry.getRpcService(EventAggregatorService.class);
        //initBindingNormalizedNodeCodecRegistry();
        initDependencies();
        //initBaDependencies();
    }

    public void startUp(){
        createBiNotificationListener();
        //createBaNotificationListener();
    }

    private void createBaNotificationListener(){
        BaNotificationReaderImpl baNotificationReader = new BaNotificationReaderImpl();
        BaNotificationListenerImpl baNotificationListener = new BaNotificationListenerImpl(rpcRegistry,notificationService,baNotificationReader);
        NodeId nodeId = new NodeId("BaNode");
        Notifications notifications = createNotifications("binding-aware-pattern");
        baNotificationListener.readTopic(nodeId,notifications);
    }

    private void createBiNotificationListener(){
        BiNotificationReader biNotificationReader = new BiNotificationReader();
        BiNotificationListenerImpl biNotificationListener = new BiNotificationListenerImpl(rpcRegistry,domNotificationService,biNotificationReader);
        NodeId nodeId = new NodeId("BiEventSource");
        Notifications notifications = createNotifications("binding-independent-pattern");
        biNotificationListener.readTopic(nodeId,notifications);
    }

    public void initBaDependencies(){
        domNotificationService = broker.registerConsumer(new Consumers.NoopDomConsumer()).getService(DOMNotificationService.class);
        BindingNormalizedNodeSerializer codec = bindingNormalizedNodeCodecRegistry;
        BindingDOMNotificationServiceAdapter bindingDOMNotificationServiceAdapter =
            new BindingDOMNotificationServiceAdapter(codec,domNotificationService);
        notificationService = bindingDOMNotificationServiceAdapter;
                //consumerContext.getSALService(NotificationService.class);
                //bindingAwareBroker.registerConsumer(new Consumers.NoopBaConsumer()).getSALService(NotificationService.class);
    }

    public void initBiDependencies(){
        domNotificationService = broker.registerConsumer(new Consumers.NoopDomConsumer()).getService(DOMNotificationService.class);
        BindingNormalizedNodeSerializer codec = bindingNormalizedNodeCodecRegistry;
        BindingDOMNotificationServiceAdapter bindingDOMNotificationServiceAdapter =
                new BindingDOMNotificationServiceAdapter(codec,domNotificationService);
        notificationService = bindingDOMNotificationServiceAdapter;
    }

    public void initDependencies(){
        domNotificationService = broker.registerConsumer(new Consumers.NoopDomConsumer()).getService(DOMNotificationService.class);
//        BindingNormalizedNodeSerializer codec = bindingNormalizedNodeCodecRegistry;
//        BindingDOMNotificationServiceAdapter bindingDOMNotificationServiceAdapter =
//                new BindingDOMNotificationServiceAdapter(codec,domNotificationService);
//        notificationService = bindingDOMNotificationServiceAdapter;
    }

    private Notifications createNotifications(String notificationName){
        NotificationType notificationType = new NotificationType(notificationName);
        Notifications notifications = new Notifications();
        notifications.add(notificationType);
        return notifications;
    }

    private void initBindingNormalizedNodeCodecRegistry(){
        JavassistUtils utils =
                JavassistUtils.forClassPool(ClassPool.getDefault());
        bindingNormalizedNodeCodecRegistry = new
                BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
    }


}
