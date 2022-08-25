package org.eclipse.milo.opcua.sdk.server.model.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class TransitionTypeNode extends BaseObjectTypeNode implements TransitionType {
    public TransitionTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                              LocalizedText displayName, LocalizedText description, UInteger writeMask,
                              UInteger userWriteMask, RolePermissionType[] rolePermissions,
                              RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                              UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    public TransitionTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                              LocalizedText displayName, LocalizedText description, UInteger writeMask,
                              UInteger userWriteMask, RolePermissionType[] rolePermissions,
                              RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions);
    }

    @Override
    public PropertyTypeNode getTransitionNumberNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(TransitionType.TRANSITION_NUMBER);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public UInteger getTransitionNumber() {
        return getProperty(TransitionType.TRANSITION_NUMBER).orElse(null);
    }

    @Override
    public void setTransitionNumber(UInteger value) {
        setProperty(TransitionType.TRANSITION_NUMBER, value);
    }
}