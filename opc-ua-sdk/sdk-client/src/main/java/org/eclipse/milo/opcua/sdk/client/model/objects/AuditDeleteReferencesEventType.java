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

import org.eclipse.milo.opcua.sdk.client.model.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteReferencesItem;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part5/6.4.23">https://reference.opcfoundation.org/v105/Core/docs/Part5/6.4.23</a>
 */
public interface AuditDeleteReferencesEventType extends AuditNodeManagementEventType {
    QualifiedProperty<DeleteReferencesItem[]> REFERENCES_TO_DELETE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ReferencesToDelete",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=385"),
        1,
        DeleteReferencesItem[].class
    );

    /**
     * Get the local value of the ReferencesToDelete Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ReferencesToDelete Node.
     * @throws UaException if an error occurs creating or getting the ReferencesToDelete Node.
     */
    DeleteReferencesItem[] getReferencesToDelete() throws UaException;

    /**
     * Set the local value of the ReferencesToDelete Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param value the local value to set for the ReferencesToDelete Node.
     * @throws UaException if an error occurs creating or getting the ReferencesToDelete Node.
     */
    void setReferencesToDelete(DeleteReferencesItem[] value) throws UaException;

    /**
     * Read the value of the ReferencesToDelete Node from the server and update the local value if
     * the operation succeeds.
     *
     * @return the {@link DeleteReferencesItem[]} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    DeleteReferencesItem[] readReferencesToDelete() throws UaException;

    /**
     * Write a new value for the ReferencesToDelete Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param value the {@link DeleteReferencesItem[]} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeReferencesToDelete(DeleteReferencesItem[] value) throws UaException;

    /**
     * An asynchronous implementation of {@link #readReferencesToDelete}.
     *
     * @return a CompletableFuture that completes successfully with the value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends DeleteReferencesItem[]> readReferencesToDeleteAsync();

    /**
     * An asynchronous implementation of {@link #writeReferencesToDelete}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeReferencesToDeleteAsync(DeleteReferencesItem[] value);

    /**
     * Get the ReferencesToDelete {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ReferencesToDelete {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getReferencesToDeleteNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getReferencesToDeleteNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * PropertyType Node or completes exceptionally if an error occurs creating or
     * getting the Node.
     */
    CompletableFuture<? extends PropertyType> getReferencesToDeleteNodeAsync();
}
