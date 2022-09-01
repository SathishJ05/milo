package org.eclipse.milo.opcua.sdk.client.model.objects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class CertificateGroupFolderTypeNode extends FolderTypeNode implements CertificateGroupFolderType {
    public CertificateGroupFolderTypeNode(OpcUaClient client, NodeId nodeId, NodeClass nodeClass,
                                          QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                          UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                          RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                          UByte eventNotifier) {
        super(client, nodeId, nodeClass, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    @Override
    public CertificateGroupTypeNode getDefaultApplicationGroupNode() throws UaException {
        try {
            return getDefaultApplicationGroupNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError));
        }
    }

    @Override
    public CompletableFuture<? extends CertificateGroupTypeNode> getDefaultApplicationGroupNodeAsync(
    ) {
        CompletableFuture<UaNode> future = getMemberNodeAsync(
            "http://opcfoundation.org/UA/",
            "DefaultApplicationGroup",
            ExpandedNodeId.parse("ns=0;i=35"),
            false
        );
        return future.thenApply(node -> (CertificateGroupTypeNode) node);
    }

    @Override
    public CertificateGroupTypeNode getDefaultHttpsGroupNode() throws UaException {
        try {
            return getDefaultHttpsGroupNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError));
        }
    }

    @Override
    public CompletableFuture<? extends CertificateGroupTypeNode> getDefaultHttpsGroupNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync(
            "http://opcfoundation.org/UA/",
            "DefaultHttpsGroup",
            ExpandedNodeId.parse("ns=0;i=35"),
            false
        );
        return future.thenApply(node -> (CertificateGroupTypeNode) node);
    }

    @Override
    public CertificateGroupTypeNode getDefaultUserTokenGroupNode() throws UaException {
        try {
            return getDefaultUserTokenGroupNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError));
        }
    }

    @Override
    public CompletableFuture<? extends CertificateGroupTypeNode> getDefaultUserTokenGroupNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync(
            "http://opcfoundation.org/UA/",
            "DefaultUserTokenGroup",
            ExpandedNodeId.parse("ns=0;i=35"),
            false
        );
        return future.thenApply(node -> (CertificateGroupTypeNode) node);
    }
}