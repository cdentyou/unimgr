package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;

public interface ActivationDriverRepoService {

    void bindBuilder(ActivationDriverBuilder builder);

    void unbindBuilder(ActivationDriverBuilder builder);

    public ActivationDriver getBuilder(GFcPort port, ActivationDriverBuilder.BuilderContext context);
}