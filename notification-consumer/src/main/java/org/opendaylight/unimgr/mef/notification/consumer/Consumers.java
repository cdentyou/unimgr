package org.opendaylight.unimgr.mef.notification.consumer;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by root on 22.02.17.
 */
public class Consumers {
    private static final Logger LOG = LoggerFactory.getLogger(Consumers.class);

    public static class NoopDomConsumer implements Consumer{

        @Override
        public void onSessionInitiated(Broker.ConsumerSession consumerSession) {
            LOG.info("NoopDomConsumer initialized");
        }

        @Override
        public Collection<ConsumerFunctionality> getConsumerFunctionality() {
            return Collections.emptySet();
        }
    }

    public static class NoopBaConsumer implements BindingAwareConsumer {

        @Override
        public void onSessionInitialized(BindingAwareBroker.ConsumerContext consumerContext) {
            LOG.info("NoopBaConsumer initialized");
        }
    }
}
