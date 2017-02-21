package org.opendaylight.unimgr.mef.notification.eventsource;

import org.opendaylight.controller.sal.core.api.AbstractProvider;
import org.opendaylight.controller.sal.core.api.Broker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BI Provider.
 */
public class BiEventSourceProvider extends AbstractProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(BiEventSourceProvider.class);

    @Override
    public void onSessionInitiated(Broker.ProviderSession session) {
        LOG.info("BiEventSourceProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("BiEventSourceProvider Closed");
    }
}
