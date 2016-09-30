/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.unimgr.mef.nrp.ovs.activator;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.unimgr.mef.nrp.common.ResourceActivator;
import org.opendaylight.unimgr.mef.nrp.ovs.util.OFUtil;
import org.opendaylight.yang.gen.v1.urn.mef.unimgr.ext.rev160725.FcPort1;
import org.opendaylight.yang.gen.v1.urn.onf.core.network.module.rev160630.g_forwardingconstruct.FcPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;

/**
 * @author marek.ryznar@amartus.com
 */
public class OVSActivator implements ResourceActivator {

    private final DataBroker dataBroker;

    public OVSActivator(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void activate(String nodeName, String outerName, String innerName, FcPort flowPoint, FcPort neighbor, long mtu) throws TransactionCommitFailedException {

        String portName = flowPoint.getTp().getValue();
        FcPort1 fcPort1 = flowPoint.getAugmentation(FcPort1.class);

        Long vid = null;
        if(fcPort1!=null){
            if(fcPort1.getCTagVlanId()!=null){
                vid = fcPort1.getCTagVlanId().getValue();
            }
        }

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node ofNode = OFUtil.getOFNode(dataBroker,portName);

        Table table = OFUtil.getTable(ofNode);
        Table editedtable = OFUtil.handleFlows(table,portName, vid);

        OFUtil.writeTable(dataBroker,editedtable,ofNode.getKey(),table.getKey());
    }

    @Override
    public void deactivate(String nodeName, String outerName, String innerName, FcPort flowPoint, FcPort neighbor, long mtu) {
        //TODO: Here it should write to the original state or call ODL mechanism to do it
    }


}
