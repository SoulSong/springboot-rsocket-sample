package com.shf.rsocket.interceptor.metadata;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/6/18 1:27
 */
@Slf4j
public class DefaultMetadataPushReceiveInterceptor extends MetadataPushReceiveInterceptor {
    private final static MetadataPushReceiveHandler DEFAULT_METADATA_PUSH_RECEIVE_HANDLER = metadata -> {
        log.info("Received metadata content is : {}", metadata);
        return Mono.empty();
    };

    public DefaultMetadataPushReceiveInterceptor() {
        this(DEFAULT_METADATA_PUSH_RECEIVE_HANDLER);
    }

    public DefaultMetadataPushReceiveInterceptor(MetadataPushReceiveHandler metadataPushReceiveHandler) {
        super(metadataPushReceiveHandler);
    }
}
