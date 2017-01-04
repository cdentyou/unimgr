package org.opendaylight.unimgr.mef.notification.es.ovs;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;

/**
 * Class created to ease getting matching rules from OpenFlow flows and to add it to notification message.
 * For now 5 most popular types of Match are checked, but all of them should be checked.
 *
 * @author marek.ryznar@amartus.com
 */
public class MatchUtil {

    private static final String INDENT = "\n      ";
    private static final String SUBINDENT = "\n        ";

    public static void getMatch(Match match, StringBuilder message){
        getInPort(match.getInPort(),message,"Virtual Port: ");
        getInPort(match.getInPhyPort(),message,"Physical port: ");
        getEthernetMatch(match.getEthernetMatch(),message);
        getIpMatch(match.getIpMatch(),message);
        getVlanMatch(match.getVlanMatch(),message);
    }

    protected static void getInPort(NodeConnectorId nodeConnectorId, StringBuilder message,String portKind){
        if(nodeConnectorId != null){
            message.append(INDENT);
            message.append(portKind);
            message.append(nodeConnectorId.getValue());
        }
    }

    protected static void getEthernetMatch(EthernetMatch ethernetMatch, StringBuilder message){
        if(ethernetMatch != null) {
            message.append(INDENT);
            message.append("Ethernet:");
            if (ethernetMatch.getEthernetType() != null) {
                message.append(SUBINDENT);
                message.append("Type: ");
                message.append(ethernetMatch.getEthernetType().getType().getValue());
            }
            if (ethernetMatch.getEthernetSource() != null) {
                message.append(SUBINDENT);
                message.append("Source: ");
                message.append(ethernetMatch.getEthernetSource().getAddress().getValue());
            }
            if (ethernetMatch.getEthernetDestination() != null) {
                message.append(SUBINDENT);
                message.append("Destination: ");
                message.append(ethernetMatch.getEthernetDestination().getAddress().getValue());
            }
        }
    }

    protected static void getIpMatch(IpMatch ipMatch, StringBuilder message){
        if(ipMatch != null){
            message.append(INDENT);
            message.append("IP:");
            if(ipMatch.getIpProtocol() != null){
                message.append(SUBINDENT);
                message.append("Protocol: ");
                message.append(ipMatch.getIpProtocol());
            }
            if(ipMatch.getIpProto() != null){
                message.append(SUBINDENT);
                message.append("Version: ");
                message.append(ipMatch.getIpProto().getName());
            }
            if(ipMatch.getIpDscp() != null){
                message.append(SUBINDENT);
                message.append("dscp: ");
                message.append(ipMatch.getIpDscp().getValue());
            }
            if(ipMatch.getIpEcn() != null){
                message.append(SUBINDENT);
                message.append("ecn: ");
                message.append(ipMatch.getIpEcn());
            }
        }
    }

    protected static void getVlanMatch(VlanMatch vlanMatch, StringBuilder message){
        if(vlanMatch != null){
            message.append(INDENT);
            message.append("VLAN:");
            if(vlanMatch.getVlanId() != null){
                message.append(SUBINDENT);
                message.append("VlanID: ");
                message.append(vlanMatch.getVlanId().getVlanId().getValue());
            }
            if(vlanMatch.getVlanPcp() != null){
                message.append(SUBINDENT);
                message.append("PCP: ");
                message.append(vlanMatch.getVlanPcp().getValue());
            }
        }
    }
}
