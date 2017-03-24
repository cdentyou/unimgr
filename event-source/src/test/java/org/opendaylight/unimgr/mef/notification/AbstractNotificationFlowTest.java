package org.opendaylight.unimgr.mef.notification;

import org.opendaylight.controller.messagebus.spi.EventSourceRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.unimgr.mef.notification.model.types.NodeId;
import org.opendaylight.unimgr.mef.notification.model.types.NotificationType;
import org.opendaylight.unimgr.mef.notification.model.types.Notifications;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.EventAggregatorService;

import static org.mockito.Mockito.mock;

/**
 * Class creates common objects for Notification Flow Tests
 */
class AbstractNotificationFlowTest {
    EventSourceRegistry eventSourceRegistryMock = mock(EventSourceRegistry.class);

    RpcProviderRegistry rpcProviderRegistryMock = mock(RpcProviderRegistry.class);
    EventAggregatorService eventAggregatorServiceMock = mock(EventAggregatorService.class);
    BindingAwareBroker bindingAwareBroker = mock(BindingAwareBroker.class);

    private String testNotificationName = "notType";
    private String testNotificationName2 = "test";
    String testWrapperName = "Wrapper";
    NotificationType notificationTypeForWrapper = new NotificationType(testNotificationName);
    NotificationType notificationTypeForWrapper2 = new NotificationType(testNotificationName2);
    NotificationType notificationTypeForListener = new NotificationType(testNotificationName);
    NotificationType notificationTypeForListener2 = new NotificationType(testNotificationName2);
    Notifications notifications = new Notifications();
    NodeId nodeId = new NodeId(testWrapperName);
}
