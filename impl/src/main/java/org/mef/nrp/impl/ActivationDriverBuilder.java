package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

import java.util.Optional;

/**
 * Driver builder that can provide statefull driver that is used in trasaction
 * @author bartosz.michalik@amartus.com
 */
public interface ActivationDriverBuilder {
    /**
     *
     * @param port to configre
     * @param context (de)activation context
     * @return {@link Optional#empty()} in case it cannot be instantiated for a port, driver otherwise
     */
    Optional<ActivationDriver> driverFor(GFcPort port, GForwardingConstruct context);

}
