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

import org.opendaylight.unimgr.mef.nrp.api.TapiConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.Network;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.NetworkBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.NetworkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.NetworkKey;
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


    public TopologyService(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() throws Exception {
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        CheckedFuture<? extends Optional<? extends DataObject>, ReadFailedException> result = tx.read(LogicalDatastoreType.OPERATIONAL, topo());

        Optional<? extends DataObject> topology = result.checkedGet();

        if(! topology.isPresent()) {
            log.info("initialize Presto IETF network");
            Network topo = new NetworkBuilder()
                    .setNetworkId(new NetworkId(TapiConstants.PRESTO_NETWORK_ID))
                    .build();
            tx.put(LogicalDatastoreType.OPERATIONAL, topo(), topo);
            try {
                tx.submit().checkedGet();
                log.debug("MEF Presto network created");
            } catch (TransactionCommitFailedException e) {
                log.error("Failed to create MEF Presto network");
                throw new IllegalStateException("cannot create MEF Presto network", e);
            }
        }
    }

    private static InstanceIdentifier<Network> topo() {
        return InstanceIdentifier
                .builder(Network.class, new NetworkKey(new NetworkId(TapiConstants.PRESTO_NETWORK_ID))).build();
    }

    public void close() {

    }
}
