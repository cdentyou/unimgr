package org.opendaylight.unimgr.mef.notification.es.ovs;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMNotificationPublishService;
import org.opendaylight.unimgr.mef.notification.es.AbstractEventSource;
import org.opendaylight.unimgr.mef.notification.impl.TopologyTransaction;
import org.opendaylight.unimgr.mef.notification.message.EventSourceMessenger;
import org.opendaylight.unimgr.mef.notification.message.MessageGenerator;
import org.opendaylight.unimgr.mef.nrp.common.ResourceNotAvailableException;
import org.opendaylight.yang.gen.v1.urn.onf.core.network.module.rev160630.g_forwardingconstruct.FcPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
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
 * Class responsible for sending notifications about Open vSwitch devices.
 * Contain two kinds of notification:
 *  - OpenFlow
 *  - OVSDB
 *
 * @author marek.ryznar@amartus.com
 */
public class OvsEventSource extends AbstractEventSource implements EventSourceMessenger {

    private static final Logger LOG = LoggerFactory.getLogger(OvsEventSource.class);
    private SchemaPath spOvsdb;
    private SchemaPath spOpenFlow;
    private TopologyTransaction topologyTransaction;
    private FcPort fcPort;

    public OvsEventSource(DOMNotificationPublishService domPublish, Node node) {
        super(domPublish, node);
        this.messageGeneratePeriod = 5;
        LOG.info("OvsEventSource has started!");
        setAvailableNotifications();
        startMessageGenerator();
    }

    private void setAvailableNotifications(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(2015, 4, 8, 0, 0, 0);
        Date revisionDate = cal.getTime();

        URI uriOvsdb;
        URI uriOpenFlow;
        try {
            uriOvsdb = new URI("ovsdb");
            uriOpenFlow = new URI("openflow");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI for notification", e);
        }

        QName qnOvsdb = QName.create(uriOvsdb,revisionDate,"ovsdb-message");
        QName qnOpenFlow = QName.create(uriOpenFlow,revisionDate,"openflow-message");

        spOvsdb = SchemaPath.create(true, qnOvsdb);
        spOpenFlow = SchemaPath.create(true, qnOpenFlow);

        listSchemaPaths.add(spOvsdb);
        listSchemaPaths.add(spOpenFlow);
    }

    private void startMessageGenerator(){
        messageText ="Ovs message text";
        messageGenerator = new MessageGenerator(sourceNode.getNodeId().getValue(), messageText,domPublish, this);
        scheduler.scheduleAtFixedRate(messageGenerator, 0, messageGeneratePeriod, TimeUnit.SECONDS);
    }

    @Override
    public String getMessage() {
        return messageText;
    }

    /**
     * Depending on given notification(s), message will include information about OpenFlow or OVSDB Operational DataStore.
     *
     * @param schemaPaths Indicate topic that message will be send to.
     * @return message
     */
    @Override
    public String getMessage(List<SchemaPath> schemaPaths) {
        StringBuilder message = new StringBuilder();
        LOG.info("OvsEventSource getMessage schemaPatch : {}",schemaPaths.toString());
        message.append("OVS node: ");
        message.append(sourceNode.getKey().getNodeId().getValue());
        message.append("\n");

        for(SchemaPath schemaPath : schemaPaths){
            if(schemaPath.equals(spOpenFlow)){
                LOG.info("spOpenFlow matched!");
                message.append(getOpenFlowTable());
            }
            else if(schemaPath.equals(spOvsdb)){
                LOG.info("spOvsdb matched!");
                message.append(getOvsdbNode());
            }
        }
        return message.toString();
    }

    private String getOpenFlowTable(){
        String portName = fcPort.getTp().getValue();
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("OpenFlow data: ");
        try {
            org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node node = topologyTransaction.readNode(portName);
            Table table = OvsUtil.getTable(node);
            messageBuilder.append(OvsUtil.tableToString(table));
        } catch (ResourceNotAvailableException e) {
            e.printStackTrace();
        }
        messageBuilder.append("\n");
        return messageBuilder.toString();
    }

    private String getOvsdbNode(){
        String message = "  OVSDB data:(TODO) \n";

        Optional<Node> node = topologyTransaction.getOVSDBNode(sourceNode.getKey().getNodeId());
        if(node.isPresent()){
            //output is pretty messy this way, but it will be send as a object or will be parsed to json
            message = node.get().toString();
        }

        return message;
    }

    public void setTopologyTransaction(DataBroker dataBroker){
        topologyTransaction = new TopologyTransaction(dataBroker);
    }

    public void setFcPort(FcPort fcPort) {
        this.fcPort = fcPort;
    }
}
