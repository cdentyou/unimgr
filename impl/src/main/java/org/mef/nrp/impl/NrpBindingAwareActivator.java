package org.mef.nrp.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.sal.binding.api.*;
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
    private static final ActivationDriverRepoService activationDriverRepoService = new ActivationDriverRepoService();

    @Override
    protected void onBrokerAvailable(BindingAwareBroker broker, BundleContext context) {
        L2vpnXconnectDriverBuilder l2vpnXconnectDriverBuilder = new L2vpnXconnectDriverBuilder();
        broker.registerConsumer(l2vpnXconnectDriverBuilder);

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
        activationDriverRepoServiceTracker.open();
        context.registerService(ActivationDriverRepoService.class, activationDriverRepoService, new Hashtable());
        context.registerService(BindingAwareService.class, activationDriverRepoService, new Hashtable());
        context.registerService(ActivationDriverBuilder.class,l2vpnXconnectDriverBuilder, new Hashtable());

    }
}
