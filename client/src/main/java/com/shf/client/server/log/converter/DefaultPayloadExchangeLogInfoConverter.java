package com.shf.client.server.log.converter;

import com.shf.rsocket.interceptor.log.entity.RequestLogInfo;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import io.rsocket.Payload;
import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.core.codec.Decoder;
import org.springframework.messaging.rsocket.DefaultMetadataExtractor;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.api.PayloadExchange;
import org.springframework.security.rsocket.authentication.BasicAuthenticationPayloadExchangeConverter;
import org.springframework.security.rsocket.authentication.BearerPayloadExchangeConverter;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.List;
import java.util.Map;

import static com.shf.client.configuration.RSocketConfiguration.METADATA_TO_EXTRACT_REF_LIST;

/**
 * Description:
 * Convert {@link Payload} to {@link RequestLogInfo} by {@link MetadataExtractor}.
 * <p>
 * Refer to {@link BasicAuthenticationPayloadExchangeConverter} and {@link BearerPayloadExchangeConverter}.
 *
 * @author songhaifeng
 * @date 2019/12/21 23:14
 */
public class DefaultPayloadExchangeLogInfoConverter implements PayloadExchangeLogInfoConverter {
    private final MimeType metadataMimetype = MimeTypeUtils.parseMimeType(
            WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());

    private final MetadataExtractor metadataExtractor;
    private final RSocketStrategies rSocketStrategies;

    public DefaultPayloadExchangeLogInfoConverter(RSocketStrategies rSocketStrategies) {
        Assert.notNull(rSocketStrategies, "RSocketStrategies must not be null.");
        this.rSocketStrategies = rSocketStrategies;
        metadataExtractor = createDefaultExtractor();
    }

    @Override
    public RequestLogInfo convert(PayloadExchange exchange) {
        RequestLogInfo.RequestLogInfoBuilder builder = RequestLogInfo.builder();
        Payload payload = exchange.getPayload();

        builder.data(new String(ByteBufUtil.getBytes(payload.data().slice()), CharsetUtil.UTF_8));
        if (payload.hasMetadata()) {
            Map<String, Object> metadata = metadataExtractor.extract(payload, metadataMimetype);
            builder.metadata(metadata);
        }
        return builder.build();
    }

    /**
     * Create a {@link MetadataExtractor} to extract all metadata from payload.
     *
     * @return {@link MetadataExtractor}
     */
    private MetadataExtractor createDefaultExtractor() {
        // Fetch already register decoders from rSocketStrategies.
        List<Decoder<?>> decoders = rSocketStrategies.decoders();
        // Create a new metadata-extractor instance with decoders upon.
        DefaultMetadataExtractor metadataExtractor = new DefaultMetadataExtractor(decoders.toArray(new Decoder[0]));
        // Register all MimeTypes with targetType or parameterizedTypeReference.
        METADATA_TO_EXTRACT_REF_LIST.forEach(metadataToExtractRef -> {
            if (null != metadataToExtractRef.getTargetType()) {
                metadataExtractor.metadataToExtract(metadataToExtractRef.getMimeType(), metadataToExtractRef.getTargetType(), metadataToExtractRef.getName());
            } else {
                metadataExtractor.metadataToExtract(metadataToExtractRef.getMimeType(), metadataToExtractRef.getParameterizedTypeReference(), metadataToExtractRef.getName());
            }
        });
        // Register simple_authentication_mimeType
        // {@code AuthenticationPayloadExchangeConverter} and {@code SimpleAuthenticationSpec#build}
        metadataExtractor.metadataToExtract(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()), byte[].class, "authentication");

        return metadataExtractor;
    }
}
