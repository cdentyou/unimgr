package org.mef.nrp.cisco.xr;

import org.mef.nrp.impl.ActivationDriverBuilder;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

public class CiscoXRDriverProvider implements BindingAwareProvider, AutoCloseable {


    private ServiceRegistration<ActivationDriverBuilder> xconnectReg;
    private ServiceRegistration<ActivationDriverBuilder> bridgeReg;

    public CiscoXRDriverProvider() {


    }

    @Override
    public void onSessionInitiated(ProviderContext session) {

        final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

        L2vpnXconnectDriverBuilder l2vpnXconnectDriverBuilder = new L2vpnXconnectDriverBuilder();
        l2vpnXconnectDriverBuilder.onSessionInitialized(session);
        xconnectReg = context.registerService(ActivationDriverBuilder.class, l2vpnXconnectDriverBuilder, null);

        L2vpnBridgeDriverBuilder l2vpnBridgeDriverBuilder = new L2vpnBridgeDriverBuilder();
        l2vpnBridgeDriverBuilder.onSessionInitialized(session);
        bridgeReg = context.registerService(ActivationDriverBuilder.class, l2vpnBridgeDriverBuilder, null);
    }

    @Override
    public void close() throws Exception {
        if(bridgeReg != null) {
            bridgeReg.unregister();
        }
        if(xconnectReg != null) {
            xconnectReg.unregister();
        }
    }
}
