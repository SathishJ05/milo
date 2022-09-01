package org.eclipse.milo.opcua.sdk.client.model.objects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class KeyCredentialAuditEventTypeNode extends AuditUpdateMethodEventTypeNode implements KeyCredentialAuditEventType {
    public KeyCredentialAuditEventTypeNode(OpcUaClient client, NodeId nodeId, NodeClass nodeClass,
                                           QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                           UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                           RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                           UByte eventNotifier) {
        super(client, nodeId, nodeClass, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    @Override
    public String getResourceUri() throws UaException {
        PropertyTypeNode node = getResourceUriNode();
        return (String) node.getValue().getValue().getValue();
    }

    @Override
    public void setResourceUri(String value) throws UaException {
        PropertyTypeNode node = getResourceUriNode();
        node.setValue(new Variant(value));
    }

    @Override
    public String readResourceUri() throws UaException {
        try {
            return readResourceUriAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeResourceUri(String value) throws UaException {
        try {
            writeResourceUriAsync(value).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends String> readResourceUriAsync() {
        return getResourceUriNodeAsync()
            .thenCompose(node -> node.readAttributeAsync(AttributeId.Value))
            .thenApply(v -> (String) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeResourceUriAsync(String resourceUri) {
        DataValue value = DataValue.valueOnly(new Variant(resourceUri));
        return getResourceUriNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getResourceUriNode() throws UaException {
        try {
            return getResourceUriNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getResourceUriNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync(
            "http://opcfoundation.org/UA/",
            "ResourceUri",
            ExpandedNodeId.parse("ns=0;i=46"),
            false
        );
        return future.thenApply(node -> (PropertyTypeNode) node);
    }
}