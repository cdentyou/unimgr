package org.mef.nrp.impl;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730.InterfaceConfigurations;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730._interface.configurations.InterfaceConfiguration;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.ios.xr.ifmgr.cfg.rev150730._interface.configurations.InterfaceConfigurationKey;
import org.opendaylight.yang.gen.v1.http.cisco.com.ns.yang.cisco.xr.types.rev150629.InterfaceName;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.types.rev160229.Identifier45;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc._interface.rev160317.Interface1;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc._interface.rev160317.Interface1.FrameFormat;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc.service.rev160317.MefServices;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc.service.rev160317.mef.services.Uni;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc.service.rev160317.mef.services.uni.Evc;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc.service.rev160317.mef.services.uni.EvcBuilder;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc.service.rev160317.mef.services.uni.EvcKey;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc.service.rev160317.mef.services.uni.evc.EvcStatus;
import org.opendaylight.yang.gen.v1.http.www.microsemi.com.microsemi.edge.assure.msea.uni.evc.service.rev160317.mef.services.uni.evc.EvcStatusBuilder;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class EdgeAssureActivator implements ResourceActivator {

    private static final Logger log = LoggerFactory.getLogger(EdgeAssureActivator.class);
    private MountPointService mountService;
    DataBroker baseDataBroker;
    
    EdgeAssureActivator(DataBroker dataBroker, MountPointService mountService) {
        this.mountService = mountService;
        baseDataBroker = dataBroker;
    }
    

    @Override
    public void activate(String nodeName, String outerName, String innerName, GFcPort port, GFcPort neighbor, long mtu) {
		log.info("Activation called on EdgeAssureActivator");

		
    	String portLtpId = port.getLtpRefList().get(0).getValue();
    	String neighborLtpId = neighbor.getLtpRefList().get(0).getValue();

        String neighborHostname = neighborLtpId.split(":")[0];
        InterfaceName interfaceName = new InterfaceName(portLtpId.split(":")[1]);

		long evcId = 1;
		
		EvcBuilder evcBuilder = new EvcBuilder();
		evcBuilder.setEvcIndex(evcId).setName(new Identifier45("evc"+String.valueOf(evcId)));
        List<Evc> evcConfigs = new LinkedList<>();
        evcConfigs.add(evcBuilder.build());

        InstanceIdentifier<Evc> evcConfigId =
        		InstanceIdentifier.builder(MefServices.class)
        		.child(Uni.class)
                .child(Evc.class, new EvcKey(evcId))
                .build();
		
        DataBroker netconfDataBroker = getNodeDataBroker(nodeName);
        
        WriteTransaction w = netconfDataBroker.newWriteOnlyTransaction();
        w.merge(LogicalDatastoreType.CONFIGURATION, evcConfigId, evcBuilder.build());

	}

    @Override
    public void deactivate(String nodeName, String outerName, String innerName, GFcPort port, GFcPort neighbor, long mtu) {
		log.info("Deactivation called on EdgeAssureActivator. Not yet implemented.");

	}	
	
	/*
	 * Find the instance of EdgeAssure by name where it is mounted by NETCONF
	 * Client Connector as a mount point
	 * e.g http://localhost:8080/restconf/config/network-topology:network-topology/topology/topology-netconf/node/edgeassure1/yang-ext:mount/ 
	 */
    private DataBroker getNodeDataBroker(String nodeName) {
        NodeId nodeId = new NodeId(nodeName);

        InstanceIdentifier<Node> nodeInstanceId = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())))
                .child(Node.class, new NodeKey(nodeId))
                .build();

        final Optional<MountPoint> nodeOptional = mountService.getMountPoint(nodeInstanceId);

        if (!nodeOptional.isPresent()) {
            return null;
        }

        MountPoint nodeMountPoint = nodeOptional.get();
        return nodeMountPoint.getService(DataBroker.class).get();
    }
}
