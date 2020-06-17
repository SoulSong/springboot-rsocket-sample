package com.shf.rsocket.interceptor.metadata;

import com.shf.rsocket.interceptor.OrderRSocketInterceptor;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * description :
 * Handle metadata_push event by the interceptor.
 *
 * @author songhaifeng
 * @date 2020/6/18 1:08
 */
@Slf4j
public class MetadataPushReceiveInterceptor implements OrderRSocketInterceptor {

    private MetadataPushReceiveHandler metadataPushReceiveHandler;

    public MetadataPushReceiveInterceptor(MetadataPushReceiveHandler metadataPushReceiveHandler) {
        this.metadataPushReceiveHandler = metadataPushReceiveHandler;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public RSocket apply(RSocket rSocket) {
        return new RSocketProxy(rSocket) {
            /**
             * In production, here needs to decode metadata from a json to a java-bean.
             *
             * @param payload payload
             * @return Mono<Void>
             */
            @Override
            public Mono<Void> metadataPush(Payload payload) {
                try {
                    if (payload.metadata().readableBytes() > 0) {
                        return metadataPushReceiveHandler.handle(payload.getMetadataUtf8());
                    }
                    return Mono.empty();
                } finally {
                    ReferenceCountUtil.safeRelease(payload);
                }
            }
        };
    }
}
