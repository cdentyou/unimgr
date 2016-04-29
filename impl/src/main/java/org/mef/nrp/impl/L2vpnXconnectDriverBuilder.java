package org.mef.nrp.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

import java.util.Optional;

/**
 * Fake driver builder;
 * @author bartosz.michalik@amartus.com
 */
public class L2vpnXconnectDriverBuilder implements ActivationDriverBuilder, BindingAwareConsumer {

    private final FixedServiceNaming namingProvider;
    private L2vpnXconnectActivator xconnectActivator;
    private static DataBroker dataBroker;
    private static MountPointService mountService;


    @Override
    public void onSessionInitialized(BindingAwareBroker.ConsumerContext session) {
         dataBroker = session.getSALService(DataBroker.class);
         mountService = session.getSALService(MountPointService.class);
    }

    L2vpnXconnectDriverBuilder() {
        this.namingProvider = new FixedServiceNaming();
        xconnectActivator = new L2vpnXconnectActivator(dataBroker, mountService);
    }

    @Override
    public Optional<ActivationDriver> driverFor(GFcPort port,BuilderContext  context) {
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
                xconnectActivator.activate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);

            }

            @Override
            public void deactivate() throws Exception {
                String id = ctx.getUuid();
                long mtu = 1500;
                String outerName = namingProvider.getOuterName(id);
                String innerName = namingProvider.getInnerName(id);

                String aEndNodeName = aEnd.getLtpRefList().get(0).getValue().split(":")[0];
                xconnectActivator.deactivate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);
            }

            @Override
            public int priority() {
                return 0;
            }
        };

        return Optional.of(driver);
    }
}
