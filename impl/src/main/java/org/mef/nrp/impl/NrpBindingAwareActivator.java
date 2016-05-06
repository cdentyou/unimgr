package org.mef.nrp.impl;

import org.opendaylight.controller.sal.binding.api.*;
import org.opendaylight.unimgr.impl.UnimgrProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * this activator enables us to aggregate implementations of
 *
 * @author alex.feigin@hpe.com
 * @author bartosz.michalik@amartus.com
 */
public class NrpBindingAwareActivator extends AbstractBrokerAwareActivator {
    private static final Logger LOG = LoggerFactory.getLogger(NrpBindingAwareActivator.class);
    private static final ActivationDriverRepoService activationDriverRepoService = new ActivationDriverRepoService();

    private volatile BindingAwareBroker broker;
    private ReentrantLock lock = new ReentrantLock();
    private ServiceTracker<ActivationDriverBuilder, ActivationDriverRepoService> tracker;
    private volatile BindingAwareBroker lastRemovedBroker;

    private HashSet<ServiceRegistration<ActivationDriverBuilder>> registeredDrivers = new HashSet<>();


    @Override
    protected void startImpl(BundleContext context) {
        if(tracker != null) {
            LOG.warn("NrpBindingAwareActivator.startImpl duplicated call  - ignoring");
            return;
        }
        lock.lock();
        try {
            LOG.info("NrpBindingAwareActivator.startImpl - initializing");
            tracker = prepareServiceTracker(context);
            tracker.open();
        } finally {
            lock.unlock();
        }

    }

    @Override
    protected void stopImpl(BundleContext context) {
        lock.lock();
        try {
            if(tracker != null) {
                tracker.close();
            }
            tracker = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void onBrokerAvailable(BindingAwareBroker broker, BundleContext context) {

        lock.lock();
        try {
            if(this.broker != null) {
                LOG.debug("NrpBindingAwareActivator.onBrokerAvailable - subsequent call - ignoring");
                return;
            }

            this.broker = broker;
            if(this.broker == lastRemovedBroker) {
                LOG.debug("NrpBindingAwareActivator.onBrokerAvailable - modified broker instance - ignoring");
            } else {

                unregisterDrivers(context);
            }
        } finally {
            lock.unlock();
        }


        LOG.info("NrpBindingAwareActivator.onBrokerAvailable start with {}", broker);

        UnimgrProvider.setActivationDriverRepoService(activationDriverRepoService);
        context.registerService(BindingAwareService.class, activationDriverRepoService, new Hashtable());
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable register ActivationDriverRepoService as an BindingAwareService");


        registerDrivers(context);

    }

    /**
     * registers driver services - TODO change to multi bundle implementation
     * @param context
     */
    protected void registerDrivers(BundleContext context) {
        L2vpnXconnectDriverBuilder l2vpnXconnectDriverBuilder = new L2vpnXconnectDriverBuilder();
        //TODO make it registered consumer in case dependencies should be injected
        broker.registerConsumer(l2vpnXconnectDriverBuilder);
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable register L2vpnXconnectDriverBuilder as a BindingAwareConsumer");

        L2vpnBridgeDriverBuilder l2vpnBridgeDriverBuilder = new L2vpnBridgeDriverBuilder();


        final ServiceRegistration<ActivationDriverBuilder> xconnectReq = context.registerService(ActivationDriverBuilder.class, l2vpnXconnectDriverBuilder, new Hashtable());
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable register L2vpnXconnectDriverBuilder as a ActivationDriverBuilder");
        final ServiceRegistration<ActivationDriverBuilder> xBridge = context.registerService(ActivationDriverBuilder.class, l2vpnBridgeDriverBuilder, new Hashtable());
        LOG.info("NrpBindingAwareActivator.onBrokerAvailable register L2vpnBridgeDriverBuilder as a ActivationDriverBuilder");

        registeredDrivers.add(xconnectReq);
        registeredDrivers.add(xBridge);
    }

    /**
     * XXX not thread safe by itself - refactor with caution
     * @param context
     */
    protected void unregisterDrivers(BundleContext context) {
        if(registeredDrivers.isEmpty()) return;

        LOG.info("NrpBindingAwareActivator.onBrokerAvailable - unregistering drivers");
        registeredDrivers.forEach(ServiceRegistration::unregister);
        registeredDrivers.clear();

    }


    @Override
    protected void onBrokerRemoved(BindingAwareBroker broker, BundleContext context) {
        lock.lock();
        this.broker = null;
        this.lastRemovedBroker = broker;
        lock.unlock();
    }

    protected ServiceTracker<ActivationDriverBuilder, ActivationDriverRepoService> prepareServiceTracker(BundleContext context) {
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

        ServiceTracker<ActivationDriverBuilder, ActivationDriverRepoService> activationDriverRepoServiceTracker =
                new ServiceTracker<>(context, ActivationDriverBuilder.class, customizer);
        return activationDriverRepoServiceTracker;
    }
}

