package org.opendaylight.unimgr.mef.notification.listener;

import org.opendaylight.controller.md.sal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eventsource.user.agent.topic.rev150408.ReadTopicInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

import javax.annotation.Nonnull;
import java.util.concurrent.Future;

/**
 * Created by root on 05.01.17.
 */
public class OvsEventSourceListener implements EventSourceListener {

    @Override
    public void close() throws Exception {

    }

    @Override
    public void onNotification(@Nonnull DOMNotification domNotification) {

    }

    @Override
    public Future<RpcResult<Void>> readTopic(ReadTopicInput input) {
        return null;
    }
}
