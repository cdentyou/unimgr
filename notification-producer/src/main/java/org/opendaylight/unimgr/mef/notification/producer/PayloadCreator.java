package org.opendaylight.unimgr.mef.notification.producer;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by root on 22.02.17.
 */
public class PayloadCreator {

    public static Node prepareTestNode(String nodeId){
        List<TerminationPoint> tps = new LinkedList<>();
        TerminationPoint terminationPoint = buildTerminationPoint("TestTpId","TestTpKey");
        TerminationPoint terminationPoint2 = buildTerminationPoint("TestTpId2","TestTpKey2");
        TerminationPoint terminationPoint3 = buildTerminationPoint("TestTpId3","TestTpKey3");
        tps.add(terminationPoint);
        tps.add(terminationPoint2);
        tps.add(terminationPoint3);

        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setNodeId(new NodeId(nodeId));
        nodeBuilder.setTerminationPoint(tps);
        return nodeBuilder.build();
    }

    private static TerminationPoint buildTerminationPoint(String tpId, String tpKey){
        TerminationPointBuilder terminationPointBuilder = new TerminationPointBuilder();
        terminationPointBuilder.setTpId(new TpId(tpId));
        terminationPointBuilder.setKey(new TerminationPointKey(new TpId(tpKey)));
        TerminationPoint terminationPoint = terminationPointBuilder.build();
        return terminationPoint;
    }

    public static LeafNode<String> prepareTestLeafNode(){
        QName ipAddressQname = QName.create(Node.QNAME, "ip-address");
        String ipAddress = "192.168.1.1";
        LeafNode<String> nodeIpValue = ImmutableNodes.leafNode(ipAddressQname, ipAddress);
        return nodeIpValue;
    }
}
