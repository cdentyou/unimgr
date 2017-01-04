package org.opendaylight.unimgr.mef.notification.api;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.messagebus.spi.EventSource;
import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.unimgr.mef.notification.es.EventSourceFactory;
import org.opendaylight.unimgr.mef.notification.es.example.ExampleEventSource;
import org.opendaylight.unimgr.mef.notification.es.ovs.OvsEventSource;
import org.opendaylight.unimgr.mef.notification.impl.Providers;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.*;
import org.opendaylight.yang.gen.v1.urn.onf.core.network.module.rev160630.g_forwardingconstruct.FcPort;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author marek.ryznar@amartus.com
 */
public class EventSourceApiImpl implements EventSourceApi{
    private static final Logger LOG = LoggerFactory.getLogger(ExampleEventSource.class);
    private final Broker domBroker;
    private final EventSourceRegistry eventSourceRegistry;
    private final BindingAwareBroker broker;
    private EventAggregatorService eventAggregatorService;
    private List<EventSource> eventSourceList;
    private Map<TopicId, SchemaPath> topicsPerNotifications;
    private Map<TopicId, String[]> topicsPerPatterns;
    private EventSourceFactory eventSourceFactory;

    public EventSourceApiImpl(Broker domBroker, EventSourceRegistry eventSourceRegistry,BindingAwareBroker broker) {
        LOG.info("EventSourceApiImpl constructor reached.");
        this.domBroker = domBroker;
        this.eventSourceRegistry = eventSourceRegistry;
        this.broker = broker;
        initEventAggregatorService();
        eventSourceList = Collections.synchronizedList(new ArrayList<EventSource>());
        topicsPerNotifications = new ConcurrentHashMap<>();
        topicsPerPatterns = new ConcurrentHashMap<>();
        eventSourceFactory = new EventSourceFactory(domBroker,eventSourceRegistry);
    }

    @Override
    public ExampleEventSource generateExampleEventSource(String nodeName){
        LOG.info(" generateExampleEventSource() has started.");
        ExampleEventSource exampleEventSource = (ExampleEventSource) eventSourceFactory.getEventSource("Example",nodeName);
        eventSourceList.add(exampleEventSource);
        LOG.info(" generateExampleEventSource() has finished.");
        return exampleEventSource;
    }

    @Override
    public OvsEventSource generateOvsEventSource(String nodeName, DataBroker dataBroker) {
        OvsEventSource ovsEventSource = (OvsEventSource) eventSourceFactory.getEventSource("Ovs",nodeName);
        ovsEventSource.setTopologyTransaction(dataBroker);
        return ovsEventSource;
    }

    @Override
    public OvsEventSource generateOvsEventSource(FcPort flowPoint, DataBroker dataBroker) {
        String nodeName = flowPoint.getNode().getValue();
        OvsEventSource ovsEventSource = (OvsEventSource) eventSourceFactory.getEventSource("Ovs",nodeName);
        ovsEventSource.setTopologyTransaction(dataBroker);
        ovsEventSource.setFcPort(flowPoint);
        return ovsEventSource;
    }

    /**
     * Method create topicsPerNotifications corresponding to event source (created for given nodeName).
     *
     * @param nodeName Name of the node included in EventSource.
     */
    @Override
    public void createTopicToEventSource(String nodeName){
        LOG.info("createTopicToEventSource for: {}",nodeName);
        List<EventSource> resultList = eventSourceList.stream()
                .filter(eventSource -> checkEventSourceByNode(eventSource,nodeName))
                .collect(Collectors.toList());

        LOG.info("EventSource list size: {}, result list size: {}",eventSourceList.size(), resultList.size());
        resultList.stream()
                .forEach(eventSource -> addTopicForNotification(eventSource));
    }

    @Override
    public void createTopicToEventSource(EventSource eventSource) {
        addTopicForNotification(eventSource);
    }

