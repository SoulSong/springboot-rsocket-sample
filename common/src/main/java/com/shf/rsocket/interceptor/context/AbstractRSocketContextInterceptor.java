package com.shf.rsocket.interceptor.context;

import com.shf.entity.context.MetadataContextHolder;
import com.shf.rsocket.interceptor.PayloadExtractFunction;
import com.shf.rsocket.interceptor.PayloadUtils;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * description :
 * Default {@link RSocketInterceptor} for store metadata into {@link reactor.util.context.Context}.
 *
 * @author songhaifeng
 * @date 2020/5/28 0:23
 */
@Slf4j
public abstract class AbstractRSocketContextInterceptor implements RSocketContextInterceptor {
    private final PayloadExtractFunction payloadExtractFunction;

    public AbstractRSocketContextInterceptor(@NonNull PayloadExtractFunction payloadExtractFunction) {
        this.payloadExtractFunction = payloadExtractFunction;
    }

    @Override
    public RSocket apply(RSocket rSocket) {
        return new RSocketProxy(rSocket) {
            @Override
            public Mono<Void> fireAndForget(Payload payload) {
                int refCnt = ReferenceCountUtil.refCnt(payload);
                // set refCnt, otherwise data and metadata will be null
                ReferenceCountUtil.retain(payload);
                return handle(super.fireAndForget(payload), payload)
                        .doFinally(signalType -> {
                            if (ReferenceCountUtil.refCnt(payload) != refCnt) {
                                ReferenceCountUtil.release(payload);
                            }
                        });
            }

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                return super.requestResponse(payload)
                        .subscriberContext(context -> MetadataContextHolder.setContext(PayloadUtils.extractMetadata(payloadExtractFunction, payload)))
                        .doFinally(signalType -> MetadataContextHolder.clearContext().subscribe())
                        .doOnError(this::logError);
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
                return super.requestStream(payload)
                        .subscriberContext(context -> MetadataContextHolder.setContext(PayloadUtils.extractMetadata(payloadExtractFunction, payload)))
                        .doFinally(signalType -> MetadataContextHolder.clearContext())
                        .doOnError(this::logError);
            }

            /**
             * refer to {@code MessagingRSocket#requestChannel}
             *
             * @param payloads payloads
             * @return Flux
             */
            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                return Flux.from(payloads)
                        .switchOnFirst((signal, innerFlux) -> {
                            Payload firstPayload = signal.get();
                            return firstPayload == null ? innerFlux :
                                    handleAndReply(innerFlux, firstPayload);
                        });
            }

            @Override
            public Mono<Void> metadataPush(Payload payload) {
                return handle(super.metadataPush(payload), payload);
            }

            private Mono<Void> handle(Mono<Void> mono, Payload payload) {
                return mono
                        .subscriberContext(context -> MetadataContextHolder.setContext(PayloadUtils.extractMetadata(payloadExtractFunction, payload)))
                        .doFinally(signalType -> MetadataContextHolder.clearContext())
                        .doOnError(this::logError);
            }

            private Flux<Payload> handleAndReply(Flux<Payload> payloads, Payload firstPayload) {
                return super.requestChannel(payloads)
                        .subscriberContext(context -> MetadataContextHolder.setContext(PayloadUtils.extractMetadata(payloadExtractFunction, firstPayload)))
                        .doFinally(signalType -> MetadataContextHolder.clearContext())
                        .doOnError(this::logError);
            }

            private void logError(Throwable error) {
                log.error("Extract metadata fail, error : {}", error.getMessage());
            }

        };
    }
}
