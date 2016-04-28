package org.mef.nrp.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MefNrpProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(MefNrpProvider.class);

    private DataBroker dataBroker;
    private MountPointService mountService;
    private ServiceEngine serviceEngine;

    private ListenerRegistration<DataChangeListener> topoRegistration;
    private ListenerRegistration<DataChangeListener> scaModelRegistration;

    public MefNrpProvider(DataBroker broker) {
        dataBroker = broker;
    }

    @Override
    public void close() throws Exception {
        topoRegistration.close();
        scaModelRegistration.close();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        mountService = session.getSALService(MountPointService.class);
        serviceEngine = new ServiceEngine(dataBroker, mountService);

        InstanceIdentifier<Topology> idTopo = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName()))).build();
//        topoRegistration = dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
//                idTopo.child(Node.class), new NetconfTopologyChangeListener(dataBroker, mountService),
//                DataChangeScope.SUBTREE);

    }
}
