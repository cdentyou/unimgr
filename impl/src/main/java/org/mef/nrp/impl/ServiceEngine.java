package org.mef.nrp.impl;

import java.util.Map;
import java.util.TreeMap;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.fcroutelist.FcRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceEngine {

    private static final Logger log = LoggerFactory.getLogger(ServiceEngine.class);

    protected DataBroker dataBroker;
    protected MountPointService mountService;

    String serviceDriver = "OdlServiceActivator";
    String activationDriver = "L2vpnXconnectActivator";

    Map<String, ServiceActivator> serviceActivators =
            new TreeMap<String, ServiceActivator>();
    Map<String, ResourceActivator> resourceActivators =
            new TreeMap<String, ResourceActivator>();

    public ServiceEngine(DataBroker dataBroker, MountPointService mountService) {
        this.dataBroker = dataBroker;
        this.mountService = mountService;

        serviceActivators.put(serviceDriver, new OdlServiceActivator(dataBroker, mountService));
        resourceActivators.put(activationDriver, new L2vpnXconnectActivator(dataBroker, mountService));
    }

    public void handleServiceChange(FcRoute before, FcRoute after) {

        ServiceActivator serviceActivator = serviceActivators.get(serviceDriver);
        ResourceActivator resourceActivator = resourceActivators.get(activationDriver);

        if (before != null) {
            serviceActivator.deactivate(before, resourceActivator);
        }

        if (after != null) {
            serviceActivator.activate(after, resourceActivator);
        }
    }
}
