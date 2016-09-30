/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.unimgr.mef.nrp.ovs.util;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for flows
 *
 * @author marek.ryznar@amartus.com
 */
public class FlowUtils {
    protected static Long internalVid = null;

    public static Flow handleFlow(Flow flow, String inPort, Long vid){
        List<Instruction> resultInstructions = new LinkedList<>();
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        List<Instruction> instructions = new LinkedList<>();
        FlowBuilder flowBuilder = new FlowBuilder();
        if(flow.getInstructions()!=null){
            instructions = flow.getInstructions().getInstruction();
        }

        for(Instruction instruction : instructions){
            //For now - we are handling situation with all action situated in single instruction (no need for other cases for now)
            if(instruction.getOrder()==0){
                ApplyActionsCase applyActionsCase = (ApplyActionsCase) instruction.getInstruction();
                if(flow.getMatch().getInPort()!=null){
                    Instruction newInstruction = null;
                    if(flow.getMatch().getInPort().getValue().equals(inPort)){
                        newInstruction = setActions(applyActionsCase.getApplyActions(),true,inPort,vid);
                        if(vid != null){
                            flowBuilder.setMatch(createVlanMatch(vid,inPort));
                        } else {
                            flowBuilder.setMatch(flow.getMatch());
                        }
                    }
                    else{
                        newInstruction = setActions(applyActionsCase.getApplyActions(),false,inPort,vid);
                        flowBuilder.setMatch(flow.getMatch());
                    }
                    resultInstructions.add(newInstruction);
                }
            }
        }

        if(resultInstructions.isEmpty()){
            return null;
        }

        instructionsBuilder.setInstruction(resultInstructions);
        flowBuilder.setInstructions(instructionsBuilder.build());
        Flow editedFlow = copyFlowValues(flow,flowBuilder);
        return editedFlow;
    }

    public static List<Flow> addVlanPassFlows(List<Flow> flows,String portName,Long vlanID){

        List<String> portNames = getPortNames(portName);
        for(String port:portNames){
            flows.add(createVlanPassingFlow(port,vlanID,portName));
        }

        return flows;
    }

    private static Flow createVlanPassingFlow(String port, Long vlanID, String inputPort){
        FlowBuilder flowBuilder = new FlowBuilder();
        String flowName = "vlan-" + port;
        FlowId flowId = new FlowId(flowName);
        FlowKey flowKey = new FlowKey(flowId);
        flowBuilder.setId(flowId);
        flowBuilder.setKey(flowKey);
        flowBuilder.setTableId(Short.valueOf("0"));
        flowBuilder.setPriority(10); //2 is default, so a little bit higher
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setHardTimeout(0);

        if(vlanID!=null){
            flowBuilder.setMatch(createVlanMatch(vlanID,port));
        } else {
            flowBuilder.setMatch(createVlanMatch(internalVid,port));
        }
        flowBuilder.setInstructions(createInstructions(inputPort));

        return flowBuilder.build();
    }

    private static Instructions createInstructions(String port){
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        List<Instruction> instructions = new LinkedList<>();

        //if internalVid has value it means that it was pushed inside ovs network and it needs to be pop
        List<Action> resultActions = new LinkedList<>();
        int order = 0;
        if(internalVid != null){
            order = 1;
            resultActions.add(createPopVlanAction());
        }
        resultActions.add(createOutputAction(port,order));

        instructions.add(createInstruction(resultActions));
        instructionsBuilder.setInstruction(instructions);
        return instructionsBuilder.build();
    }

