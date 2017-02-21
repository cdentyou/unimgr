package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by root on 14.02.17.
 */
public class BaEventSourceProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(BaEventSourceProvider.class);

    @Override
    public void close() throws Exception {
        LOG.info("EventsourceBAProvider Closed");
    }

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext providerContext) {
        LOG.info("EventsourceBAProvider Session Initiated");
    }
}
