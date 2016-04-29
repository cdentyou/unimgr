package org.mef.nrp.impl;

import org.opendaylight.controller.sal.binding.api.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
    private static final ActivationDriverRepoService activationDriverRepoService = new ActivationDriverRepoService();
    @Override
    protected void onBrokerAvailable(BindingAwareBroker broker, BundleContext context) {
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable start");

        L2vpnXconnectDriverBuilder l2vpnXconnectDriverBuilder = new L2vpnXconnectDriverBuilder();
        //TODO make it registered consumer in case dependencies should be injected
        L2vpnBridgeDriverBuilder l2vpnBridgeDriverBuilder = new L2vpnBridgeDriverBuilder();
        broker.registerConsumer(l2vpnXconnectDriverBuilder);
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable register L2vpnXconnectDriverBuilder as a BindingAwareConsumer");

        ServiceTrackerCustomizer<ActivationDriverBuilder, ActivationDriverRepoService> customizer = //
                new ServiceTrackerCustomizer<ActivationDriverBuilder, ActivationDriverRepoService>() {
                    @Override
                    public ActivationDriverRepoService addingService(ServiceReference<ActivationDriverBuilder> reference) {

                        ActivationDriverBuilder activationDriverBuilder = reference.getBundle().getBundleContext().getService(reference);
                        activationDriverRepoService.bindBuilder(activationDriverBuilder);
                        return activationDriverRepoService;
                    }

                    @Override
                    public void modifiedService(ServiceReference<ActivationDriverBuilder> reference, ActivationDriverRepoService service) {
                        // TODO: workout how to dump the old version
                        ActivationDriverBuilder activationDriverBuilder = reference.getBundle().getBundleContext().getService(reference);
                        activationDriverRepoService.bindBuilder(activationDriverBuilder);
                    }

                    @Override
                    public void removedService(ServiceReference<ActivationDriverBuilder> reference, ActivationDriverRepoService service) {
                        ActivationDriverBuilder activationDriverBuilder = reference.getBundle().getBundleContext().getService(reference);
                        activationDriverRepoService.unbindBuilder(activationDriverBuilder);
                    }
                };


        context.registerService(ActivationDriverRepoService.class, activationDriverRepoService, new Hashtable());
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable register ActivationDriverRepoService as an ActivationDriverRepoService");

        ServiceTracker<ActivationDriverBuilder, ActivationDriverRepoService> activationDriverRepoServiceTracker =
                new ServiceTracker<>(context, ActivationDriverBuilder.class, customizer);
        activationDriverRepoServiceTracker.open();
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable started tracking ActivationDriverBuilder services");
        context.registerService(ActivationDriverRepoService.class, activationDriverRepoService, new Hashtable());
        context.registerService(BindingAwareService.class, activationDriverRepoService, new Hashtable());
        context.registerService(ActivationDriverBuilder.class,l2vpnXconnectDriverBuilder, new Hashtable());
        context.registerService(ActivationDriverBuilder.class,l2vpnBridgeDriverBuilder, new Hashtable());
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable register L2vpnXconnectDriverBuilder as a ActivationDriverBuilder");
    }
}

