package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.fcroutelist.FcRoute;

public interface ServiceActivator {

    public void activate(FcRoute fcRoute, ResourceActivator resourceActivator);

    public void deactivate(FcRoute fcRoute, ResourceActivator resourceActivator);
}
