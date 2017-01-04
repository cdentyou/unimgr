package org.opendaylight.unimgr.mef.notification.es;

import org.opendaylight.controller.sal.core.api.AbstractProvider;
import org.opendaylight.controller.sal.core.api.Broker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author marek.ryznar@amartus.com
 */
public class EventSourceBIProvider extends AbstractProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(EventSourceBIProvider.class);

    @Override
    public void onSessionInitiated(Broker.ProviderSession session) {
        LOG.info("EventSourceBIProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("EventSourceBIProvider Closed");
    }
}
