/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.utils;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.unimgr.mef.nrp.impl.TopologyService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author bartosz.michalik@amartus.com
 */
@RunWith(PowerMockRunner.class)
public class TopologyServiceTest {
    @Mock private DataBroker dataBroker;
    @Mock private ReadWriteTransaction tx;

    private TopologyService service;
    private Optional<Topology> optionalDataObject;


    @Before
    public void setUp() throws ReadFailedException {
        optionalDataObject = mock(Optional.class);
        CheckedFuture<Optional<Topology>, ReadFailedException> future = mock(CheckedFuture.class);
        when(future.checkedGet()).thenReturn(optionalDataObject);
        when(optionalDataObject.isPresent()).thenReturn(true);

        service = new TopologyService(dataBroker);
        when(dataBroker.newReadWriteTransaction()).thenReturn(tx);

        doReturn(future).when(tx).read(eq(LogicalDatastoreType.OPERATIONAL), any(InstanceIdentifier.class));
    }

    @Test
    public void initializeTopologyEmpty() throws Exception {
        when(optionalDataObject.isPresent()).thenReturn(false);
        when(tx.submit()).thenReturn(mock(CheckedFuture.class));

        service.init();
        verify(tx).put(eq(LogicalDatastoreType.OPERATIONAL), any(InstanceIdentifier.class), anyObject());
        verify(tx).submit();
    }

    @Test
    public void initializeTopologyExisting() throws Exception {
        when(optionalDataObject.isPresent()).thenReturn(true);
        service.init();
        verify(tx, times(0)).put(any(LogicalDatastoreType.class), any(InstanceIdentifier.class), anyObject());
    }
}
