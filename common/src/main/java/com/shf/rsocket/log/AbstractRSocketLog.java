package com.shf.rsocket.log;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.plugins.LimitRateInterceptor;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * description :
 * Refer to {@link LimitRateInterceptor}
 *
 * @author songhaifeng
 * @date 2020/5/20 23:57
 */
@Slf4j
public abstract class AbstractRSocketLog implements RSocketLogInterceptor {

    static final String SEND = " send";
    static final String RECEIVE = " receive";


    abstract String getPrefix();

    abstract String getResponsePrefix();

    @Override
    public void log(Payload payload) {
        if (payload.hasMetadata()) {
            log.info("[{}], payload.data->{};payload.metadata->{};", getPrefix(), payload.getDataUtf8(), payload.getMetadataUtf8());
        } else {
            log.info("[{}], payload.data->{};", getPrefix(), payload.getDataUtf8());
        }
    }

    @Override
    public RSocket apply(RSocket rSocket) {
        return new RSocketProxy(rSocket) {
            @Override
            public Mono<Payload> requestResponse(final Payload payload) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                log(payload);
                Mono<Payload> responsePayload = super.requestResponse(payload);
                return responsePayload.doOnSuccess(response -> {
                    stopWatch.stop();
                    log.info("[{}-Response payload] {}, spent:{}ms", getResponsePrefix(), response.getDataUtf8(), stopWatch.getTotalTimeMillis());
                }).doFinally(signalType -> {
                    if (stopWatch.isRunning()) {
                        stopWatch.stop();
                    }
                });

            }

            @Override
            public Mono<Void> fireAndForget(Payload payload) {
                log(payload);
                return super.fireAndForget(payload);
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
                log(payload);
                return super.requestStream(payload);
            }

            @Override
            public Mono<Void> metadataPush(Payload payload) {
                log(payload);
                return super.metadataPush(payload);
            }
        };
    }
}
