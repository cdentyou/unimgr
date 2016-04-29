package org.mef.nrp.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.l2vpn.cfg.rev151109.l2vpn.AutoDiscoveryBuilder;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Remo service mockup - soon a real one
 *
 * @author alex.feigin@hpe.com
 */
public class ActivationDriverRepoService implements IActivationDriverRepoService, BindingAwareService {

    private static DataBroker dataBroker;
    private static MountPointService mountService;
//    private final L2vpnXconnectDriverBuilder builder;

    private static List<ActivationDriverBuilder> builders = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void bindBuilder(ActivationDriverBuilder builder) {
        builders.add(builder);
    }

    @Override
    public void unbindBuilder(ActivationDriverBuilder builder) {
        builders.remove(builder);
    }

    //    public static void initialize(BindingAwareBroker.ProviderContext session)  {
//
//        DataBroker dataBroker = session.getSALService(DataBroker.class);
//        MountPointService mountService = session.getSALService(MountPointService.class);
//
//        if(instance != null) throw new IllegalStateException("already initialized");
//        instance = new ActivationDriverRepoService(dataBroker, mountService);
//    }


    @Override
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
