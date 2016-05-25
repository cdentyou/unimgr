package org.mef.nrp.api;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;

/**
 * Interface of components capable to deliver a driver for given port(s)
 */
public interface ActivationDriverRepoService {

    /**
     * Get driver for a port
     * @param port to
     * @param context
     * @return activation driver
     * @throws ActivationDriverAmbiguousException when multiple drivers declare they can configure port
     * @throws ActivationDriverNotFoundException when no driver found for port
     */
    ActivationDriver getDriver(GFcPort port, ActivationDriverBuilder.BuilderContext context);

    /**
     * Get driver for ports
     * @param aPort from port
     * @param zPort to port
     * @param context
     * @return activation driver
     * @throws ActivationDriverAmbiguousException when multiple drivers declare they can configure ports
     * @throws ActivationDriverNotFoundException when no driver found for ports
     */
    ActivationDriver getDriver(GFcPort aPort, GFcPort zPort, ActivationDriverBuilder.BuilderContext context);
}