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
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part4/5.12.3/#5.12.3.2">https://reference.opcfoundation.org/v105/Core/docs/Part4/5.12.3/#5.12.3.2</a>
 */
@EqualsAndHashCode(
    callSuper = false
)
@SuperBuilder
@ToString
public class MonitoredItemModifyResult extends Structure implements UaStructuredType {
    public static final ExpandedNodeId TYPE_ID = ExpandedNodeId.parse("ns=0;i=758");

    public static final ExpandedNodeId BINARY_ENCODING_ID = ExpandedNodeId.parse("i=760");

    public static final ExpandedNodeId XML_ENCODING_ID = ExpandedNodeId.parse("i=759");

    public static final ExpandedNodeId JSON_ENCODING_ID = ExpandedNodeId.parse("i=15326");

    private final StatusCode statusCode;

    private final Double revisedSamplingInterval;

    private final UInteger revisedQueueSize;

    private final ExtensionObject filterResult;

    public MonitoredItemModifyResult(StatusCode statusCode, Double revisedSamplingInterval,
                                     UInteger revisedQueueSize, ExtensionObject filterResult) {
        this.statusCode = statusCode;
        this.revisedSamplingInterval = revisedSamplingInterval;
        this.revisedQueueSize = revisedQueueSize;
        this.filterResult = filterResult;
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

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public Double getRevisedSamplingInterval() {
        return revisedSamplingInterval;
    }

    public UInteger getRevisedQueueSize() {
        return revisedQueueSize;
    }

    public ExtensionObject getFilterResult() {
        return filterResult;
    }

    public static StructureDefinition definition(NamespaceTable namespaceTable) {
        return new StructureDefinition(
            new NodeId(0, 760),
            new NodeId(0, 22),
            StructureType.Structure,
            new StructureField[]{
                new StructureField("StatusCode", LocalizedText.NULL_VALUE, new NodeId(0, 19), -1, null, UInteger.valueOf(0), false),
                new StructureField("RevisedSamplingInterval", LocalizedText.NULL_VALUE, new NodeId(0, 290), -1, null, UInteger.valueOf(0), false),
                new StructureField("RevisedQueueSize", LocalizedText.NULL_VALUE, new NodeId(0, 289), -1, null, UInteger.valueOf(0), false),
                new StructureField("FilterResult", LocalizedText.NULL_VALUE, new NodeId(0, 22), -1, null, UInteger.valueOf(0), false)
            }
        );
    }

    public static final class Codec extends GenericDataTypeCodec<MonitoredItemModifyResult> {
        @Override
        public Class<MonitoredItemModifyResult> getType() {
            return MonitoredItemModifyResult.class;
        }

        @Override
        public MonitoredItemModifyResult decodeType(EncodingContext context, UaDecoder decoder) {
            StatusCode statusCode = decoder.decodeStatusCode("StatusCode");
            Double revisedSamplingInterval = decoder.decodeDouble("RevisedSamplingInterval");
            UInteger revisedQueueSize = decoder.decodeUInt32("RevisedQueueSize");
            ExtensionObject filterResult = decoder.decodeExtensionObject("FilterResult");
            return new MonitoredItemModifyResult(statusCode, revisedSamplingInterval, revisedQueueSize, filterResult);
        }

        @Override
        public void encodeType(EncodingContext context, UaEncoder encoder,
                               MonitoredItemModifyResult value) {
            encoder.encodeStatusCode("StatusCode", value.getStatusCode());
            encoder.encodeDouble("RevisedSamplingInterval", value.getRevisedSamplingInterval());
            encoder.encodeUInt32("RevisedQueueSize", value.getRevisedQueueSize());
            encoder.encodeExtensionObject("FilterResult", value.getFilterResult());
        }
    }
}