    @Override
    public void createTopic(String nodePattern, String notificationPattern) {
        CreateTopicInput topicInput = createTopicInput(nodePattern,notificationPattern);
        Future<RpcResult<CreateTopicOutput>> topicOutput = eventAggregatorService.createTopic(topicInput);
        try {
            TopicId topicId = topicOutput.get().getResult().getTopicId();
            String[] patterns = {nodePattern,notificationPattern};
            topicsPerPatterns.put(topicId,patterns);
            LOG.info("Topic for nodePattern: {} and notificationPattern: {} created with id: {}",nodePattern,notificationPattern,topicId.getValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method destroy all topicsPerNotifications corresponding to EventSource created by {@link #createTopicToEventSource(String) createTopicToEventSourceAt} method
     *
     * @param nodeName Name of the node included in EventSource.
     */
    @Override
    public void destroyEventSourceTopics(String nodeName){
        List<EventSource> resultList = eventSourceList.stream()
                .filter(eventSource -> checkEventSourceByNode(eventSource,nodeName))
                .collect(Collectors.toList());

        resultList.stream()
                .forEach(eventSource -> destroyEventSourceTopicsHelper(eventSource));
        deleteEventSource(nodeName);
    }

    @Override
    public void destroyTopic(String topicId){
        destroyTopic(new TopicId(topicId));
    }

    @Override
    public void deleteEventSource(String nodeName){
        List<EventSource> toDelete = eventSourceList.stream()
                .filter(eventSource -> checkEventSourceByNode(eventSource,nodeName))
                .collect(Collectors.toList());

        toDelete.stream()
                .forEach(eventSource -> deleteEventSource(eventSource));
    }

    @Override
    public void deleteEventSource(EventSource eventSource){
        destroyEventSourceTopicsHelper(eventSource);
        eventSourceList.remove(eventSource);
        try {
            eventSource.close();
        } catch (Exception e) {
            LOG.warn("EventSource closure error: {}",e);
        }
    }

    public boolean checkEventSourceByNode(EventSource eventSource, String nodeName){
        String esNodeValue = eventSource.getSourceNodeKey().getNodeId().getValue();
        return nodeName.equals(esNodeValue);
    }

    private void destroyEventSourceTopicsHelper(EventSource eventSource){
        List<SchemaPath> notifications = eventSource.getAvailableNotifications();
        notifications.stream()
                .forEach(notification -> handleNotificationDestroy(notification));
    }

    private void handleNotificationDestroy(SchemaPath schemaPath){
        if(!topicsPerNotifications.containsValue(schemaPath)){
            return ;
        }
        Set<Map.Entry<TopicId, SchemaPath>> toDelete = topicsPerNotifications.entrySet().stream()
                .filter(topic -> topic.getValue().equals(schemaPath))
                .collect(Collectors.toSet());

        toDelete.stream()
                .forEach(topic -> destroyTopic(topic.getKey()));
    }

    private void destroyTopic(TopicId topicId){
        topicsPerNotifications.remove(topicId);
        DestroyTopicInputBuilder destroyTopicInputBuilder = new DestroyTopicInputBuilder();
        destroyTopicInputBuilder.setTopicId(topicId);
        eventAggregatorService.destroyTopic(destroyTopicInputBuilder.build());
    }

    private CreateTopicInput createTopicInput(String nodeName, String notificationPatternName){
        CreateTopicInputBuilder createTopicInputBuilder = new CreateTopicInputBuilder();
        Pattern pattern = new Pattern(nodeName);
        createTopicInputBuilder.setNodeIdPattern(pattern);
        NotificationPattern notificationPattern = new NotificationPattern(notificationPatternName);
        createTopicInputBuilder.setNotificationPattern(notificationPattern);
        return createTopicInputBuilder.build();
    }

    private void addTopicForNotification(EventSource eventSource){
        LOG.info("addTopicForNotification");
        String nodeName = eventSource.getSourceNodeKey().getNodeId().getValue();
        List<SchemaPath> notifications = eventSource.getAvailableNotifications();
        String notificationPattern;
        for(SchemaPath notification:notifications){
            LOG.info("addTopicForNotification for loop");
            notificationPattern = notification.getLastComponent().getNamespace().toString();
            CreateTopicInput topicInput = createTopicInput(nodeName,notificationPattern);
            Future<RpcResult<CreateTopicOutput>> topicOutput = eventAggregatorService.createTopic(topicInput);
            try {
                TopicId topicId = topicOutput.get().getResult().getTopicId();
                topicsPerNotifications.put(topicId,notification);
                LOG.info("Topic for node: {} and notification pattern: {} created with id: {}",nodeName,notificationPattern,topicId.getValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to initialize EventAggregatorService needed to create and destroy topicsPerNotifications.
     */
    public void initEventAggregatorService(){
        final BindingAwareBroker.ProviderContext bindingCtx = broker.registerProvider(new Providers.BindingAware());
        final RpcProviderRegistry rpcRegistry = bindingCtx.getSALService(RpcProviderRegistry.class);
        eventAggregatorService = rpcRegistry.getRpcService(EventAggregatorService.class);
    }

    public void startUp(){
        LOG.info("Bundle EventSourceApiImpl has started.");
    }

    public List<EventSource> getEventSourceList() {
        return eventSourceList;
    }

    public Map<TopicId, SchemaPath> getTopicsPerNotifications() {
        return topicsPerNotifications;
    }

    public String getSchemaPathString(SchemaPath schemaPath){
        return schemaPath.getLastComponent().getNamespace().toString();
    }
}