package org.mef.nrp.impl;

import org.mef.nrp.api.ActivationDriver;
import org.mef.nrp.api.ActivationDriverBuilder;
import org.mef.nrp.common.FixedServiceNaming;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

import java.util.Optional;

/**
 * Fake driver builder;
 * @author sean.condon@microsemi.com
 */
public class EdgeAssureDriverBuilder implements ActivationDriverBuilder {

    private final FixedServiceNaming namingProvider;
    private final EdgeAssureActivator edgeAssureActivator;

    EdgeAssureDriverBuilder(DataBroker dataBroker, MountPointService mountService) {
        this.namingProvider = new FixedServiceNaming();
        edgeAssureActivator = new EdgeAssureActivator(dataBroker, mountService);
    }

    @Override
    public Optional<ActivationDriver> driverFor(GFcPort port, BuilderContext context) {
        final ActivationDriver driver = new ActivationDriver() {
            public GForwardingConstruct ctx;
            public GFcPort aEnd;
            public GFcPort zEnd;

            @Override
            public void commit() {
                //ignore for the moment
            }

            @Override
            public void rollback() {
                //ignore for the moment
            }

            @Override
            public void initialize(GFcPort from, GFcPort to, GForwardingConstruct ctx) throws Exception {
                this.zEnd = to;
                this.aEnd = from;
                this.ctx = ctx;
            }

            @Override
            public void activate() throws Exception {
                String id = ctx.getUuid();
                long mtu = 1500;
                String outerName = namingProvider.getOuterName(id);
                String innerName = namingProvider.getInnerName(id);

                String aEndNodeName = aEnd.getLtpRefList().get(0).getValue().split(":")[0];
                edgeAssureActivator.activate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);

            }

            @Override
            public void deactivate() throws Exception {
                String id = ctx.getUuid();
                long mtu = 1500;
                String outerName = namingProvider.getOuterName(id);
                String innerName = namingProvider.getInnerName(id);

                String aEndNodeName = aEnd.getLtpRefList().get(0).getValue().split(":")[0];
                edgeAssureActivator.deactivate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);
            }

            @Override
            public int priority() {
                return 0;
            }
        };

        return Optional.of(driver);
    }

    @Override
    public Optional<ActivationDriver> driverFor(GFcPort aPort, GFcPort zPort, BuilderContext context) {
        return Optional.empty();
    }
}
