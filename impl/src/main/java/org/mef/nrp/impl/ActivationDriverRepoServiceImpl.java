package org.mef.nrp.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author alex.feigin@hpe.com
 * FIXME [bmi] reimplement as a singleton if we really need a single instance
 */
public class ActivationDriverRepoServiceImpl implements ActivationDriverRepoService {
    private static final Logger LOG = LoggerFactory.getLogger(ActivationDriverRepoServiceImpl.class);
    private static DataBroker dataBroker;
    private static MountPointService mountService;

    private static Collection<ActivationDriverBuilder> builders = ConcurrentHashMap.newKeySet();

    /* (non-Javadoc)
     * @see org.mef.nrp.impl.ActivationDriverRepoService#bindBuilder(org.mef.nrp.impl.ActivationDriverBuilder)
     */
    @Override
    public void bindBuilder(ActivationDriverBuilder builder) {
        if (builder == null) {
            return;
        }
        LOG.info("ActivationDriverRepoService.bindBuilder got [{}] instance", builder.getClass().getSimpleName());
        builders.add(builder);
    }

    /* (non-Javadoc)
     * @see org.mef.nrp.impl.ActivationDriverRepoService#unbindBuilder(org.mef.nrp.impl.ActivationDriverBuilder)
     */
    @Override
    public void unbindBuilder(ActivationDriverBuilder builder) {
        if (builder==null)
        {
            return;
        }
        LOG.info("ActivationDriverRepoService.unbindBuilder got [{}] instance", builder.getClass().getSimpleName());
        builders.remove(builder);
    }

    public ActivationDriver getBuilder(GFcPort port, ActivationDriverBuilder.BuilderContext context) {
        final List<ActivationDriver> drivers = builders.stream().map(x -> x.driverFor(port, context))
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
}
