package org.mef.nrp.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 *
 * @author alex.feigin@hpe.com
 */
    public class ActivationDriverRepoService implements BindingAwareService {

    private static DataBroker dataBroker;
    private static MountPointService mountService;
//    private final L2vpnXconnectDriverBuilder builder;

    private static List<ActivationDriverBuilder> builders = Collections.synchronizedList(new ArrayList<>());

    public void bindBuilder(ActivationDriverBuilder builder) {
        builders.add(builder);
    }

    public void unbindBuilder(ActivationDriverBuilder builder) {
        builders.remove(builder);
    }

    public ActivationDriver getBuilder(GFcPort port, ActivationDriverBuilder.BuilderContext context) {
        Stream<ActivationDriver> s = Arrays.stream(builders.toArray(new ActivationDriverBuilder[0]))//
                .map(x -> x.driverFor(port, context))//
                .filter(x -> x.isPresent())//
                .map(x -> x.get());
        if (s.count() > 1) {
            throw new ActivationDriverAmbiguousException();
        }
        if (s.count() == 0) {
            throw new ActivationDriverNotFoundException();
        }
        return s.findFirst().get();
    }

}
