package com.shf.client.server.log.converter;

import com.shf.client.server.log.RequestLogInfo;

import io.netty.buffer.ByteBuf;
import io.rsocket.Payload;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.WellKnownMimeType;

import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.api.PayloadExchange;
import org.springframework.security.rsocket.authentication.BasicAuthenticationPayloadExchangeConverter;
import org.springframework.security.rsocket.authentication.BearerPayloadExchangeConverter;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import reactor.core.publisher.Mono;

/**
 * Description:
 * Refer to {@link BasicAuthenticationPayloadExchangeConverter} and {@link BearerPayloadExchangeConverter}.
 *
 * @author: songhaifeng
 * @date: 2019/12/21 23:14
 */
public class DefaultPayloadExchangeLogInfoConverter implements PayloadExchangeLogInfoConverter {
    private MimeType metadataMimetype = MimeTypeUtils.parseMimeType(
            WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());

    private RSocketStrategies rSocketStrategies;

    private MetadataExtractor metadataExtractor;

    public DefaultPayloadExchangeLogInfoConverter(RSocketStrategies rSocketStrategies) {
        Assert.notNull(rSocketStrategies, "RSocketStrategies must not be mull.");
        this.rSocketStrategies = rSocketStrategies;
        metadataExtractor = rSocketStrategies.metadataExtractor();
    }

    @Override
    public Mono<RequestLogInfo> convert(PayloadExchange exchange) {
        Payload payload = exchange.getPayload();
        ByteBuffer data = payload.getData();
        new String(data.array());
        ByteBuf metadata = payload.metadata();
        CompositeMetadata compositeMetadata = new CompositeMetadata(metadata, false);
        for (CompositeMetadata.Entry entry : compositeMetadata) {
            ByteBuf content = entry.getContent();
            String mimeType = entry.getMimeType();
            String token = content.toString(StandardCharsets.UTF_8);
        }
        return Mono.empty();
    }
}