    private static Action createPopVlanAction(){
        ActionBuilder actionBuilder = new ActionBuilder();

        PopVlanActionCaseBuilder popVlanActionCaseBuilder = new PopVlanActionCaseBuilder();
        PopVlanActionBuilder popVlanActionBuilder = new PopVlanActionBuilder();
        popVlanActionCaseBuilder.setPopVlanAction(popVlanActionBuilder.build());

        actionBuilder.setOrder(0);
        actionBuilder.setAction(popVlanActionCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action createOutputAction(String port,int order){
        ActionBuilder actionBuilder = new ActionBuilder();
        String outputNodeNumber = port.split(":")[2];

        OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
        outputActionBuilder.setOutputNodeConnector(new Uri(outputNodeNumber));
        outputActionBuilder.setMaxLength(65535);
        outputActionCaseBuilder.setOutputAction(outputActionBuilder.build());

        actionBuilder.setOrder(order);
        actionBuilder.setAction(outputActionCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Match createVlanMatch(Long vlanID, String port){
        MatchBuilder matchBuilder = new MatchBuilder();

        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        vlanIdBuilder.setVlanIdPresent(true);
        VlanId vlanId = new VlanId((int) (long) vlanID);
        vlanIdBuilder.setVlanId(vlanId);
        vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
        matchBuilder.setVlanMatch(vlanMatchBuilder.build());

        NodeConnectorId nodeConnectorId = new NodeConnectorId(port);
        matchBuilder.setInPort(nodeConnectorId);

        return matchBuilder.build();
    }

    private static List<String> getPortNames(String port){
        List<String> resultList = new LinkedList<>();
        String ovsName = port.split(":")[0] + ":" + port.split(":")[1];

        for (Map.Entry<String, String> entry : OFUtil.portMap.entrySet()){
            String currentPort = entry.getValue();
            if(currentPort.contains(ovsName) && isPort(entry.getValue()) && !currentPort.equals(port))
                resultList.add(entry.getValue());
        }

        return resultList;
    }

    private static boolean isPort(String port){
        String[] s = port.split(":");
        if(s.length!=3)
            return false;

        String lastValue = s[2];
        if(lastValue.matches("^\\d+$"))
            return true;

        return false;
    }

    private static Flow copyFlowValues(Flow oldFlow, FlowBuilder newFlow){
        newFlow.setId(oldFlow.getId());
        newFlow.setPriority(oldFlow.getPriority());
        newFlow.setFlags(oldFlow.getFlags());
        newFlow.setIdleTimeout(oldFlow.getIdleTimeout());
        newFlow.setHardTimeout(oldFlow.getHardTimeout());
        newFlow.setCookie(oldFlow.getCookie());
        newFlow.setTableId(oldFlow.getTableId());
        newFlow.addAugmentation(FlowStatisticsData.class,oldFlow.getAugmentation(FlowStatisticsData.class));
        return newFlow.build();
    }

    private static Instruction setActions(ApplyActions applyActions,boolean isInput,String port,Long vid){
        List<Action> resultActions;

        if(isInput){
            if(vid==null){
                resultActions = setInputAction(applyActions,true);
            } else {
                resultActions = setInputAction(applyActions,false);
            }
        } else {
            resultActions = setReaminingActions(applyActions, port);
        }

        Instruction resultInstruction = createInstruction(resultActions);
        return resultInstruction;
    }

    private static Instruction createInstruction(List<Action> actions){
        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
        applyActionsBuilder.setAction(actions);
        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());

        InstructionBuilder instructionBuilder = new InstructionBuilder();
        instructionBuilder.setOrder(0);
        instructionBuilder.setInstruction(applyActionsCaseBuilder.build());

        return instructionBuilder.build();
    }

    private static List<Action> setInputAction(ApplyActions applyActions,boolean push){
        List<Action> actions = applyActions.getAction();
        List<Action> resultActions = new LinkedList<>();
        Action buffer;
        for(Action action:actions){
            buffer = deleteControllerAction(action);
            if(buffer!=null){
                resultActions.add(buffer);
            }
        }

        if(push) {
            //TODO: generate vlan, based on used vlan in topology
            internalVid = Long.valueOf("1234");
            int vlanID = (int) (long) internalVid;

            buffer = createPushVlanAction();
            resultActions.add(buffer);
            buffer = createSetVlanIdAction(vlanID);
            resultActions.add(buffer);
        }

        return resultActions;
    }

    private static List<Action> setReaminingActions(ApplyActions applyActions,String port){
        List<Action> actions = applyActions.getAction();
        List<Action> resultActions = new LinkedList<>();
        Action buffer;
        for(Action action:actions){
            buffer = deletePassingWithoutVlan(action,port);
            if(buffer!=null){
                resultActions.add(buffer);
            }
        }
        return  resultActions;
    }

    private static Action deleteControllerAction(Action action){
        ActionBuilder actionBuilder = new ActionBuilder();

        OutputActionCase outputActionCase = (OutputActionCase) action.getAction();
        OutputAction outputAction = outputActionCase.getOutputAction();
        String outputNode = outputAction.getOutputNodeConnector().getValue();
        if(outputNode.equals("CONTROLLER")){
            return null;
        }
        actionBuilder.setAction(outputActionCase);
        actionBuilder.setOrder(action.getOrder() + 2); // +2 becouse two action was added (push and set vlan)
        return actionBuilder.build();
    }

    private static Action deletePassingWithoutVlan(Action action,String port){
        ActionBuilder actionBuilder = new ActionBuilder();
        String inPortNumber = port.split(":")[2];

        OutputActionCase outputActionCase = (OutputActionCase) action.getAction();
        OutputAction outputAction = outputActionCase.getOutputAction();
        String outputNode = outputAction.getOutputNodeConnector().getValue();
        if(outputNode.equals("CONTROLLER") || outputNode.equals(inPortNumber)){
            return null;
        }

        actionBuilder.setAction(outputActionCase);
        actionBuilder.setOrder(action.getOrder());
        return actionBuilder.build();
    }

    private static Action createPushVlanAction(){
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setOrder(0);

        PushVlanActionBuilder pushVlanActionBuilder = new PushVlanActionBuilder();
        pushVlanActionBuilder.setEthernetType(33024);
        PushVlanAction pushVlanAction = pushVlanActionBuilder.build();

        PushVlanActionCaseBuilder pushVlanActionCaseBuilder = new PushVlanActionCaseBuilder();
        pushVlanActionCaseBuilder.setPushVlanAction(pushVlanAction);

        actionBuilder.setAction(pushVlanActionCaseBuilder.build());

        return actionBuilder.build();
    }

    private static Action createSetVlanIdAction(int vlan){
        ActionBuilder actionBuilder = new ActionBuilder();

        org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId vlanId = new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(vlan);

        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        vlanIdBuilder.setVlanIdPresent(true);
        vlanIdBuilder.setVlanId(vlanId);

        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        vlanMatchBuilder.setVlanId(vlanIdBuilder.build());

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        setFieldBuilder.setVlanMatch(vlanMatchBuilder.build());

        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        setFieldCaseBuilder.setSetField(setFieldBuilder.build());

        actionBuilder.setAction(setFieldCaseBuilder.build());
        actionBuilder.setOrder(1);

        return actionBuilder.build();
    }
}
