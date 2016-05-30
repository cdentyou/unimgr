package org.mef.nrp.impl;


import org.mef.nrp.api.*;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default application repo that is populated with the application driver builders registered as OSGi services.
 *
 *
 * @author alex.feigin@hpe.com
 * @author bartosz.michalik@amartus.com [modifications]
 */
public class ActivationDriverRepoServiceImpl implements ActivationDriverRepoService {
    private static final Logger LOG = LoggerFactory.getLogger(ActivationDriverRepoServiceImpl.class);

    private Collection<ActivationDriverBuilder> builders;

    public ActivationDriverRepoServiceImpl() {
        this.builders = Collections.emptyList();
    }

    /**
     * Used by blueprint to inject dynamic list of driver builders
     * @param builders list of service proxies
     */
    @SuppressWarnings("unused")
    public void setDriverBuilders(List<ActivationDriverBuilder> builders) {
        LOG.debug("Activation drivers initialized");
        this.builders = builders;
    }

    protected ActivationDriver getDriver(Function<ActivationDriverBuilder, Optional<ActivationDriver>> driver) {
        final List<ActivationDriver> drivers = builders.stream().map(driver)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (drivers.size() > 1) {
            throw new ActivationDriverAmbiguousException();
        }
        if (drivers.size() == 0) {
            throw new ActivationDriverNotFoundException();
        }
        return drivers.get(0);
    }

    public ActivationDriver getDriver(GFcPort aPort, GFcPort zPort, ActivationDriverBuilder.BuilderContext context) {
        return getDriver(x -> x.driverFor(aPort, zPort, context));
    }

    public ActivationDriver getDriver(GFcPort port, ActivationDriverBuilder.BuilderContext context) {
        return getDriver(x -> x.driverFor(port, context));
    }
}
