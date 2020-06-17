package com.shf.rsocket.interceptor.metadata;

import io.rsocket.Payload;
import reactor.core.publisher.Mono;

/**
 * description :
 * Defined how to handle metadata_push event.
 *
 * @author songhaifeng
 * @date 2020/6/18 1:12
 */
public interface MetadataPushReceiveHandler {

    /**
     * Handle metadata string
     *
     * @param metadata extract from {@link Payload#getMetadataUtf8()}
     * @return Mono<Void>
     */
    Mono<Void> handle(String metadata);
}
