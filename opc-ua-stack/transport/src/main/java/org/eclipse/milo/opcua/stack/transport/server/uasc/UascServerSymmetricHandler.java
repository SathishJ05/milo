/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.transport.server.uasc;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.channel.ChannelParameters;
import org.eclipse.milo.opcua.stack.core.channel.ChunkDecoder;
import org.eclipse.milo.opcua.stack.core.channel.ChunkDecoder.DecodedMessage;
import org.eclipse.milo.opcua.stack.core.channel.ChunkEncoder;
import org.eclipse.milo.opcua.stack.core.channel.ChunkEncoder.EncodedMessage;
import org.eclipse.milo.opcua.stack.core.channel.MessageAbortException;
import org.eclipse.milo.opcua.stack.core.channel.MessageDecodeException;
import org.eclipse.milo.opcua.stack.core.channel.MessageEncodeException;
import org.eclipse.milo.opcua.stack.core.channel.ServerSecureChannel;
import org.eclipse.milo.opcua.stack.core.channel.headers.HeaderDecoder;
import org.eclipse.milo.opcua.stack.core.channel.messages.MessageType;
import org.eclipse.milo.opcua.stack.core.encoding.binary.OpcUaBinaryDecoder;
import org.eclipse.milo.opcua.stack.core.encoding.binary.OpcUaBinaryEncoder;
import org.eclipse.milo.opcua.stack.core.types.UaRequestMessageType;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ResponseHeader;
import org.eclipse.milo.opcua.stack.core.types.structured.ServiceFault;
import org.eclipse.milo.opcua.stack.core.util.BufferUtil;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.eclipse.milo.opcua.stack.transport.server.ServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UascServerSymmetricHandler extends ByteToMessageCodec<UascServiceResponse> implements HeaderDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int maxChunkCount;
    private final int maxChunkSize;
    private List<ByteBuf> chunkBuffers;

    private final OpcUaBinaryEncoder binaryEncoder;
    private final OpcUaBinaryDecoder binaryDecoder;

    private final UascServerConfig config;
    private final ServerApplication application;
    private final ChannelParameters channelParameters;
    private final ChunkEncoder chunkEncoder;
    private final ChunkDecoder chunkDecoder;
    private final ServerSecureChannel secureChannel;

    UascServerSymmetricHandler(
        UascServerConfig config,
        ServerApplication application,
        ChannelParameters channelParameters,
        ChunkEncoder chunkEncoder,
        ChunkDecoder chunkDecoder,
        ServerSecureChannel secureChannel
    ) {

        this.config = config;
        this.application = application;
        this.channelParameters = channelParameters;
        this.chunkEncoder = chunkEncoder;
        this.chunkDecoder = chunkDecoder;
        this.secureChannel = secureChannel;

        binaryEncoder = new OpcUaBinaryEncoder(application.getEncodingContext());
        binaryDecoder = new OpcUaBinaryDecoder(application.getEncodingContext());

        maxChunkCount = channelParameters.getLocalMaxChunkCount();
        maxChunkSize = channelParameters.getLocalReceiveBufferSize();

        chunkBuffers = new ArrayList<>(maxChunkCount);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().addLast(new UascServiceRequestHandler(application));

        super.handlerAdded(ctx);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, UascServiceResponse response, ByteBuf buffer) throws Exception {
        sendServiceResponse(response, buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (buffer.readableBytes() >= HEADER_LENGTH) {
            int messageLength = getMessageLength(buffer, maxChunkSize);

            if (buffer.readableBytes() >= messageLength) {
                MessageType messageType = MessageType.fromMediumInt(
                    buffer.getMediumLE(buffer.readerIndex())
                );

                if (messageType == MessageType.SecureMessage) {
                    onSecureMessage(ctx, buffer.readSlice(messageLength), out);
                } else {
                    ctx.fireChannelRead(buffer.readRetainedSlice(messageLength));
                }
            }
        }
    }

    private void onSecureMessage(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws UaException {
        buffer.skipBytes(3); // Skip messageType

        char chunkType = (char) buffer.readByte();

        if (chunkType == 'A') {
            chunkBuffers.forEach(ByteBuf::release);
            chunkBuffers.clear();
        } else {
            buffer.skipBytes(4); // Skip messageSize

            long secureChannelId = buffer.readUnsignedIntLE();
            if (secureChannelId != secureChannel.getChannelId()) {
                throw new UaException(StatusCodes.Bad_SecureChannelIdInvalid,
                    "invalid secure channel id: " + secureChannelId);
            }

            int chunkSize = buffer.readerIndex(0).readableBytes();
            if (chunkSize > maxChunkSize) {
                throw new UaException(StatusCodes.Bad_TcpMessageTooLarge,
                    String.format("max chunk size exceeded (%s)", maxChunkSize));
            }

            chunkBuffers.add(buffer.retain());

            if (maxChunkCount > 0 && chunkBuffers.size() > maxChunkCount) {
                throw new UaException(StatusCodes.Bad_TcpMessageTooLarge,
                    String.format("max chunk count exceeded (%s)", maxChunkCount));
            }

            if (chunkType == 'F') {
                final List<ByteBuf> buffersToDecode = chunkBuffers;
                chunkBuffers = new ArrayList<>();

                ByteBuf message = null;

                try {
                    DecodedMessage decodedMessage =
                        chunkDecoder.decodeSymmetric(secureChannel, buffersToDecode);

                    message = decodedMessage.getMessage();
                    long requestId = decodedMessage.getRequestId();

                    binaryDecoder.setBuffer(message);
                    UaRequestMessageType requestMessage = (UaRequestMessageType) binaryDecoder.decodeMessage(null);

                    String endpointUrl = ctx.channel()
                        .attr(UascServerHelloHandler.ENDPOINT_URL_KEY)
                        .get();

                    EndpointDescription endpoint = ctx.channel()
                        .attr(UascServerAsymmetricHandler.ENDPOINT_KEY)
                        .get();

                    String path = EndpointUtil.getPath(endpointUrl);

                    InetSocketAddress remoteSocketAddress =
                        (InetSocketAddress) ctx.channel().remoteAddress();

                    var serviceRequest = new UascServiceRequest(
                        endpointUrl,
                        secureChannel,
                        requestMessage,
                        requestId
                    );

                    out.add(serviceRequest);
                } catch (MessageAbortException e) {
                    logger.warn(
                        "Received message abort chunk; error={}, reason={}",
                        e.getStatusCode(), e.getMessage()
                    );
                } catch (MessageDecodeException e) {
                    logger.error("Error decoding symmetric message", e);

                    ctx.close();
                } finally {
                    if (message != null) {
                        message.release();
                    }
                    buffersToDecode.clear();
                }
            }
        }
    }

    private void sendServiceResponse(UascServiceResponse response, ByteBuf outBuffer) {
        ByteBuf messageBuffer = BufferUtil.pooledBuffer();
        try {
            binaryEncoder.setBuffer(messageBuffer);
            binaryEncoder.encodeMessage(null, response.getResponseMessage());

            checkMessageSize(messageBuffer);

            EncodedMessage encodedMessage = chunkEncoder.encodeSymmetric(
                secureChannel,
                response.getRequestId(),
                messageBuffer,
                MessageType.SecureMessage
            );

            CompositeByteBuf chunkComposite = BufferUtil.compositeBuffer();

            for (ByteBuf chunk : encodedMessage.getMessageChunks()) {
                chunkComposite.addComponent(chunk);
                chunkComposite.writerIndex(chunkComposite.writerIndex() + chunk.readableBytes());
            }

            outBuffer.writeBytes(chunkComposite);
        } catch (MessageEncodeException e) {
            logger.error("Error encoding {}: {}", response, e.getMessage(), e);

            sendServiceFault(response, outBuffer, e);
        } catch (UaSerializationException e) {
            logger.error("Error serializing response: {}", e.getStatusCode(), e);

            sendServiceFault(response, outBuffer, e);
        } finally {
            messageBuffer.release();
        }
    }

    private void sendServiceFault(UascServiceResponse response, ByteBuf outBuffer, Exception fault) {
        StatusCode statusCode = UaException.extract(fault)
            .map(UaException::getStatusCode)
            .orElse(StatusCode.BAD);

        var serviceFault = new ServiceFault(
            new ResponseHeader(
                DateTime.now(),
                response.getResponseMessage().getResponseHeader().getRequestHandle(),
                statusCode,
                null, null, null
            )
        );

        ByteBuf messageBuffer = BufferUtil.pooledBuffer();

        try {
            binaryEncoder.setBuffer(messageBuffer);
            binaryEncoder.encodeMessage(null, serviceFault);

            checkMessageSize(messageBuffer);

            EncodedMessage encodedMessage = chunkEncoder.encodeSymmetric(
                secureChannel,
                response.getRequestId(),
                messageBuffer,
                MessageType.SecureMessage
            );

            CompositeByteBuf chunkComposite = BufferUtil.compositeBuffer();

            for (ByteBuf chunk : encodedMessage.getMessageChunks()) {
                chunkComposite.addComponent(chunk);
                chunkComposite.writerIndex(chunkComposite.writerIndex() + chunk.readableBytes());
            }

            outBuffer.writeBytes(chunkComposite);
        } catch (MessageEncodeException e) {
            logger.error("Error encoding {}: {}", serviceFault, e.getMessage(), e);
        } catch (UaSerializationException e) {
            logger.error("Error serializing ServiceFault: {}", e.getStatusCode(), e);
        } finally {
            messageBuffer.release();
        }
    }

    private void checkMessageSize(ByteBuf messageBuffer) throws UaSerializationException {
        int messageSize = messageBuffer.readableBytes();
        int remoteMaxMessageSize = channelParameters.getRemoteMaxMessageSize();

        if (remoteMaxMessageSize > 0 && messageSize > remoteMaxMessageSize) {
            throw new UaSerializationException(
                StatusCodes.Bad_ResponseTooLarge,
                "response exceeds remote max message size: " +
                    messageSize + " > " + remoteMaxMessageSize
            );
        }
    }

}