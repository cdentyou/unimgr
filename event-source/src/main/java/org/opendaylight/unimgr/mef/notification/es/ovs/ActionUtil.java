package org.opendaylight.unimgr.mef.notification.es.ovs;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import java.util.List;

/**
 * Class created to assist with getting information from OpenFlow instruction and form them into the part of notification message.
 *
 * @author marek.ryznar@amartus.com
 */
public class ActionUtil {

    private static final String INDENT = "\n      ";
    private static final String SUBINDENT = "\n        ";

    public static void getActions(Instructions instructions, StringBuilder message) {
        if (instructions.getInstruction() == null)
            return;
        for (Instruction instruction : instructions.getInstruction()) {
            if (instruction.getInstruction() != null) {
                handleInstruction(instruction.getInstruction(), message);
            }
        }
    }

    private static void handleInstruction(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction instruction, StringBuilder message) {
        Class<? extends DataContainer> actionCase = instruction.getImplementedInterface();

        if (actionCase.equals(ApplyActionsCase.class)) {
            ApplyActionsCase applyActionsCase = (ApplyActionsCase) instruction;
            ApplyActions applyActions = applyActionsCase.getApplyActions();
            List<Action> actions = applyActions.getAction();
            for (Action action : actions) {
                handleAction(action.getAction(), message);
            }
        }
    }

    private static void handleAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, StringBuilder message) {
        if (action == null)
            return;
        Class<? extends DataContainer> actionCase = action.getImplementedInterface();
        message.append(INDENT);
        if (actionCase.equals(PopVlanActionCase.class)) {
            message.append("Pop Vlan");
        } else if (actionCase.equals(ControllerActionCase.class)) {
            getPopVlanAction(action, message);
        } else if (actionCase.equals(DropActionCase.class)) {
            message.append("DROP");
        } else if (actionCase.equals(FloodActionCase.class)) {
            message.append("Flood action");
        } else if (actionCase.equals(FloodAllActionCase.class)) {
            message.append("Flood all action");
        } else if (actionCase.equals(GroupActionCase.class)) {
            getGroupAction(action, message);
        } else if (actionCase.equals(LoopbackActionCase.class)) {
            message.append("Loopback");
        } else if (actionCase.equals(OutputActionCase.class)) {
            getOutputAction(action, message);
        } else if (actionCase.equals(PushVlanActionCase.class)) {
            getPushVlanAction(action, message);
        } else if (actionCase.equals(SetFieldCase.class)) {
            getSetFieldAction(action, message);
        } else if (actionCase.equals(SetVlanIdActionCase.class)){
            getSetVlanIdAction(action, message);
        } else if (actionCase.equals(StripVlanActionCase.class)){
            message.append("Strip Vlan");
        }
    }

    private static void getSetVlanIdAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, StringBuilder message) {
        SetVlanIdActionCase setVlanIdActionCase = (SetVlanIdActionCase) action;
        if(setVlanIdActionCase.getSetVlanIdAction()!=null){
            message.append("Set vlan id:");
            message.append(setVlanIdActionCase.getSetVlanIdAction().getVlanId().getValue());
        }
    }

    private static void getPopVlanAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, StringBuilder message) {
        ControllerActionCase controllerActionCase = (ControllerActionCase) action;
        if (controllerActionCase.getControllerAction() != null) {
            message.append("CONTROLLER:");
            message.append(controllerActionCase.getControllerAction().getMaxLength());
        }
    }

    private static void getGroupAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, StringBuilder message) {
        GroupActionCase groupActionCase = (GroupActionCase) action;
        if (groupActionCase.getGroupAction() != null) {
            message.append("GROUP:");
            message.append(groupActionCase.getGroupAction().getGroup());
        }
    }

    private static void getOutputAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, StringBuilder message) {
        OutputActionCase outputActionCase = (OutputActionCase) action;
        if (outputActionCase.getOutputAction() != null && outputActionCase.getOutputAction().getOutputNodeConnector() != null) {
            message.append("OUTPUT:");
            message.append(outputActionCase.getOutputAction().getOutputNodeConnector().getValue());
        }
    }

    private static void getPushVlanAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, StringBuilder message) {
        PushVlanActionCase pushVlanActionCase = (PushVlanActionCase) action;
        if (pushVlanActionCase.getPushVlanAction() != null) {
            message.append("Push vlan: ");
            PushVlanAction pushVlanAction = pushVlanActionCase.getPushVlanAction();
            if (pushVlanAction.getEthernetType() != null) {
                message.append(SUBINDENT);
                message.append("EtherType:");
                message.append(pushVlanAction.getEthernetType());
                message.append(" ");
            }
            if (pushVlanAction.getVlanId() != null) {
                message.append(SUBINDENT);
                message.append("VlanId:");
                message.append(pushVlanAction.getVlanId().getValue());
                message.append(" ");
            }
            if (pushVlanAction.getCfi() != null) {
                message.append(SUBINDENT);
                message.append("Cfi:");
                message.append(pushVlanAction.getCfi().getValue());
                message.append(" ");
            }
            if (pushVlanAction.getPcp() != null) {
                message.append(SUBINDENT);
                message.append("Pcp:");
                message.append(pushVlanAction.getPcp());
                message.append(" ");
            }
            if (pushVlanAction.getTag() != null) {
                message.append(SUBINDENT);
                message.append("Tag:");
                message.append(pushVlanAction.getTag());
                message.append(" ");
            }
        }
    }

    private static void getSetFieldAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, StringBuilder message) {
        SetFieldCase setFieldCase = (SetFieldCase) action;
        if(setFieldCase.getSetField()!=null){
            SetField setField = setFieldCase.getSetField();
            message.append("Set Field:");
            MatchUtil.getVlanMatch(setField.getVlanMatch(),message);
            MatchUtil.getIpMatch(setField.getIpMatch(),message);
            MatchUtil.getEthernetMatch(setField.getEthernetMatch(),message);
            MatchUtil.getInPort(setField.getInPhyPort(),message,"Physical Port: ");
            MatchUtil.getInPort(setField.getInPort(),message,"Virtual Port: ");
        }

    }
}