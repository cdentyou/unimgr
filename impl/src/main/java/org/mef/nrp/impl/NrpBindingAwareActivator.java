package org.mef.nrp.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.AbstractBrokerAwareActivator;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * this activator enables us to aggregate implementations of
 *
 * @author alex.feigin@hpe.com
 */
public class NrpBindingAwareActivator extends AbstractBrokerAwareActivator {
    private static final Logger LOG = LoggerFactory.getLogger(NrpBindingAwareActivator.class);

    @Override
    protected void onBrokerAvailable(BindingAwareBroker broker, BundleContext context) {
        broker.registerConsumer(activationDriverRepoService);
    }

////    private final MBeanServer configMBeanServer = ManagementFactory.getPlatformMBeanServer();
//
//    private ServiceRegistration<ActivationDriverBuilder> activationDriverBuilderServiceRegistration;
//    //    private NrpActivator. configRegistryLookup = null;
//    private BundleContext context;
////    private ServiceRegistration<ConfigSubsystemFacadeFactory> osgiRegistration;
//
    final ActivationDriverRepoService activationDriverRepoService = new ActivationDriverRepoService();
//
//    @Override
//    public void start(final BundleContext context) throws Exception {
//        LOG.debug("ConfigPersister starting");
//        this.context = context;
//
//        context.registerService(BindingAwareConsumer.class, activationDriverRepoService, new Hashtable());
//        context.registerService(IActivationDriverRepoService.class, activationDriverRepoService, new Hashtable());
//        ServiceTrackerCustomizer<ActivationDriverBuilder, ActivationDriverRepoService> customizer = //
//                new ServiceTrackerCustomizer<ActivationDriverBuilder, ActivationDriverRepoService>() {
//                    @Override
//                    public ActivationDriverRepoService addingService(ServiceReference<ActivationDriverBuilder> reference) {
//                        LOG.debug("Got addingService(SchemaContextProvider) event");
//                        if (reference.getProperty(ActivationDriverBuilder.class.getName()) == null) {
//                            LOG.debug("ActivationDriverBuilder not from unimngr. Ignoring");
//                            return null;
//                        }
//
//                        ActivationDriverBuilder activationDriverBuilder = reference.getBundle().getBundleContext().getService(reference);
//                        final Object sourceProvider = Preconditions.checkNotNull(
//                                reference.getProperty(ActivationDriverBuilder.class.getName()), "Source provider not found");
//                        Preconditions.checkArgument(sourceProvider instanceof SchemaSourceProvider);
//                        activationDriverRepoService.bindBuilder(activationDriverBuilder);
//
////                final BindingRuntimeContext runtimeContext = (BindingRuntimeContext) reference
////                        .getProperty(BindingRuntimeContext.class.getName());
////                LOG.debug("BindingRuntimeContext retrieved as {}", runtimeContext);
////                if(runtimeContext != null) {
////                    yangStoreService.refresh(runtimeContext);
////                }
//
////                yangStoreServiceServiceRegistration = context.registerService(ActivationDriverRepoService.class, yangStoreService, new Hashtable<String, Object>());
////                configRegistryLookup = new NrpActivator.ConfigRegistryLookupThread();
////                configRegistryLookup.start();
////                return yangStoreService;
//                        return activationDriverRepoService;
//                    }
//
//
//                    @Override
//                    public void modifiedService(ServiceReference<ActivationDriverBuilder> reference, ActivationDriverRepoService service) {
////                if (service == null) {
////                    return;
////                }
////
////                LOG.debug("Got modifiedService(SchemaContextProvider) event");
////                final BindingRuntimeContext runtimeContext = (BindingRuntimeContext) reference
////                        .getProperty(BindingRuntimeContext.class.getName());
////                LOG.debug("BindingRuntimeContext retrieved as {}", runtimeContext);
////                service.refresh(runtimeContext);
//                    }
//
//                    @Override
//                    public void removedService(ServiceReference<ActivationDriverBuilder> reference, ActivationDriverRepoService service) {
////                if(service == null) {
////                    return;
////                }
////
////                LOG.debug("Got removedService(SchemaContextProvider) event");
////                alreadyStarted.set(false);
////                configRegistryLookup.interrupt();
////                yangStoreServiceServiceRegistration.unregister();
////                yangStoreServiceServiceRegistration = null;
////            }
//                    }
//                };
//
//        ServiceTracker<ActivationDriverBuilder, ActivationDriverRepoService> activationDriverRepoServiceTracker =
//                new ServiceTracker<>(context, ActivationDriverBuilder.class, customizer);
//        activationDriverRepoServiceTracker.open();
//
//    }
//
//    @Override
//    public void stop(BundleContext context) throws Exception {
////        if(configRegistryLookup != null) {
////            configRegistryLookup.interrupt();
////        }
////        if(osgiRegistrayion != null) {
////            osgiRegistrayion.unregister();
////        }
////        if (yangStoreServiceServiceRegistration != null) {
////            yangStoreServiceServiceRegistration.unregister();
////            yangStoreServiceServiceRegistration = null;
////        }
//    }
//
//    /**
//     * Find ConfigRegistry from config manager in JMX
//     */
//    private class ConfigRegistryLookupThread extends Thread {
//        public static final int ATTEMPT_TIMEOUT_MS = 1000;
//        private static final int SILENT_ATTEMPTS = 30;
//
//        private final ActivationDriverRepoService activationDriverRepoService;
//
//        private ConfigRegistryLookupThread(ActivationDriverRepoService activationDriverRepoService) {
//            super("config-registry-lookup");
//            this.activationDriverRepoService = activationDriverRepoService;
//        }
//
//        @Override
//        public void run() {
//
////            ConfigRegistryJMXClient configRegistryJMXClient;
////            ConfigRegistryJMXClient configRegistryJMXClientNoNotifications;
////            int i = 0;
////            // Config registry might not be present yet, but will be eventually
////            while (true) {
////
////                try {
////                    configRegistryJMXClient = new ConfigRegistryJMXClient(configMBeanServer);
////                    configRegistryJMXClientNoNotifications = ConfigRegistryJMXClient.createWithoutNotifications(configMBeanServer);
////                    break;
////                } catch (IllegalStateException e) {
////                    ++i;
////                    if (i > SILENT_ATTEMPTS) {
////                        LOG.info("JMX client not created after {} attempts, still trying", i, e);
////                    } else {
////                        LOG.debug("JMX client could not be created, reattempting, try {}", i, e);
////                    }
////                    try {
////                        Thread.sleep(ATTEMPT_TIMEOUT_MS);
////                    } catch (InterruptedException e1) {
////                        Thread.currentThread().interrupt();
////                        throw new IllegalStateException("Interrupted while reattempting connection", e1);
////                    }
////                }
////            }
////
////            final ConfigRegistryJMXClient jmxClient = configRegistryJMXClient;
////            final ConfigRegistryJMXClient jmxClientNoNotifications = configRegistryJMXClientNoNotifications;
////            if (i > SILENT_ATTEMPTS) {
////                LOG.info("Created JMX client after {} attempts", i);
////            } else {
////                LOG.debug("Created JMX client after {} attempts", i);
////            }
////
////            final ConfigSubsystemFacadeFactory configSubsystemFacade =
////                    new ConfigSubsystemFacadeFactory(jmxClient, jmxClientNoNotifications, yangStoreService);
////            osgiRegistrayion = context.registerService(ConfigSubsystemFacadeFactory.class, configSubsystemFacade, new Hashtable<String, Object>());
//        }
//    }
}

