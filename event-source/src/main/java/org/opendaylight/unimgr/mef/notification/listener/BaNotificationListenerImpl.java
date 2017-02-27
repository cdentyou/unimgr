package org.opendaylight.unimgr.mef.notification.listener;

import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.unimgr.mef.notification.listener.reader.BaNotificationReader;
import org.opendaylight.unimgr.mef.notification.model.message.BaNotification;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.topic.rev150408.TopicReadService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by root on 15.02.17.
 */
public class BaNotificationListenerImpl extends AbstractTopicReadService implements BaNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(BaNotificationListenerImpl.class);
    private ListenerRegistration<BaNotificationListenerImpl> listenerReg;
    private BaNotificationReader baNotificationReader;

    public BaNotificationListenerImpl(RpcProviderRegistry rpcRegistry, NotificationService notificationService, BaNotificationReader baNotificationReader){
        super(rpcRegistry);
        listenerReg = notificationService.registerNotificationListener(this);
        //rpcRegistry.addRpcImplementation(TopicReadService.class,this);
        this.baNotificationReader = baNotificationReader;
    }

    @Override
    public void onNotification(Notification notification) {
        LOG.info("BiNotificationListenerImpl.onNotification(): {}",notification);
        if(notification==null){
            LOG.warn("Notification is null.");
            return;
        }
        BaNotification baNotification = (BaNotification) notification;
        String topicId = baNotification.getTopicId();
        if(topicId!=null && getRegisteredTopic().keySet().contains(topicId)){
            baNotificationReader.read(notification);
        }
    }

    @Override
    public void close() throws Exception {
        listenerReg.close();
    }
}
