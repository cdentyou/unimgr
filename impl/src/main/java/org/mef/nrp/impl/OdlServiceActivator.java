package org.mef.nrp.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.fcroutelist.FcRoute;

public class OdlServiceActivator implements ServiceActivator {

    private DataBroker dataBroker;
    private MountPointService mountService;
    private ServiceNaming namingProvider = new FixedServiceNaming();

    public OdlServiceActivator(DataBroker dataBroker, MountPointService mountService) {
        this.dataBroker = dataBroker;
        this.mountService = mountService;
    }

    @Override
    public void activate(FcRoute fcRoute, ResourceActivator resourceActivator) {

        String id = fcRoute.getId();
        long mtu = 1500;
        String outerName = namingProvider.getOuterName(id);
        String innerName = namingProvider.getInnerName(id);

        GFcPort aEnd = fcRoute.getForwardingConstruct().get(0).getFcPort().get(0);
        GFcPort zEnd = fcRoute.getForwardingConstruct().get(0).getFcPort().get(1);

        String aEndNodeName = aEnd.getLtpRefList().get(0).getValue().split(":")[0];
        resourceActivator.activate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);

        String zEndNodeName = zEnd.getLtpRefList().get(0).getValue().split(":")[0];
        resourceActivator.activate(zEndNodeName, outerName, innerName, zEnd, aEnd, mtu);
    }

    @Override
    public void deactivate(FcRoute fcRoute, ResourceActivator resourceActivator) {

        String id = fcRoute.getId();
        long mtu = 1500;
        String outerName = namingProvider.getOuterName(id);
        String innerName = namingProvider.getInnerName(id);

        GFcPort aEnd = fcRoute.getForwardingConstruct().get(0).getFcPort().get(0);
        GFcPort zEnd = fcRoute.getForwardingConstruct().get(0).getFcPort().get(1);

        String aEndNodeName = aEnd.getLtpRefList().get(0).getValue().split(":")[0];
        resourceActivator.deactivate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);

        String zEndNodeName = zEnd.getLtpRefList().get(0).getValue().split(":")[0];
        resourceActivator.deactivate(zEndNodeName, outerName, innerName, zEnd, aEnd, mtu);
    }

}
