/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.encoding.binary;

import java.util.Arrays;

import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.encoding.DataTypeCodec;
import org.eclipse.milo.opcua.stack.core.encoding.TestEncodingContext;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ApplicationType;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class BinaryDecoderTest extends BinarySerializationFixture {

    @Test(description = "a null array, once encoded, should decode to a null array")
    public void testDecodeNullArray() {
        Argument argument = new Argument(
            "test",
            NodeIds.Int16,
            1,
            null,
            LocalizedText.NULL_VALUE
        );

        DataTypeCodec codec = OpcUaDataTypeManager.getInstance().getCodec(
            OpcUaDefaultBinaryEncoding.ENCODING_NAME,
            Argument.TYPE_ID.toNodeId(new NamespaceTable()).get()
        );

        assertNotNull(codec);

        codec.encode(new TestEncodingContext(), writer, argument);
        Argument decoded = (Argument) codec.decode(new TestEncodingContext(), reader);

        assertEquals(decoded.getName(), argument.getName());

        assertNull(decoded.getArrayDimensions());
    }

    @Test
    public void testEnumScalar() {
        writer.encodeEnum(null, ApplicationType.Client);
        ApplicationType decoded = ApplicationType.from(reader.decodeEnum(null));

        assertEquals(decoded, ApplicationType.Client);
    }

    @Test
    public void testEnumArray() throws Exception {
        ApplicationType[] array = new ApplicationType[]{
            ApplicationType.Client,
            ApplicationType.ClientAndServer
        };
        writer.encodeEnumArray(null, array);
        Integer[] decodedValues = reader.decodeEnumArray(null);
        ApplicationType[] decoded = Arrays.stream(decodedValues)
            .map(ApplicationType::from)
            .toArray(ApplicationType[]::new);

        assertEquals(decoded, array);
    }

}
