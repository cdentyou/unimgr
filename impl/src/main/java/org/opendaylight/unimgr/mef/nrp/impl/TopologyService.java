/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.mef.nrp.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bartosz.michalik@amartus.com
 */
public class TopologyService {
    private static final Logger log = LoggerFactory.getLogger(TopologyService.class);
    private final DataBroker dataBroker;

    private static final String PRESTO_TOPO = "mef:presto-topology";

    public TopologyService(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() throws Exception {
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        CheckedFuture<? extends Optional<? extends DataObject>, ReadFailedException> result = tx.read(LogicalDatastoreType.OPERATIONAL, topo());

        Optional<? extends DataObject> topology = result.checkedGet();

        if(! topology.isPresent()) {
            log.info("initialize Presto topology");
            Topology topo = new TopologyBuilder()
                    .setTopologyId(new TopologyId(PRESTO_TOPO))
                    .build();
            tx.put(LogicalDatastoreType.OPERATIONAL, topo(), topo);
            try {
                tx.submit().checkedGet();
                log.debug("presto topology created");
            } catch (TransactionCommitFailedException e) {
                log.error("Failed to create presto topology");
                throw new IllegalStateException("cannot create presto topology", e);
            }
        }
    }

    private static InstanceIdentifier<Topology> topo() {
        return InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class,
                        new TopologyKey(new TopologyId(PRESTO_TOPO)));
    }

    public void close() {

    }
}
