/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.mef.nrp.rpc;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.tapi.rev161004.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.tapiconnectivity.rev161011.*;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.tapiconnectivity.rev161011.create.connectivity.service.output.ConnServiceBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.concurrent.Future;

/**
 * @author bartosz.michalik@amartus.com
 */
public class TapiConnectivityServiceImpl implements TapiConnectivityService {
    private final DataBroker broker;

    public TapiConnectivityServiceImpl(DataBroker broker) {
        this.broker = broker;
    }


    @Override
    public Future<RpcResult<CreateConnectivityServiceOutput>> createConnectivityService(CreateConnectivityServiceInput input) {
        CreateConnectivityServiceOutput result = new CreateConnectivityServiceOutputBuilder()
                .setConnService(new ConnServiceBuilder()
                        .setUuid(UniversalId.getDefaultInstance("some value"))
                        .build())
                .build();
        return RpcResultBuilder.success(result).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteConnectivityServiceOutput>> deleteConnectivityService(DeleteConnectivityServiceInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<UpdateConnectivityServiceOutput>> updateConnectivityService(UpdateConnectivityServiceInput input) {
        return null;
    }
}
