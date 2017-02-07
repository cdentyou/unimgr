package org.opendaylight.unimgr.mef.notification.message;

import org.junit.Before;
import org.opendaylight.unimgr.mef.notification.EventSourceTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.user.agent.topic.rev150408.TopicId;
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

    //TODO: delete or finish (if there will be spare time)
   // @Test
    public void testCreateNotification(){
        //given
        Node node = EventSourceTestUtils.prepareTestNode("TestNode",true);
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
//            if(clazz.equals(topicIdClass)){
//                TopicId topicId = (TopicId) dataContainerChild.getValue();
//                assertEquals(new TopicId(testTopicId),topicId);
//            } else if(clazz.equals(String.class)){
//                assertEquals(testEventSourceIndent,dataContainerChild.getValue());
//            } else if(dataContainerChild.getValue() instanceof Collection<?>){
//                Collection<?> collection = (Collection<?>) dataContainerChild.getValue();
//            }
        }
    }


//    private void checkPassedObject(){
//
//    }

}
