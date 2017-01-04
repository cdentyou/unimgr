package org.opendaylight.unimgr.mef.notification.es.example;

import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.unimgr.mef.notification.es.AbstractEventSource;
import org.opendaylight.unimgr.mef.notification.message.EventSourceMessenger;
import org.opendaylight.unimgr.mef.notification.message.MessageGenerator;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * First EventSource class, used for testing.
 *
 * @author marek.ryznar@amartus.com
 */
public class ExampleEventSource extends AbstractEventSource implements EventSourceMessenger {
    private static final Logger LOG = LoggerFactory.getLogger(ExampleEventSource.class);

    public ExampleEventSource(DOMNotificationPublishService domPublish, Node node){
        super(domPublish, node);
        LOG.info("ExampleEventSource constructor started.");

        messageText = "ExampleEventSource message";
        messageGeneratePeriod = 2;
        setAvailableNotifications();
        startMessageGenerator();
        LOG.info("ExampleEventSource constructor finished.");
    }

    private void startMessageGenerator(){
        // message generator is started as scheduled task
        messageGenerator = new MessageGenerator(sourceNode.getNodeId().getValue(), this.messageText,domPublish, this);
        scheduler.scheduleAtFixedRate(messageGenerator, 0, messageGeneratePeriod, TimeUnit.SECONDS);
    }

    /*
     * This method internally set list of SchemaPath(s) that represents all types of notification that event source can produce.
     * In actual implementation event source can set this list same way as this example code or it can obtain it from other sources
     * (e.g. configuration parameters, device capabilities etc.)
     */
    private void setAvailableNotifications(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(2015, 4, 8, 0, 0, 0);
        Date revisionDate = cal.getTime();

        URI uriSample = null;
        URI uriTest = null;
        URI uriOVS = null;
        try {
            uriSample = new URI("urn:opendaylight:unimgr:mef:notification:es:example:notification");
            uriTest = new URI("urn:opendaylight:unimgr:mef:notification:es:test:notification");
            uriOVS = new URI("ovs");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI for notification", e);
        }

        QName qnSample = QName.create(uriSample,revisionDate,"example-message");
        QName qnTest = QName.create(uriTest,revisionDate,"example-message");
        QName qnOvs = QName.create(uriOVS,revisionDate,"example-message");

        SchemaPath spSample = SchemaPath.create(true, qnSample);
        SchemaPath spTest = SchemaPath.create(true, qnTest);
        SchemaPath spOvs = SchemaPath.create(true, qnOvs);

        listSchemaPaths.add(spSample);
        listSchemaPaths.add(spTest);
        listSchemaPaths.add(spOvs);
    }

    @Override
    public String getMessage() {
        return messageText;
    }

    @Override
    public String getMessage(List<SchemaPath> schemaPaths) {
        return messageText;
    }

    public void setMessageGeneratePeriod(Short period){
        this.messageGeneratePeriod = period;
    }
}
