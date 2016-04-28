package org.opendaylight.unimgr.impl;

import org.mef.nrp.impl.ActivationDriver;
import org.mef.nrp.impl.ActivationDriverBuilder;
import org.mef.nrp.impl.ActivationTransaction;
import org.mef.nrp.impl.FakeActivationDriverRepo;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.fcroutelist.FcRoute;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.g_forwardingconstruct.FcPort;


import java.util.List;
/**
 * @author bartosz.michalik@amartus.com
 */
public class FcRouteActivatorService {
    public FcRouteActivatorService(DataBroker dataBroker) {

    }

    public void activate(FcRoute route) {
        for(GForwardingConstruct fwdC : route.getForwardingConstruct()) {
            ActivationTransaction tx = prepareTransaction(fwdC);
            tx.activate();
        }
    }

    public void deactivate(FcRoute route) {
        for(GForwardingConstruct fwdC : route.getForwardingConstruct()) {
            ActivationTransaction tx = prepareTransaction(fwdC);
            tx.deactivate();
        }
    }

    private ActivationTransaction prepareTransaction(GForwardingConstruct fwdC) {
        final List<FcPort> list = fwdC.getFcPort();
        //TODO validate pre-condition
        final GFcPort a = list.get(0);
        final GFcPort z = list.get(1);

        //1. find and initialize drivers
        ActivationDriver aActivator = null;
        try {
            aActivator = findDriver(a, fwdC);
            aActivator.initialize(a,z, fwdC);
        } catch (Exception e) {
            //LOG problem with driver for a
            return null; //???
        }
        ActivationDriver zActivator = null;
        try {
            zActivator = findDriver(z, fwdC);
            zActivator.initialize(z,a, fwdC);
        } catch (Exception e) {
            //LOG problem with driver for a
            return null; //???
        }

        final ActivationTransaction tx = new ActivationTransaction();
        tx.addDriver(aActivator);
        tx.addDriver(zActivator);

        return tx;
    }

    /***
     *
     * @param port
     * @param fwdC
     * @return
     * @throws Exception when there is more than one driver
     */
    private ActivationDriver findDriver(GFcPort port, GForwardingConstruct fwdC) throws Exception {
        final FakeActivationDriverRepo driverRepo = FakeActivationDriverRepo.getInstance();

        final ActivationDriverBuilder builder = driverRepo.getBuilder(port);
        //FIXME all fake :D
        return builder.driverFor(port, fwdC).get();
    }

    static final class Context {
        final GFcPort a;
        final GFcPort z;
        final GForwardingConstruct fwC;

        public Context(GFcPort a, GFcPort z, GForwardingConstruct fwC) {
            this.a = a;
            this.z = z;
            this.fwC = fwC;
        }
    }
}
