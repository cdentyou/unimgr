package org.mef.nrp.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;

/**
 * I am not going to synchronize this as it should disappear - the sooner the better
 * @author bartosz.michalik@amartus.com
 */
public class FakeActivationDriverRepo {

    private static FakeActivationDriverRepo instance;
    private final DataBroker dataBroker;
    private final MountPointService mountService;
    private final L2vpnXconnectDriverBuilder builder;

    public static void initialize(BindingAwareBroker.ProviderContext session)  {

        DataBroker dataBroker = session.getSALService(DataBroker.class);
        MountPointService mountService = session.getSALService(MountPointService.class);

        if(instance != null) throw new IllegalStateException("already initialized");
        instance = new FakeActivationDriverRepo(dataBroker, mountService);
    }


    public static FakeActivationDriverRepo getInstance()  {
        if(instance != null) throw new IllegalStateException("not initialized");
        return instance;
    }


    private FakeActivationDriverRepo(DataBroker dataBroker, MountPointService mountService) {
        this.dataBroker = dataBroker;
        this.mountService = mountService;
        // for the moment the only one
        builder = new L2vpnXconnectDriverBuilder(dataBroker, mountService);
    }


    public ActivationDriverBuilder getBuilder(GFcPort port) {
        return builder;
    }
}
