package org.eclipse.milo.opcua.sdk.server.model.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.SelectionListTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class NetworkAddressTypeNode extends BaseObjectTypeNode implements NetworkAddressType {
    public NetworkAddressTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                                  LocalizedText displayName, LocalizedText description, UInteger writeMask,
                                  UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                  RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                  UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    public NetworkAddressTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                                  LocalizedText displayName, LocalizedText description, UInteger writeMask,
                                  UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                  RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions);
    }

    @Override
    public SelectionListTypeNode getNetworkInterfaceNode() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "NetworkInterface");
        return (SelectionListTypeNode) component.orElse(null);
    }

    @Override
    public String getNetworkInterface() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "NetworkInterface");
        return component.map(node -> (String) node.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public void setNetworkInterface(String value) {
        getVariableComponent("http://opcfoundation.org/UA/", "NetworkInterface").ifPresent(n -> n.setValue(new DataValue(new Variant(value))));
    }
}