package org.opendaylight.unimgr.mef.notification.message;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.unimgr.mef.notification.EventSourceTestUtils;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class NotificationCreatorTest {

    private NotificationCreator notificationCreator;

    @Before
    public void setUp(){
        notificationCreator = new NotificationCreator();
    }

    @Test
    public void testCreateNotification(){
        //given
        Node node = EventSourceTestUtils.prepareTestNode();
        InstanceIdentifier instanceIdentifier = EventSourceTestUtils.prepareTestNodeInstanceIdentifier(node.getNodeId());
        String testEventSourceIndent = "eventSourceIndent";
        String testTopicId = "12345";

        //when
        TopicDOMNotification notification = notificationCreator.createNotification(node,instanceIdentifier,testEventSourceIndent,testTopicId);

        //then
        //check identifier
        assertEquals(NotificationCreator.getTopicNotificationArg(),notification.getBody().getIdentifier());

        Class<?> topicIdClass = TopicId.class;
        Collection<DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?>> childrens = notification.getBody().getValue();
        for(DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> dataContainerChild : childrens ){
            System.out.println(dataContainerChild.getValue().getClass());
            Class<?> clazz = dataContainerChild.getValue().getClass();
            if(clazz.equals(topicIdClass)){
                System.out.println("1");
                TopicId topicId = (TopicId) dataContainerChild.getValue();
                assertEquals(new TopicId(testTopicId),topicId);
            } else if(clazz.equals(String.class)){
                System.out.println("2");
                assertEquals(testEventSourceIndent,dataContainerChild.getValue());
            } else if(dataContainerChild.getValue() instanceof Collection<?>){
                System.out.println("3");
                Collection<?> obj = (Collection<?>) dataContainerChild.getValue();
            }
        }
        System.out.println(notification);
    }

//    private void checkPassedObject(){
//
//    }

}
