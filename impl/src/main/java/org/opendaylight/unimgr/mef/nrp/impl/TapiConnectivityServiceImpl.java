/*
 * Copyright (c) 2017 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.unimgr.mef.nrp.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opendaylight.yang.gen.v1.urn.mef.yang.tapicommon.rev170227.AdministrativeState;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapicommon.rev170227.LifecycleState;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapicommon.rev170227.OperationalState;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapicommon.rev170227.UniversalId;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.CreateConnectivityServiceInput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.CreateConnectivityServiceOutput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.CreateConnectivityServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.DeleteConnectivityServiceInput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.DeleteConnectivityServiceOutput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.GetConnectionDetailsInput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.GetConnectionDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.GetConnectivityServiceDetailsInput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.GetConnectivityServiceDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.GetConnectivityServiceListOutput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.TapiConnectivityService;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.UpdateConnectivityServiceInput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.UpdateConnectivityServiceOutput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.connectivity.service.EndPointKey;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.connectivity.service.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.create.connectivity.service.input.EndPoint;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.create.connectivity.service.output.ServiceBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bartosz.michalik@amartus.com
 */
public class TapiConnectivityServiceImpl implements TapiConnectivityService, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TapiConnectivityServiceImpl.class);

    private ExecutorService executor = new ThreadPoolExecutor(4, 16,
            30, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>());

    public void init() {
        log.info("TapiConnectivityService initialized");
    }

    @Override
    public Future<RpcResult<CreateConnectivityServiceOutput>> createConnectivityService(CreateConnectivityServiceInput input) {
        return executor.submit(new CreateConnectivity(input));
    }


    @Override
    public Future<RpcResult<UpdateConnectivityServiceOutput>> updateConnectivityService(UpdateConnectivityServiceInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<GetConnectionDetailsOutput>> getConnectionDetails(GetConnectionDetailsInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<GetConnectivityServiceDetailsOutput>> getConnectivityServiceDetails(GetConnectivityServiceDetailsInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<DeleteConnectivityServiceOutput>> deleteConnectivityService(DeleteConnectivityServiceInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<GetConnectivityServiceListOutput>> getConnectivityServiceList() {
        return null;
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
    }

    static int instanceId = 1;

    class CreateConnectivity implements Callable<RpcResult<CreateConnectivityServiceOutput>> {

        private final CreateConnectivityServiceInput input;

        CreateConnectivity(CreateConnectivityServiceInput input) {
            this.input = input;
        }

        @Override
        public RpcResult<CreateConnectivityServiceOutput> call() throws Exception {
            log.warn("create-connectivity-service is not yet implemented. This is mock response.");

            List<org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.connectivity.service.EndPoint> outputEndPoints = new ArrayList<>();
            for (EndPoint e : input.getEndPoint()) {
                outputEndPoints.add(
                        new org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.connectivity.service.EndPointBuilder()
                        .setServiceInterfacePoint(e.getServiceInterfacePoint())
                        .setDirection(e.getDirection())
                        .setLayerProtocolName(e.getLayerProtocolName())
                        .setLocalId(e.getServiceInterfacePoint().getValue())
                        .setKey(new EndPointKey(e.getServiceInterfacePoint().getValue()))
                        .build()
                        );
            }

            ServiceBuilder service = new ServiceBuilder()
                    .setUuid(new UniversalId("Tapi:ConnectivityService:" + instanceId++))
                    .setEndPoint(outputEndPoints)
                    .setState(new StateBuilder()
                            .setLifecycleState(LifecycleState.Planned)
                            .setAdministrativeState(AdministrativeState.Unlocked)
                            .setOperationalState(OperationalState.Disabled)
                            .build());

            CreateConnectivityServiceOutputBuilder builder = new CreateConnectivityServiceOutputBuilder()
                    .setService(service.build());
            return RpcResultBuilder
                    .<CreateConnectivityServiceOutput>success(builder)
                    .build();
        }
    }
}
