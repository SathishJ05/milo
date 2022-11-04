/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.model.objects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.variables.BaseDataVariableTypeNode;
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

public class ProgramTransitionEventTypeNode extends TransitionEventTypeNode implements ProgramTransitionEventType {
    public ProgramTransitionEventTypeNode(OpcUaClient client, NodeId nodeId, NodeClass nodeClass,
                                          QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                          UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                          RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                          UByte eventNotifier) {
        super(client, nodeId, nodeClass, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    @Override
    public Object getIntermediateResult() throws UaException {
        BaseDataVariableTypeNode node = getIntermediateResultNode();
        return (Object) node.getValue().getValue().getValue();
    }

    @Override
    public void setIntermediateResult(Object value) throws UaException {
        BaseDataVariableTypeNode node = getIntermediateResultNode();
        node.setValue(new Variant(value));
    }

    @Override
    public Object readIntermediateResult() throws UaException {
        try {
            return readIntermediateResultAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeIntermediateResult(Object value) throws UaException {
        try {
            writeIntermediateResultAsync(value).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<?> readIntermediateResultAsync() {
        return getIntermediateResultNodeAsync()
            .thenCompose(node -> node.readAttributeAsync(AttributeId.Value))
            .thenApply(v -> (Object) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeIntermediateResultAsync(Object intermediateResult) {
        DataValue value = DataValue.valueOnly(new Variant(intermediateResult));
        return getIntermediateResultNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public BaseDataVariableTypeNode getIntermediateResultNode() throws UaException {
        try {
            return getIntermediateResultNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError));
        }
    }

    @Override
    public CompletableFuture<? extends BaseDataVariableTypeNode> getIntermediateResultNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync(
            "http://opcfoundation.org/UA/",
            "IntermediateResult",
            ExpandedNodeId.parse("ns=0;i=47"),
            false
        );
        return future.thenApply(node -> (BaseDataVariableTypeNode) node);
    }
}
