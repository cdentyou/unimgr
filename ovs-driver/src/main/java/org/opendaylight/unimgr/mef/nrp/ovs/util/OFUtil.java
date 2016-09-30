/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.unimgr.mef.nrp.ovs.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.unimgr.utils.NullAwareDatastoreGetter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for managing OpenFlow rules in OVS
 *
 * @author marek.ryznar@amartus.com
 */
public class OFUtil {
    protected static Map<String,String> portMap;

    public static Node getOFNode(DataBroker dataBroker, String portName){
        InstanceIdentifier ncIID = InstanceIdentifier.builder(Nodes.class).build();
        List<NullAwareDatastoreGetter<Node>> nodes =
                new NullAwareDatastoreGetter<Nodes>(org.opendaylight.unimgr.utils.MdsalUtils.readOptional(dataBroker, LogicalDatastoreType.OPERATIONAL,ncIID))
                        .collectMany(x -> x::getNode);
        portMap = new HashMap<>();
        Node result = null;
        for(NullAwareDatastoreGetter<Node> node:nodes){
            for(NodeConnector nodeConnector:node.get().get().getNodeConnector()){
                String ofName = nodeConnector.getId().getValue();
                FlowCapableNodeConnector flowCapableNodeConnector = nodeConnector.getAugmentation(FlowCapableNodeConnector.class);
                String name = flowCapableNodeConnector.getName();
                if(portName.equals(name))
                    result=node.get().get();
                portMap.put(name,ofName);
            }
        }
        return result;
    }

    public static Table getTable(Node ofNode){
        FlowCapableNode flowCapableNode = ofNode.getAugmentation(FlowCapableNode.class);
        //All flows are always in table with id = 0
        List<Table> tables = flowCapableNode.getTable().stream().filter(x -> x.getId()==0).collect(Collectors.toList());
        Table table = tables.get(0);
        return table;
    }

    public static Table handleFlows(Table table, String portName, Long vid){
        String inPort = portMap.get(portName);
        FlowUtils.internalVid = null;
        List<Flow> flows = table.getFlow();
        List<Flow> resultFlows = new LinkedList<>();
        for(Flow flow:flows){
            Flow resultFlow = FlowUtils.handleFlow(flow,inPort,vid);
            if(resultFlow!=null)
                resultFlows.add(resultFlow);
        }
        resultFlows = FlowUtils.addVlanPassFlows(resultFlows,inPort,vid);

        TableBuilder tb = new TableBuilder();
        tb.setFlow(resultFlows);
        tb.setId(table.getId());
        tb.addAugmentation(FlowTableStatisticsData.class,table.getAugmentation(FlowTableStatisticsData.class));


        return tb.build();
    }

    public static void writeTable(DataBroker dataBroker,Table table,NodeKey nodeKey,TableKey tableKey) throws TransactionCommitFailedException {
        InstanceIdentifier tableIid = getTableIid(nodeKey,tableKey);

        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.put(LogicalDatastoreType.CONFIGURATION,tableIid,table,true);
        transaction.submit().checkedGet();
    }

    private static InstanceIdentifier getTableIid(NodeKey nodeKey,TableKey tableKey){
        return InstanceIdentifier.builder(Nodes.class)
            .child(Node.class, nodeKey)
            .augmentation(FlowCapableNode.class)
            .child(Table.class,tableKey)
            .build();
    }
}
