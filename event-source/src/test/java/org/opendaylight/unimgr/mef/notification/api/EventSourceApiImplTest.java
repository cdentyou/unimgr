package org.opendaylight.unimgr.mef.notification.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.EventSourceTestUtils;
import org.opendaylight.unimgr.mef.notification.es.example.ExampleEventSource;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicId;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author marek.ryznar@amartus.com
 */
@RunWith(PowerMockRunner.class)
public class EventSourceApiImplTest {
    private EventSourceApiImpl eventSourceApi;
    private Broker brokerMock;
    private EventSourceRegistry eventSourceRegistry;
    private BindingAwareBroker bindingAwareBrokerMock;
    private EventAggregatorService eventAggregatorServiceMock;
    private DOMNotificationPublishService domPublishMock;
    private RpcProviderRegistry rpcProviderRegistryMock;
    private static final String nodeName = "testNodeName";

    @Before
    public void setUp(){
        //given
        brokerMock = mock(Broker.class);
        eventSourceRegistry = mock(EventSourceRegistry.class);
        bindingAwareBrokerMock = mock(BindingAwareBroker.class);

        BindingAwareBroker.ProviderContext bindingCtxMock = mock(BindingAwareBroker.ProviderContext.class);
        rpcProviderRegistryMock = mock(RpcProviderRegistry.class);
        eventAggregatorServiceMock = mock(EventAggregatorService.class);
        Broker.ProviderSession domCtxMock = mock(Broker.ProviderSession.class);
        domPublishMock = mock(DOMNotificationPublishService.class);

        when(rpcProviderRegistryMock.getRpcService(EventAggregatorService.class)).thenReturn(eventAggregatorServiceMock);
        when(bindingCtxMock.getSALService(RpcProviderRegistry.class)).thenReturn(rpcProviderRegistryMock);
        when(bindingAwareBrokerMock.registerProvider(any())).thenReturn(bindingCtxMock);
        when(domCtxMock.getService(DOMNotificationPublishService.class)).thenReturn(domPublishMock);
        when(brokerMock.registerProvider(any())).thenReturn(domCtxMock);

        eventSourceApi = new EventSourceApiImpl(brokerMock, eventSourceRegistry,bindingAwareBrokerMock);
    }

    @Test
    public void testGenerateAndDeleteExampleEventSource(){
        //generate test:
        //when
        ExampleEventSource exampleEventSource = eventSourceApi.generateExampleEventSource(nodeName);

        //then
        List<EventSource> eventSources = eventSourceApi.getEventSourceList();
        assertEquals(1,eventSources.size());
        EventSourceTestUtils.checkExampleEventSource((ExampleEventSource) eventSources.get(0),nodeName,eventSourceApi);

        //delete test:
        //given
        eventSourceApi.deleteEventSource(exampleEventSource);

        //then
        assertFalse(eventSourceApi.getEventSourceList().contains(exampleEventSource));
    }

    @Test
    public void testCreateAndDestroyTopicToEventSource(){
        //given
        ExampleEventSource exampleEventSource = eventSourceApi.generateExampleEventSource(nodeName);
        when(eventAggregatorServiceMock.createTopic(any()))
                .thenReturn(EventSourceTestUtils.createTopicMock(eventSourceApi))
                .thenReturn(EventSourceTestUtils.createTopicMock(eventSourceApi))
                .thenReturn(EventSourceTestUtils.createTopicMock(eventSourceApi));

        //create test:
        //when
        eventSourceApi.createTopicToEventSource(nodeName);
        EventSourceTestUtils.joinTopicsToEventSource(exampleEventSource,eventSourceApi);
        try {
            //sleep because notification is send every second since join the topic (if topic was joined)
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //then
        Map<TopicId, SchemaPath> topics = eventSourceApi.getTopicsPerNotifications();
        assertEquals(3,topics.size());
        exampleEventSource.getAvailableNotifications().stream()
                .forEach(schemaPath -> assertTrue(topics.values().contains(schemaPath)));
        try {
            verify(domPublishMock).putNotification(any());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Destroy test:
        //when
        eventSourceApi.destroyEventSourceTopics(exampleEventSource.getSourceNodeKey().getNodeId().getValue());

        //then
        assertEquals(0,eventSourceApi.getTopicsPerNotifications().size());
        assertFalse(eventSourceApi.getEventSourceList().contains(exampleEventSource));
    }
}