/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.mef.nrp.rpc;

import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.nrp_api.rev700101.ActivateConnectivityRequestInput;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.nrp_api.rev700101.ActivateConnectivityRequestOutput;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.nrp_api.rev700101.NrpApiService;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.concurrent.Future;

/**
 * @author bartosz.michalik@amartus.com
 */
public class NrpApiServiceImpl implements NrpApiService {
    @Override
    public Future<RpcResult<ActivateConnectivityRequestOutput>> activateConnectivityRequest(ActivateConnectivityRequestInput input) {
        return null;
    }
}
