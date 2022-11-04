/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.types.structured;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.encoding.EncodingContext;
import org.eclipse.milo.opcua.stack.core.encoding.GenericDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.encoding.UaDecoder;
import org.eclipse.milo.opcua.stack.core.encoding.UaEncoder;
import org.eclipse.milo.opcua.stack.core.types.UaStructuredType;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType;

/**
 * @see <a href="https://reference.opcfoundation.org/v104/Core/docs/Part11/5.5">https://reference.opcfoundation.org/v104/Core/docs/Part11/5.5</a>
 */
@EqualsAndHashCode(
    callSuper = false
)
@SuperBuilder
@ToString
public class Annotation extends Structure implements UaStructuredType {
    public static final ExpandedNodeId TYPE_ID = ExpandedNodeId.parse("ns=0;i=891");

    public static final ExpandedNodeId BINARY_ENCODING_ID = ExpandedNodeId.parse("i=893");

    public static final ExpandedNodeId XML_ENCODING_ID = ExpandedNodeId.parse("i=892");

    public static final ExpandedNodeId JSON_ENCODING_ID = ExpandedNodeId.parse("i=15382");

    private final String message;

    private final String userName;

    private final DateTime annotationTime;

    public Annotation(String message, String userName, DateTime annotationTime) {
        this.message = message;
        this.userName = userName;
        this.annotationTime = annotationTime;
    }

    @Override
    public ExpandedNodeId getTypeId() {
        return TYPE_ID;
    }

    @Override
    public ExpandedNodeId getBinaryEncodingId() {
        return BINARY_ENCODING_ID;
    }

    @Override
    public ExpandedNodeId getXmlEncodingId() {
        return XML_ENCODING_ID;
    }

    @Override
    public ExpandedNodeId getJsonEncodingId() {
        return JSON_ENCODING_ID;
    }

    public String getMessage() {
        return message;
    }

    public String getUserName() {
        return userName;
    }

    public DateTime getAnnotationTime() {
        return annotationTime;
    }

    public static StructureDefinition definition(NamespaceTable namespaceTable) {
        return new StructureDefinition(
            new NodeId(0, 893),
            new NodeId(0, 22),
            StructureType.Structure,
            new StructureField[]{
                new StructureField("Message", LocalizedText.NULL_VALUE, new NodeId(0, 12), -1, null, UInteger.valueOf(0), false),
                new StructureField("UserName", LocalizedText.NULL_VALUE, new NodeId(0, 12), -1, null, UInteger.valueOf(0), false),
                new StructureField("AnnotationTime", LocalizedText.NULL_VALUE, new NodeId(0, 294), -1, null, UInteger.valueOf(0), false)
            }
        );
    }

    public static final class Codec extends GenericDataTypeCodec<Annotation> {
        @Override
        public Class<Annotation> getType() {
            return Annotation.class;
        }

        @Override
        public Annotation decodeType(EncodingContext context, UaDecoder decoder) {
            String message = decoder.decodeString("Message");
            String userName = decoder.decodeString("UserName");
            DateTime annotationTime = decoder.decodeDateTime("AnnotationTime");
            return new Annotation(message, userName, annotationTime);
        }

        @Override
        public void encodeType(EncodingContext context, UaEncoder encoder, Annotation value) {
            encoder.encodeString("Message", value.getMessage());
            encoder.encodeString("UserName", value.getUserName());
            encoder.encodeDateTime("AnnotationTime", value.getAnnotationTime());
        }
    }
}
