package org.eclipse.milo.opcua.sdk.server.model.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.BaseDataVariableTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NegotiationStatus;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class IIeeeAutoNegotiationStatusTypeNode extends BaseInterfaceTypeNode implements IIeeeAutoNegotiationStatusType {
    public IIeeeAutoNegotiationStatusTypeNode(UaNodeContext context, NodeId nodeId,
                                              QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                              UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                              RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                              UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    public IIeeeAutoNegotiationStatusTypeNode(UaNodeContext context, NodeId nodeId,
                                              QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                              UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                              RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions);
    }

    @Override
    public BaseDataVariableTypeNode getNegotiationStatusNode() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "NegotiationStatus");
        return (BaseDataVariableTypeNode) component.orElse(null);
    }

    @Override
    public NegotiationStatus getNegotiationStatus() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "NegotiationStatus");
        return component.map(node -> (NegotiationStatus) node.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public void setNegotiationStatus(NegotiationStatus value) {
        getVariableComponent("http://opcfoundation.org/UA/", "NegotiationStatus").ifPresent(n -> n.setValue(new DataValue(new Variant(value))));
    }
}