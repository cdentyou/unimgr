package org.opendaylight.unimgr.mef.notification.listener.reader;


import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.unimgr.mef.notification.utils.NotificationCodec;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by root on 16.02.17.
 */
public class BaNotificationReader<T extends DataContainer> implements DomNotificationReader {
    private static final Logger LOG = LoggerFactory.getLogger(BaNotificationReader.class);
    private static final YangInstanceIdentifier.NodeIdentifier YANG_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "yang-name").intern());
    private static final YangInstanceIdentifier.NodeIdentifier CLASS_NAME_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "class-name").intern());
    private List<T> receivedObjects = new LinkedList<>();
    private NotificationCodec notificationCodec = NotificationCodec.getInstance();
    private Class<?> expectedClass = DataContainer.class;

    @Override
    public void read(DOMNotification notification) {
        T receivedObject = (T) parsePayload(notification);
        LOG.info("Bi object received: {}",receivedObject);
        receivedObjects.add(receivedObject);
    }

    private DataContainer parsePayload(DOMNotification notification){
        ContainerNode body = notification.getBody();
        if(body.getChild(CLASS_NAME_ARG).isPresent() && body.getChild(YANG_ARG).isPresent()){
            String className = (String) notification.getBody().getChild(CLASS_NAME_ARG).get().getValue();
            try {
                expectedClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                LOG.warn("ClassNotFoundException, {}",e.toString());
                return null;
            }
            YangInstanceIdentifier yangInstanceIdentifier = (YangInstanceIdentifier) notification.getBody().getChild(YANG_ARG).get().getValue();
            NormalizedNode<?, ?> normalizedNode = (NormalizedNode<?, ?>) notification.getBody().getChild(PAYLOAD_ARG).get().getValue();

            DataContainer dataContainer = notificationCodec.fromDataContainerChild(normalizedNode,expectedClass,yangInstanceIdentifier);
            return dataContainer;
        }
        LOG.warn("Class name or YangInstanceIdetifier is not present in notification");
        return null;
    }

    public List<T> getReceivedObjects(){
        return receivedObjects;
    }
}
