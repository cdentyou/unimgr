package org.mef.nrp.impl;

import java.util.Optional;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

/**
 * Provides drivers for binding two ports on the same node.
 * @author bartosz.michalik@amartus.com
 */
public class L2vpnBridgeDriverBuilder implements ActivationDriverBuilder {
	
	private L2vpnBridgeActivator activator;

    @Override
    public Optional<ActivationDriver> driverFor(GFcPort port, BuilderContext ctx) {
        Optional<GForwardingConstruct> fwd = ctx.get(GForwardingConstruct.class.getName());
        assert fwd != null;

        if(isTheSameNode(fwd.get())) {
            Optional<ActivationDriver> driver= ctx.get("L2vpnBridgeDriverBuilder.driver");
            if(driver.isPresent()) return Optional.of(new DummyActivationDriver());
            ActivationDriver realDriver =  getDriver(port, ctx);
            assert realDriver != null;

            ctx.put("L2vpnBridgeDriverBuilder.driver", realDriver);
            return Optional.of(realDriver);

        }

        return Optional.empty();
    }

    private boolean isTheSameNode(GForwardingConstruct forwardingConstruct) {
    	String aHost = host(ltp(forwardingConstruct, 0));
    	String zHost = host(ltp(forwardingConstruct, 1));

        return aHost != null && zHost != null && aHost.equals(zHost);
    }

    protected ActivationDriver getDriver(GFcPort port, BuilderContext ctx) {
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
                String outerName = "outer";
                String innerName = "inner";

                String aEndNodeName = aEnd.getLtpRefList().get(0).getValue().split(":")[0];
                activator.activate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);

            }

            @Override
            public void deactivate() throws Exception {
                String id = ctx.getUuid();
                long mtu = 1500;
                String outerName = "outer";
                String innerName = "inner";

                String aEndNodeName = aEnd.getLtpRefList().get(0).getValue().split(":")[0];
                activator.deactivate(aEndNodeName, outerName, innerName, aEnd, zEnd, mtu);
            }

            @Override
            public int priority() {
                return 0;
            }
        };
        return driver;
    }
        
    
    public static String ltp(GForwardingConstruct fc, int port) {
    	return fc.getFcPort().get(port).getLtpRefList().get(0).getValue();
    }
    
    public static String host(String ltp) {
    	return ltp.split(":")[0];
    }
}