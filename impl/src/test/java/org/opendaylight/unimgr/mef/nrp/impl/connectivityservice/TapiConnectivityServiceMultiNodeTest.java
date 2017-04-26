/*
 * Copyright (c) 2017 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.mef.nrp.impl.connectivityservice;

import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.unimgr.mef.nrp.api.*;
import org.opendaylight.unimgr.mef.nrp.common.ResourceActivatorException;
import org.opendaylight.unimgr.mef.nrp.impl.ConnectivityServiceIdResourcePool;
import org.opendaylight.unimgr.utils.ActivationDriverMocks;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapicommon.rev170227.PortRole;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapicommon.rev170227.UniversalId;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.CreateConnectivityServiceInput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.CreateConnectivityServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.CreateConnectivityServiceOutput;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.create.connectivity.service.input.EndPoint;
import org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.create.connectivity.service.input.EndPointBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author bartosz.michalik@amartus.com
 */
public class TapiConnectivityServiceMultiNodeTest {


    private ActivationDriver ad1;
    private ActivationDriver ad2;
    private ActivationDriver ad3;


    private UniversalId uuid1 = new UniversalId("uuid1");
    private UniversalId uuid2 = new UniversalId("uuid2");
    private UniversalId uuid3 = new UniversalId("uuid3");
    private UniversalId driver1 = new UniversalId("driver1");
    private UniversalId driver2 = new UniversalId("driver2");
    private UniversalId driver3 = new UniversalId("driver3");
    private TapiConnectivityServiceImpl connectivityService;
    private RequestDecomposer decomposer;
    private RequestValidator validator;
    private ReadWriteTransaction tx;

    @Before
    public void setUp() {
        ad1 = mock(ActivationDriver.class);
        ad2 = mock(ActivationDriver.class);
        ad3 = mock(ActivationDriver.class);
        ActivationDriverRepoService repo = ActivationDriverMocks.builder()
                .add(driver1, ad1)
                .add(driver2, ad2)
                .add(driver3, ad3)
                .build();

        decomposer = mock(RequestDecomposer.class);
        validator = mock(RequestValidator.class);
        when(validator.checkValid(any())).thenReturn(new RequestValidator.ValidationResult());

        connectivityService = new TapiConnectivityServiceImpl();
        connectivityService.setDriverRepo(repo);
        connectivityService.setDecomposer(decomposer);
        connectivityService.setValidator(validator);

        tx = mock(ReadWriteTransaction.class);
        when(tx.submit()).thenReturn(mock(CheckedFuture.class));
        DataBroker broker = mock(DataBroker.class);


        when(broker.newWriteOnlyTransaction()).thenReturn(tx);
        connectivityService.setBroker(broker);
        connectivityService.setServiceIdPool(new ConnectivityServiceIdResourcePool());
        connectivityService.init();
    }


    @Test
    public void sucessfullTwoDrivers() throws ExecutionException, InterruptedException, ResourceActivatorException, TransactionCommitFailedException {
        //having
        CreateConnectivityServiceInput input = input(5);


        configureDecomposerAnswer(eps -> {
            Subrequrest s1 = new Subrequrest(driver1, uuid1, Arrays.asList(eps.get(0), eps.get(1), eps.get(2)));
            Subrequrest s3 = new Subrequrest(driver3, uuid3, Arrays.asList(eps.get(3), eps.get(4)));

            return Arrays.asList(s1, s3);
        });

        //when
        RpcResult<CreateConnectivityServiceOutput> result = this.connectivityService.createConnectivityService(input).get();
        //then
        assertTrue(result.isSuccessful());
        verify(ad1).activate();
        verify(ad3).activate();
        verify(ad1).commit();
        verify(ad3).commit();
        verifyZeroInteractions(ad2);
        //3x Connection (2 x system + 1 external) + ConnectivityService
        verify(tx,times(4)).put(eq(LogicalDatastoreType.OPERATIONAL), any(InstanceIdentifier.class), any());


    }

    private void configureDecomposerAnswer(Function<List<org.opendaylight.unimgr.mef.nrp.api.EndPoint>, List<Subrequrest>> resp) {
        when(decomposer.decompose(any(), any(Constraints.class)))
                .thenAnswer(a -> {
                    List<org.opendaylight.unimgr.mef.nrp.api.EndPoint> eps = a.getArgumentAt(0, List.class);
                    return resp.apply(eps);
                });
    }

    private CreateConnectivityServiceInput input(int count) {

        List<EndPoint> eps = IntStream.range(0, count).mapToObj(x -> ep("ep" + x)).collect(Collectors.toList());

        return new CreateConnectivityServiceInputBuilder()
                .setEndPoint(eps)
                .build();
    }

    private org.opendaylight.yang.gen.v1.urn.mef.yang.tapiconnectivity.rev170227.create.connectivity.service.input.EndPoint ep(String id) {
        return new EndPointBuilder()
                .setLocalId(id)
                .setRole(PortRole.Symmetric)
//                .setServiceInterfacePoint()
        .build();
    }

}