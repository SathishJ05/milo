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

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.encoding.DataTypeCodec;
import org.eclipse.milo.opcua.stack.core.encoding.EncodingContext;
import org.eclipse.milo.opcua.stack.core.encoding.UaEncoder;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDefaultBinaryEncoding;
import org.eclipse.milo.opcua.stack.core.types.UaEnumeratedType;
import org.eclipse.milo.opcua.stack.core.types.UaMessageType;
import org.eclipse.milo.opcua.stack.core.types.UaStructuredType;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.OptionSetUInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.IdType;
import org.eclipse.milo.opcua.stack.core.util.ArrayUtil;
import org.eclipse.milo.opcua.stack.core.util.TypeUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class OpcUaBinaryEncoder implements UaEncoder {

    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;
    private static final Charset CHARSET_UTF16 = StandardCharsets.UTF_16;

    private ByteBuf buffer;

    private int currentByte;
    private int bitCount;

    private final EncodingContext context;

    public OpcUaBinaryEncoder(EncodingContext context) {
        this.context = context;
    }

    public OpcUaBinaryEncoder setBuffer(ByteBuf buffer) {
        this.buffer = buffer;
        return this;
    }

    public <T> void writeArray(T[] values, Consumer<T> write) throws UaSerializationException {
        if (values == null) {
            buffer.writeIntLE(-1);
        } else {
            if (values.length > context.getEncodingLimits().getMaxMessageSize()) {
                throw new UaSerializationException(
                    StatusCodes.Bad_EncodingLimitsExceeded,
                    "array length exceeds max message size"
                );
            }

            writeInt32(values.length);

            for (T t : values) {
                write.accept(t);
            }
        }
    }

    public void writeBit(int value) throws UaSerializationException {
        currentByte = currentByte | ((value & 1) << bitCount);
        bitCount++;

        if (bitCount == 8) {
            buffer.writeByte(currentByte);
            currentByte = 0;
            bitCount = 0;
        }
    }

    public void writeBoolean(Boolean value) throws UaSerializationException {
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(value);
        }
    }

    public void writeSByte(Byte value) throws UaSerializationException {
        if (value == null) {
            buffer.writeByte(0);
        } else {
            buffer.writeByte(value);
        }
    }

    public void writeByte(UByte value) throws UaSerializationException {
        if (value == null) {
            buffer.writeByte(0);
        } else {
            buffer.writeByte(value.intValue());
        }
    }

    public void writeInt16(Short value) throws UaSerializationException {
        if (value == null) {
            buffer.writeShortLE(0);
        } else {
            buffer.writeShortLE(value);
        }
    }

    public void writeUInt16(UShort value) throws UaSerializationException {
        if (value == null) {
            buffer.writeShortLE(0);
        } else {
            buffer.writeShortLE(value.intValue());
        }
    }

    public void writeInt32(Integer value) throws UaSerializationException {
        if (value == null) {
            buffer.writeIntLE(0);
        } else {
            buffer.writeIntLE(value);
        }
    }

    public void writeUInt32(UInteger value) throws UaSerializationException {
        if (value == null) {
            buffer.writeIntLE(0);
        } else {
            buffer.writeIntLE(value.intValue());
        }
    }

    public void writeInt64(Long value) throws UaSerializationException {
        if (value == null) {
            buffer.writeLongLE(0L);
        } else {
            buffer.writeLongLE(value);
        }
    }

    public void writeUInt64(ULong value) throws UaSerializationException {
        if (value == null) {
            buffer.writeLongLE(0L);
        } else {
            buffer.writeLongLE(value.longValue());
        }
    }

    public void writeFloat(Float value) throws UaSerializationException {
        if (value == null) {
            buffer.writeFloatLE(0f);
        } else {
            buffer.writeFloatLE(value);
        }
    }

    public void writeDouble(Double value) throws UaSerializationException {
        if (value == null) {
            buffer.writeDoubleLE(0d);
        } else {
            buffer.writeDoubleLE(value);
        }
    }

    public void writeCharacter(Character value) throws UaSerializationException {
        buffer.writeByte(value & 0xFF);
    }

    public void writeWideChar(Character value) throws UaSerializationException {
        buffer.writeChar(value);
    }

    public void writeUtf8NullTerminatedString(String value) throws UaSerializationException {
        if (value != null) {
            byte[] bytes = value.getBytes(CHARSET_UTF8);
            for (byte b : bytes) {
                buffer.writeByte(b);
                if (b == 0) return;
            }
        }
        buffer.writeByte(0);
    }

    public void writeUtf8CharArray(String value) throws UaSerializationException {
        if (value == null) {
            writeInt32(-1);
        } else {
            writeLengthPrefixedString(value, CHARSET_UTF8);
        }
    }

    public void writeUtf16NullTerminatedString(String value) throws UaSerializationException {
        if (value == null) {
            buffer.writeByte(0);
        } else {
            for (int i = 0; i < value.length(); i++) {
                int c = value.codePointAt(i);
                buffer.writeChar(c);
                if (c == 0) return;
            }
            buffer.writeChar(0);
        }
    }

    public void writeUtf16CharArray(String value) throws UaSerializationException {
        if (value == null) {
            writeInt32(-1);
        } else {
            writeLengthPrefixedString(value, CHARSET_UTF16);
        }
    }

    public void writeDateTime(DateTime value) throws UaSerializationException {
        if (value == null) {
            buffer.writeLongLE(0L);
        } else {
            buffer.writeLongLE(value.getUtcTime());
        }
    }

    public void writeByteString(ByteString value) throws UaSerializationException {
        if (value == null || value.isNull()) {
            buffer.writeIntLE(-1);
        } else {
            byte[] bytes = value.bytes();

            assert (bytes != null);

            buffer.writeIntLE(bytes.length);
            buffer.writeBytes(bytes);
        }
    }

    public void writeGuid(UUID value) throws UaSerializationException {
        if (value == null) {
            buffer.writeZero(16);
        } else {
            long msb = value.getMostSignificantBits();
            long lsb = value.getLeastSignificantBits();

            buffer.writeIntLE((int) (msb >>> 32));
            buffer.writeShortLE((int) (msb >>> 16) & 0xFFFF);
            buffer.writeShortLE((int) (msb) & 0xFFFF);

            buffer.writeLong(lsb); // intentionally Big Endian
        }
    }

    // region OPC Built-in Types

    public void writeXmlElement(XmlElement value) throws UaSerializationException {
        if (value == null || value.isNull()) {
            buffer.writeIntLE(-1);
        } else {
            writeByteString(new ByteString(value.getFragment().getBytes(StandardCharsets.UTF_8)));
        }
    }

    public void writeDataValue(DataValue value) throws UaSerializationException {
        if (value == null) {
            buffer.writeByte(0);
        } else {
            int mask = 0x00;

            if (value.getValue() != null && value.getValue().isNotNull()) {
                mask |= 0x01;
            }

            if (!StatusCode.GOOD.equals(value.getStatusCode())) {
                mask |= 0x02;
            }

            if (value.getSourceTime() != null &&
                !DateTime.MIN_VALUE.equals(value.getSourceTime())) {

                mask |= 0x04;
            }

            if (value.getServerTime() != null &&
                !DateTime.MIN_VALUE.equals(value.getServerTime())) {

                mask |= 0x08;
            }

            if (value.getSourcePicoseconds() != null &&
                value.getSourcePicoseconds().intValue() != 0) {

                mask |= 0x10;
            }

            if (value.getServerPicoseconds() != null &&
                value.getServerPicoseconds().intValue() != 0) {

                mask |= 0x20;
            }

            buffer.writeByte(mask);

            // Value
            if ((mask & 0x01) == 0x01) {
                writeVariant(value.getValue());
            }

            // StatusCode
            if ((mask & 0x02) == 0x02) {
                writeStatusCode(value.getStatusCode());
            }

            // SourceTimestamp and SourcePicoseconds
            if ((mask & 0x04) == 0x04) {
                writeDateTime(value.getSourceTime());
            }
            if ((mask & 0x10) == 0x10) {
                writeUInt16(value.getSourcePicoseconds());
            }

            // ServerTimestamp and ServerPicoseconds
            if ((mask & 0x08) == 0x08) {
                writeDateTime(value.getServerTime());
            }
            if ((mask & 0x20) == 0x20) {
                writeUInt16(value.getServerPicoseconds());
            }
        }
    }

    public void writeDiagnosticInfo(DiagnosticInfo value) throws UaSerializationException {
        if (value == null) {
            buffer.writeByte(0);
        } else {
            int mask = 0x7F;

            if (value.getSymbolicId() == -1) mask ^= 0x01;
            if (value.getNamespaceUri() == -1) mask ^= 0x02;
            if (value.getLocalizedText() == -1) mask ^= 0x04;
            if (value.getLocale() == -1) mask ^= 0x08;
            if (value.getAdditionalInfo() == null || value.getAdditionalInfo().isEmpty()) mask ^= 0x10;
            if (value.getInnerStatusCode() == null) mask ^= 0x20;
            if (value.getInnerDiagnosticInfo() == null) mask ^= 0x40;

            buffer.writeByte(mask);

            if ((mask & 0x01) == 0x01) writeInt32(value.getSymbolicId());
            if ((mask & 0x02) == 0x02) writeInt32(value.getNamespaceUri());
            if ((mask & 0x04) == 0x04) writeInt32(value.getLocalizedText());
            if ((mask & 0x08) == 0x08) writeInt32(value.getLocale());
            if ((mask & 0x10) == 0x10) writeString(value.getAdditionalInfo());
            if ((mask & 0x20) == 0x20) writeStatusCode(value.getInnerStatusCode());
            if ((mask & 0x40) == 0x40) writeDiagnosticInfo(value.getInnerDiagnosticInfo());
        }
    }

    public void writeExpandedNodeId(ExpandedNodeId value) throws UaSerializationException {
        if (value == null) value = ExpandedNodeId.NULL_VALUE;

        int flags = 0;
        String namespaceUri = value.getNamespaceUri();
        UInteger serverIndex = value.getServerIndex();

        if (namespaceUri != null && namespaceUri.length() > 0) {
            flags |= 0x80;
        }

        if (serverIndex.longValue() > 0) {
            flags |= 0x40;
        }

        UShort index = value.getNamespaceIndex();
        if (index == null) index = UShort.MIN;
        int namespaceIndex = index.intValue();

        if (value.getType() == IdType.Numeric) {
            UInteger identifier = (UInteger) value.getIdentifier();
            long idv = identifier.longValue();

            if (namespaceIndex == 0 && idv >= 0 && idv <= 255) {
                /* Two-byte format */
                buffer.writeByte(flags);
                buffer.writeByte((int) idv);
            } else if (namespaceIndex >= 0 && namespaceIndex <= 255 && idv <= 65535) {
                /* Four-byte format */
                buffer.writeByte(0x01 | flags);
                buffer.writeByte(namespaceIndex);
                buffer.writeShortLE((int) idv);
            } else {
                /* Numeric format */
                buffer.writeByte(0x02 | flags);
                buffer.writeShortLE(namespaceIndex);
                buffer.writeIntLE((int) idv);
            }
        } else if (value.getType() == IdType.String) {
            String identifier = (String) value.getIdentifier();

            buffer.writeByte(0x03 | flags);
            buffer.writeShortLE(namespaceIndex);
            writeString(identifier);
        } else if (value.getType() == IdType.Guid) {
            UUID identifier = (UUID) value.getIdentifier();

            buffer.writeByte(0x04 | flags);
            buffer.writeShortLE(namespaceIndex);
            writeGuid(identifier);
        } else if (value.getType() == IdType.Opaque) {
            ByteString identifier = (ByteString) value.getIdentifier();

            buffer.writeByte(0x05 | flags);
            buffer.writeShortLE(namespaceIndex);
            writeByteString(identifier);
        } else {
            throw new UaSerializationException(
                StatusCodes.Bad_EncodingError,
                "invalid identifier: " + value.getIdentifier());
        }

        if (namespaceUri != null && namespaceUri.length() > 0) {
            writeString(namespaceUri);
        }

        if (serverIndex.longValue() > 0) {
            writeUInt32(serverIndex);
        }
    }

    public void writeExtensionObject(ExtensionObject value) throws UaSerializationException {
        if (value == null || value.getBody() == null) {
            writeNodeId(NodeId.NULL_VALUE);
            buffer.writeByte(0); // No body is encoded
        } else {
            Object body = value.getBody();

            switch (value.getBodyType()) {
                case ByteString: {
                    ByteString byteString = (ByteString) body;

                    writeNodeId(value.getEncodingId());
                    buffer.writeByte(1); // Body is binary encoded
                    writeByteString(byteString);

                    break;
                }
                case XmlElement: {
                    XmlElement xmlElement = (XmlElement) body;

                    writeNodeId(value.getEncodingId());
                    buffer.writeByte(2);
                    writeXmlElement(xmlElement);

                    break;
                }
                case JsonString: {
                    String jsonString = (String) body;

                    writeNodeId(value.getEncodingId());
                    buffer.writeByte(1);
                    writeByteString(ByteString.of(jsonString.getBytes(StandardCharsets.UTF_8)));

                    break;
                }

                default:
                    throw new IllegalStateException("unknown body type: " + value.getBodyType());
            }
        }
    }

    public void writeLocalizedText(LocalizedText value) throws UaSerializationException {
        if (value == null) value = LocalizedText.NULL_VALUE;

        String locale = value.getLocale();
        String text = value.getText();

        int mask = 1 | 2;
        if (locale == null || locale.isEmpty()) {
            mask ^= 1;
        }
        if (text == null || text.isEmpty()) {
            mask ^= 2;
        }

        buffer.writeByte(mask);

        if (locale != null && !locale.isEmpty()) {
            writeString(locale);
        }
        if (text != null && !text.isEmpty()) {
            writeString(text);
        }
    }

    public void writeNodeId(NodeId value) throws UaSerializationException {
        if (value == null) value = NodeId.NULL_VALUE;

        int namespaceIndex = value.getNamespaceIndex().intValue();

        if (value.getType() == IdType.Numeric) {
            UInteger identifier = (UInteger) value.getIdentifier();
            long idv = identifier.longValue();

            if (namespaceIndex == 0 && idv >= 0 && idv <= 255) {
                /* Two-byte format */
                buffer.writeByte(0x00);
                buffer.writeByte((int) idv);
            } else if (namespaceIndex >= 0 && namespaceIndex <= 255 && idv <= 65535) {
                /* Four-byte format */
                buffer.writeByte(0x01);
                buffer.writeByte(namespaceIndex);
                buffer.writeShortLE((int) idv);
            } else {
                /* Numeric format */
                buffer.writeByte(0x02);
                buffer.writeShortLE(namespaceIndex);
                buffer.writeIntLE((int) idv);
            }
        } else if (value.getType() == IdType.String) {
            String identifier = (String) value.getIdentifier();

            buffer.writeByte(0x03);
            buffer.writeShortLE(namespaceIndex);
            writeString(identifier);
        } else if (value.getType() == IdType.Guid) {
            UUID identifier = (UUID) value.getIdentifier();

            buffer.writeByte(0x04);
            buffer.writeShortLE(namespaceIndex);
            writeGuid(identifier);
        } else if (value.getType() == IdType.Opaque) {
            ByteString identifier = (ByteString) value.getIdentifier();

            buffer.writeByte(0x05);
            buffer.writeShortLE(namespaceIndex);
            writeByteString(identifier);
        } else {
            throw new UaSerializationException(
                StatusCodes.Bad_EncodingError,
                "invalid identifier: " + value.getIdentifier());
        }
    }

    public void writeQualifiedName(QualifiedName value) throws UaSerializationException {
        if (value == null) value = QualifiedName.NULL_VALUE;

        writeUInt16(value.getNamespaceIndex());
        writeString(value.getName());
    }

    public void writeString(String value) throws UaSerializationException {
        if (value == null) {
            buffer.writeIntLE(-1);
        } else {
            writeLengthPrefixedString(value, CHARSET_UTF8);
        }
    }

    public void writeStatusCode(StatusCode value) throws UaSerializationException {
        if (value == null) {
            buffer.writeIntLE(0);
        } else {
            buffer.writeIntLE((int) value.getValue());
        }
    }

    public void writeVariant(Variant variant) throws UaSerializationException {
        Object value = variant.getValue();

        if (value == null) {
            buffer.writeByte(0);
        } else {
            boolean structure = false;
            boolean enumeration = false;
            boolean optionSet = false;
            Class<?> valueClass = getClass(value);

            if (UaStructuredType.class.isAssignableFrom(valueClass)) {
                valueClass = ExtensionObject.class;
                structure = true;
            } else if (UaEnumeratedType.class.isAssignableFrom(valueClass)) {
                valueClass = Integer.class;
                enumeration = true;
            } else if (OptionSetUInteger.class.isAssignableFrom(valueClass)) {
                valueClass = ((OptionSetUInteger<?>) value).getValue().getClass();
                optionSet = true;
            }

            int typeId = TypeUtil.getBuiltinTypeId(valueClass);

            if (typeId == -1) {
                LoggerFactory.getLogger(getClass())
                    .warn("Not a built-in type: {}", valueClass);
            }

            if (value.getClass().isArray()) {
                int[] dimensions = ArrayUtil.getDimensions(value);

                if (dimensions.length == 1) {
                    buffer.writeByte(typeId | 0x80);

                    int length = Array.getLength(value);
                    buffer.writeIntLE(length);

                    for (int i = 0; i < length; i++) {
                        Object o = Array.get(value, i);

                        writeValue(o, typeId, structure, enumeration, optionSet);
                    }
                } else {
                    buffer.writeByte(typeId | 0xC0);

                    Object flattened = ArrayUtil.flatten(value);
                    int length = Array.getLength(flattened);
                    buffer.writeIntLE(length);

                    for (int i = 0; i < length; i++) {
                        Object o = Array.get(flattened, i);

                        writeValue(o, typeId, structure, enumeration, optionSet);
                    }

                    writeInt32(dimensions.length);
                    for (int dimension : dimensions) {
                        writeInt32(dimension);
                    }
                }
            } else {
                buffer.writeByte(typeId);

                writeValue(value, typeId, structure, enumeration, optionSet);
            }
        }
    }

    // endregion

    private void writeValue(Object value, int typeId, boolean structure, boolean enumeration, boolean optionSet) {
        if (structure) {
            UaStructuredType struct = (UaStructuredType) value;

            ExtensionObject extensionObject = ExtensionObject.encode(context, struct);

            writeBuiltinType(typeId, extensionObject);
        } else if (enumeration) {
            writeBuiltinType(typeId, ((UaEnumeratedType) value).getValue());
        } else if (optionSet) {
            writeBuiltinType(typeId, ((OptionSetUInteger<?>) value).getValue());
        } else {
            writeBuiltinType(typeId, value);
        }
    }

    private void writeBuiltinType(int typeId, Object value) throws UaSerializationException {
        switch (typeId) {
            case 1:
                encodeBoolean(null, (Boolean) value);
                break;
            case 2:
                writeSByte((Byte) value);
                break;
            case 3:
                writeByte((UByte) value);
                break;
            case 4:
                writeInt16((Short) value);
                break;
            case 5:
                writeUInt16((UShort) value);
                break;
            case 6:
                writeInt32((Integer) value);
                break;
            case 7:
                writeUInt32((UInteger) value);
                break;
            case 8:
                writeInt64((Long) value);
                break;
            case 9:
                writeUInt64((ULong) value);
                break;
            case 10:
                writeFloat((Float) value);
                break;
            case 11:
                writeDouble((Double) value);
                break;
            case 12:
                writeString((String) value);
                break;
            case 13:
                writeDateTime((DateTime) value);
                break;
            case 14:
                writeGuid((UUID) value);
                break;
            case 15:
                writeByteString((ByteString) value);
                break;
            case 16:
                writeXmlElement((XmlElement) value);
                break;
            case 17:
                writeNodeId((NodeId) value);
                break;
            case 18:
                writeExpandedNodeId((ExpandedNodeId) value);
                break;
            case 19:
                writeStatusCode((StatusCode) value);
                break;
            case 20:
                writeQualifiedName((QualifiedName) value);
                break;
            case 21:
                writeLocalizedText((LocalizedText) value);
                break;
            case 22:
                writeExtensionObject((ExtensionObject) value);
                break;
            case 23:
                writeDataValue((DataValue) value);
                break;
            case 24:
                writeVariant((Variant) value);
                break;
            case 25:
                writeDiagnosticInfo((DiagnosticInfo) value);
                break;
            default:
                throw new UaSerializationException(
                    StatusCodes.Bad_EncodingError,
                    "unknown builtin type: " + typeId);
        }
    }

    private Class<?> getClass(@NotNull Object o) {
        if (o.getClass().isArray()) {
            return ArrayUtil.getType(o);
        } else {
            return o.getClass();
        }
    }

    private void writeLengthPrefixedString(String value, Charset charset) throws UaSerializationException {
        byte[] bytes = value.getBytes(charset);
        int length = bytes.length;

        if (length > context.getEncodingLimits().getMaxMessageSize()) {
            throw new UaSerializationException(
                StatusCodes.Bad_EncodingLimitsExceeded,
                String.format(
                    "string length exceeds max message size (length=%s, max=%s)",
                    length, context.getEncodingLimits().getMaxMessageSize())
            );
        }

        writeInt32(length);
        buffer.writeBytes(bytes);
    }

    @Override
    public void encodeBoolean(String field, Boolean value) throws UaSerializationException {
        writeBoolean(value);
    }

    @Override
    public void encodeSByte(String field, Byte value) throws UaSerializationException {
        writeSByte(value);
    }

    @Override
    public void encodeInt16(String field, Short value) throws UaSerializationException {
        writeInt16(value);
    }

    @Override
    public void encodeInt32(String field, Integer value) throws UaSerializationException {
        writeInt32(value);
    }

    @Override
    public void encodeInt64(String field, Long value) throws UaSerializationException {
        writeInt64(value);
    }

    @Override
    public void encodeByte(String field, UByte value) throws UaSerializationException {
        writeByte(value);
    }

    @Override
    public void encodeUInt16(String field, UShort value) throws UaSerializationException {
        writeUInt16(value);
    }

    @Override
    public void encodeUInt32(String field, UInteger value) throws UaSerializationException {
        writeUInt32(value);
    }

    @Override
    public void encodeUInt64(String field, ULong value) throws UaSerializationException {
        writeUInt64(value);
    }

    @Override
    public void encodeFloat(String field, Float value) throws UaSerializationException {
        writeFloat(value);
    }

    @Override
    public void encodeDouble(String field, Double value) throws UaSerializationException {
        writeDouble(value);
    }

    @Override
    public void encodeString(String field, String value) throws UaSerializationException {
        writeString(value);
    }

    @Override
    public void encodeDateTime(String field, DateTime value) throws UaSerializationException {
        writeDateTime(value);
    }

    @Override
    public void encodeGuid(String field, UUID value) throws UaSerializationException {
        writeGuid(value);
    }

    @Override
    public void encodeByteString(String field, ByteString value) throws UaSerializationException {
        writeByteString(value);
    }

    @Override
    public void encodeXmlElement(String field, XmlElement value) throws UaSerializationException {
        writeXmlElement(value);
    }

    @Override
    public void encodeNodeId(String field, NodeId value) throws UaSerializationException {
        writeNodeId(value);
    }

    @Override
    public void encodeExpandedNodeId(String field, ExpandedNodeId value) throws UaSerializationException {
        writeExpandedNodeId(value);
    }

    @Override
    public void encodeStatusCode(String field, StatusCode value) throws UaSerializationException {
        writeStatusCode(value);
    }

    @Override
    public void encodeQualifiedName(String field, QualifiedName value) throws UaSerializationException {
        writeQualifiedName(value);
    }

    @Override
    public void encodeLocalizedText(String field, LocalizedText value) throws UaSerializationException {
        writeLocalizedText(value);
    }

    @Override
    public void encodeExtensionObject(String field, ExtensionObject value) throws UaSerializationException {
        writeExtensionObject(value);
    }

    @Override
    public void encodeDataValue(String field, DataValue value) throws UaSerializationException {
        writeDataValue(value);
    }

    @Override
    public void encodeVariant(String field, Variant value) throws UaSerializationException {
        writeVariant(value);
    }

    @Override
    public void encodeDiagnosticInfo(String field, DiagnosticInfo value) throws UaSerializationException {
        writeDiagnosticInfo(value);
    }

    @Override
    public void encodeMessage(String field, UaMessageType message) throws UaSerializationException {
        ExpandedNodeId xBinaryEncodingId = message.getBinaryEncodingId();

        NodeId encodingId = xBinaryEncodingId.toNodeId(context.getNamespaceTable())
            .orElseThrow(
                () ->
                    new UaSerializationException(
                        StatusCodes.Bad_EncodingError,
                        "namespace not registered: " + xBinaryEncodingId.getNamespaceUri())
            );

        DataTypeCodec codec = context.getDataTypeManager().getCodec(encodingId);

        if (codec == null) {
            throw new UaSerializationException(
                StatusCodes.Bad_EncodingError,
                "no codec registered: " + encodingId
            );
        }

        writeNodeId(encodingId);

        codec.encode(context, this, message);
    }

    @Override
    public void encodeEnum(String field, UaEnumeratedType value) {
        encodeInt32(field, value.getValue());
    }

    @Override
    public void encodeStruct(String field, Object value, NodeId dataTypeId) throws UaSerializationException {
        try {
            DataTypeCodec codec = context.getDataTypeManager()
                .getCodec(OpcUaDefaultBinaryEncoding.ENCODING_NAME, dataTypeId);

            if (codec != null) {
                codec.encode(context, this, value);
            } else {
                throw new UaSerializationException(
                    StatusCodes.Bad_EncodingError,
                    "no codec registered: " + dataTypeId
                );
            }
        } catch (ClassCastException e) {
            throw new UaSerializationException(StatusCodes.Bad_EncodingError, e);
        }
    }

    @Override
    public void encodeStruct(String field, Object value, ExpandedNodeId dataTypeId) throws UaSerializationException {
        NodeId localDataTypeId = dataTypeId.toNodeId(context.getNamespaceTable())
            .orElseThrow(() -> new UaSerializationException(
                StatusCodes.Bad_EncodingError,
                "namespace not not registered: " + dataTypeId
            ));

        encodeStruct(field, value, localDataTypeId);
    }

    @Override
    public void encodeStruct(String field, Object value, DataTypeCodec codec) throws UaSerializationException {
        codec.encode(context, this, value);
    }

    @Override
    public void encodeBooleanArray(String field, Boolean[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeBoolean);
    }

    @Override
    public void encodeSByteArray(String field, Byte[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeSByte);
    }

    @Override
    public void encodeInt16Array(String field, Short[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeInt16);
    }

    @Override
    public void encodeInt32Array(String field, Integer[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeInt32);
    }

    @Override
    public void encodeInt64Array(String field, Long[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeInt64);
    }

    @Override
    public void encodeByteArray(String field, UByte[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeByte);
    }

    @Override
    public void encodeUInt16Array(String field, UShort[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeUInt16);
    }

    @Override
    public void encodeUInt32Array(String field, UInteger[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeUInt32);
    }

    @Override
    public void encodeUInt64Array(String field, ULong[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeUInt64);
    }

    @Override
    public void encodeFloatArray(String field, Float[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeFloat);
    }

    @Override
    public void encodeDoubleArray(String field, Double[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeDouble);
    }

    @Override
    public void encodeStringArray(String field, String[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeString);
    }

    @Override
    public void encodeDateTimeArray(String field, DateTime[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeDateTime);
    }

    @Override
    public void encodeGuidArray(String field, UUID[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeGuid);
    }

    @Override
    public void encodeByteStringArray(String field, ByteString[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeByteString);
    }

    @Override
    public void encodeXmlElementArray(String field, XmlElement[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeXmlElement);
    }

    @Override
    public void encodeNodeIdArray(String field, NodeId[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeNodeId);
    }

    @Override
    public void encodeExpandedNodeIdArray(String field, ExpandedNodeId[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeExpandedNodeId);
    }

    @Override
    public void encodeStatusCodeArray(String field, StatusCode[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeStatusCode);
    }

    @Override
    public void encodeQualifiedNameArray(String field, QualifiedName[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeQualifiedName);
    }

    @Override
    public void encodeLocalizedTextArray(String field, LocalizedText[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeLocalizedText);
    }

    @Override
    public void encodeExtensionObjectArray(String field, ExtensionObject[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeExtensionObject);
    }

    @Override
    public void encodeDataValueArray(String field, DataValue[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeDataValue);
    }

    @Override
    public void encodeVariantArray(String field, Variant[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeVariant);
    }

    @Override
    public void encodeDiagnosticInfoArray(String field, DiagnosticInfo[] value) throws UaSerializationException {
        encodeArray(field, value, this::encodeDiagnosticInfo);
    }

    @Override
    public void encodeEnumArray(String field, UaEnumeratedType[] value) throws UaSerializationException {
        writeArray(value, v -> encodeEnum(field, v));
    }

    @Override
    public void encodeStructArray(
        String field, Object[] values, NodeId dataTypeId) throws UaSerializationException {

        writeArray(values, o -> encodeStruct(field, o, dataTypeId));
    }

    @Override
    public void encodeStructArray(
        String field,
        Object[] value,
        ExpandedNodeId dataTypeId
    ) throws UaSerializationException {

        NodeId localDataTypeId = dataTypeId.toNodeId(context.getNamespaceTable())
            .orElseThrow(() -> new UaSerializationException(
                StatusCodes.Bad_EncodingError,
                "no codec registered: " + dataTypeId
            ));

        encodeStructArray(field, value, localDataTypeId);
    }

    @Override
    public <T> void encodeArray(
        String field, T[] values, BiConsumer<String, T> encoder) throws UaSerializationException {

        if (values == null) {
            buffer.writeIntLE(-1);
        } else {
            if (values.length > context.getEncodingLimits().getMaxMessageSize()) {
                throw new UaSerializationException(
                    StatusCodes.Bad_EncodingLimitsExceeded,
                    "array length exceeds max message size"
                );
            }

            writeInt32(values.length);

            for (T t : values) {
                encoder.accept(field, t);
            }
        }
    }

}