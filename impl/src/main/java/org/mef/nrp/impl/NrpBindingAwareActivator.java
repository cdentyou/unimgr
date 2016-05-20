package org.mef.nrp.impl;

import org.opendaylight.controller.sal.binding.api.AbstractBrokerAwareActivator;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.osgi.framework.BundleContext;

/**
 * this activator enables us to aggregate implementations of
 *
 * @author alex.feigin@hpe.com
 * @author bartosz.michalik@amartus.com
 */
public class NrpBindingAwareActivator extends AbstractBrokerAwareActivator {

    @Override
    protected void onBrokerAvailable(BindingAwareBroker broker, BundleContext context) {
    }

    @Override
    protected void onBrokerRemoved(BindingAwareBroker broker, BundleContext context) {
    }

}

