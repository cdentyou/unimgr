package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.forwardingconstructlist.ForwardingConstruct;

import java.util.Optional;

/**
 * Provides drivers for binding two ports on the same node.
 * @author bartosz.michalik@amartus.com
 */
public class L2vpnBridgeDriverBuilder implements ActivationDriverBuilder {
    @Override
    public Optional<ActivationDriver> driverFor(GFcPort port, BuilderContext ctx) {
        Optional<ForwardingConstruct> fwd = ctx.get(ForwardingConstruct.class.getName());
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

    private boolean isTheSameNode(ForwardingConstruct forwardingConstruct) {
        // check if a and z are on the same node
        return false;
    }

    protected ActivationDriver getDriver(GFcPort port, BuilderContext ctx) {
        //FIXME provide driver
        return null;
    }
}
