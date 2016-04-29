package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

import java.util.Optional;

/**
 * Driver builder that can provide stateful driver that are used in NRP forwarding construct transaction
 * @author bartosz.michalik@amartus.com
 */
public interface ActivationDriverBuilder {
    /**
     * Method is meant to prepare a driver for a given port.
     * @param port to configure
     * @param context (de)activation context
     * @return {@link Optional#empty()} in case it cannot be instantiated for a port, driver otherwise
     */
    Optional<ActivationDriver> driverFor(GFcPort port, GForwardingConstruct context);

}
