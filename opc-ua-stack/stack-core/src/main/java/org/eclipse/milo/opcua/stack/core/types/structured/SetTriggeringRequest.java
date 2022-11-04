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
import org.eclipse.milo.opcua.stack.core.types.UaRequestMessageType;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part4/5.12.5/#5.12.5.2">https://reference.opcfoundation.org/v105/Core/docs/Part4/5.12.5/#5.12.5.2</a>
 */
@EqualsAndHashCode(
    callSuper = false
)
@SuperBuilder
@ToString
public class SetTriggeringRequest extends Structure implements UaRequestMessageType {
    public static final ExpandedNodeId TYPE_ID = ExpandedNodeId.parse("ns=0;i=773");

    public static final ExpandedNodeId BINARY_ENCODING_ID = ExpandedNodeId.parse("i=775");

    public static final ExpandedNodeId XML_ENCODING_ID = ExpandedNodeId.parse("i=774");

    public static final ExpandedNodeId JSON_ENCODING_ID = ExpandedNodeId.parse("i=15332");

    private final RequestHeader requestHeader;

    private final UInteger subscriptionId;

    private final UInteger triggeringItemId;

    private final UInteger[] linksToAdd;

    private final UInteger[] linksToRemove;

    public SetTriggeringRequest(RequestHeader requestHeader, UInteger subscriptionId,
                                UInteger triggeringItemId, UInteger[] linksToAdd, UInteger[] linksToRemove) {
        this.requestHeader = requestHeader;
        this.subscriptionId = subscriptionId;
        this.triggeringItemId = triggeringItemId;
        this.linksToAdd = linksToAdd;
        this.linksToRemove = linksToRemove;
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

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public UInteger getSubscriptionId() {
        return subscriptionId;
    }

    public UInteger getTriggeringItemId() {
        return triggeringItemId;
    }

    public UInteger[] getLinksToAdd() {
        return linksToAdd;
    }

    public UInteger[] getLinksToRemove() {
        return linksToRemove;
    }

    public static StructureDefinition definition(NamespaceTable namespaceTable) {
        return new StructureDefinition(
            new NodeId(0, 775),
            new NodeId(0, 22),
            StructureType.Structure,
            new StructureField[]{
                new StructureField("RequestHeader", LocalizedText.NULL_VALUE, new NodeId(0, 389), -1, null, UInteger.valueOf(0), false),
                new StructureField("SubscriptionId", LocalizedText.NULL_VALUE, new NodeId(0, 288), -1, null, UInteger.valueOf(0), false),
                new StructureField("TriggeringItemId", LocalizedText.NULL_VALUE, new NodeId(0, 288), -1, null, UInteger.valueOf(0), false),
                new StructureField("LinksToAdd", LocalizedText.NULL_VALUE, new NodeId(0, 288), 1, null, UInteger.valueOf(0), false),
                new StructureField("LinksToRemove", LocalizedText.NULL_VALUE, new NodeId(0, 288), 1, null, UInteger.valueOf(0), false)
            }
        );
    }

    public static final class Codec extends GenericDataTypeCodec<SetTriggeringRequest> {
        @Override
        public Class<SetTriggeringRequest> getType() {
            return SetTriggeringRequest.class;
        }

        @Override
        public SetTriggeringRequest decodeType(EncodingContext context, UaDecoder decoder) {
            RequestHeader requestHeader = (RequestHeader) decoder.decodeStruct("RequestHeader", RequestHeader.TYPE_ID);
            UInteger subscriptionId = decoder.decodeUInt32("SubscriptionId");
            UInteger triggeringItemId = decoder.decodeUInt32("TriggeringItemId");
            UInteger[] linksToAdd = decoder.decodeUInt32Array("LinksToAdd");
            UInteger[] linksToRemove = decoder.decodeUInt32Array("LinksToRemove");
            return new SetTriggeringRequest(requestHeader, subscriptionId, triggeringItemId, linksToAdd, linksToRemove);
        }

        @Override
        public void encodeType(EncodingContext context, UaEncoder encoder, SetTriggeringRequest value) {
            encoder.encodeStruct("RequestHeader", value.getRequestHeader(), RequestHeader.TYPE_ID);
            encoder.encodeUInt32("SubscriptionId", value.getSubscriptionId());
            encoder.encodeUInt32("TriggeringItemId", value.getTriggeringItemId());
            encoder.encodeUInt32Array("LinksToAdd", value.getLinksToAdd());
            encoder.encodeUInt32Array("LinksToRemove", value.getLinksToRemove());
        }
    }
}
