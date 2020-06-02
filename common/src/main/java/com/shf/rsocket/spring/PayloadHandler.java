package com.shf.rsocket.spring;

import com.shf.rsocket.entity.PayloadInfo;
import com.shf.rsocket.interceptor.PayloadExtractFunction;
import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.Map;

/**
 * description :
 * Handle {@link io.rsocket.Payload}
 *
 * @author songhaifeng
 * @date 2020/6/2 1:29
 */
public class PayloadHandler {
    private static final MimeType MESSAGE_RSOCKET_COMPOSITE_METADATA = MimeTypeUtils.parseMimeType(
            WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());

    /**
     * Define a function for extracting {@link io.rsocket.Payload} with {@link MetadataExtractor}
     *
     * @param metadataExtractor metadataExtractor
     * @return PayloadExtractFunction
     */
    public static PayloadExtractFunction payloadExtractFunction(MetadataExtractor metadataExtractor) {
        return (payload, extractData, extractMetadata) -> {
            PayloadInfo payloadInfo = new PayloadInfo();
            if (extractData) {
                payloadInfo.withData(payload.getDataUtf8());
            }

            if (extractMetadata) {
                if (payload.hasMetadata()) {
                    Map<String, Object> metadata = metadataExtractor.extract(payload, MESSAGE_RSOCKET_COMPOSITE_METADATA);
                    payloadInfo.withMetadata(metadata);
                }
            }
            return payloadInfo;
        };
    }

}
