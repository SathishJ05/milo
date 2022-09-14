/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.server.nodes;

import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.ServerTable;
import org.eclipse.milo.opcua.stack.core.channel.EncodingLimits;
import org.eclipse.milo.opcua.stack.core.encoding.EncodingContext;
import org.eclipse.milo.opcua.stack.core.types.DataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager;

public class TestEncodingContext implements EncodingContext {

    @Override
    public DataTypeManager getDataTypeManager() {
        return OpcUaDataTypeManager.getInstance();
    }

    @Override
    public EncodingLimits getEncodingLimits() {
        return EncodingLimits.DEFAULT;
    }

    @Override
    public NamespaceTable getNamespaceTable() {
        return new NamespaceTable();
    }

    @Override
    public ServerTable getServerTable() {
        return new ServerTable();
    }

}