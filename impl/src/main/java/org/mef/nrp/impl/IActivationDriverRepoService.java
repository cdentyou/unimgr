package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

/**
 * Created by mef on 29/04/16.
 */
public interface IActivationDriverRepoService {
    void bindBuilder(ActivationDriverBuilder builder);

    void unbindBuilder(ActivationDriverBuilder builder);

    ActivationDriver getBuilder(GFcPort port, ActivationDriverBuilder.BuilderContext context);
}
