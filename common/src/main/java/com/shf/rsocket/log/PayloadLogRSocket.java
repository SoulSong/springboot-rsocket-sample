package com.shf.rsocket.log;

import com.shf.rsocket.log.entity.RequestLogInfo;
import com.shf.rsocket.log.entity.ResponseLogInfo;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * description :
 * Wrapper the {@link RSocket} for logging request payload(four interaction models) and response payload(only request/response model).
 *
 * @author songhaifeng
 * @date 2020/5/22 13:53
 */
@Slf4j
public class PayloadLogRSocket extends RSocketProxy {

    private final MetadataExtractor metadataExtractor;
    private final String requestPrefix;
    private final String responsePrefix;

    public PayloadLogRSocket(RSocket delegate, MetadataExtractor metadataExtractor, String requestPrefix, String responsePrefix) {
        super(delegate);
        this.metadataExtractor = metadataExtractor;
        this.requestPrefix = requestPrefix;
        this.responsePrefix = responsePrefix;
    }

    @Override
    public Mono<Payload> requestResponse(final Payload payload) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logRequest(payload);
        Mono<Payload> responsePayload = super.requestResponse(payload);
        return logResponse(stopWatch, responsePayload);
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        logRequest(payload);
        return super.fireAndForget(payload);
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        logRequest(payload);
        return super.requestStream(payload);
    }

    @Override
    public Mono<Void> metadataPush(Payload payload) {
        logRequest(payload);
        return super.metadataPush(payload);
    }

    /**
     * Log request payload.
     *
     * @param payload payload
     */
    private void logRequest(Payload payload) {
        RequestLogInfo.RequestLogInfoBuilder builder = RequestLogInfo.builder();

        builder.data(payload.getDataUtf8());
        if (payload.hasMetadata()) {
            Map<String, Object> metadata = metadataExtractor.extract(payload, RSocketLog.METADATA_MIME_TYPE);
            builder.metadata(metadata);
        }
        builder.build().log(requestPrefix);
    }

    /**
     * Log response payload.
     *
     * @param stopWatch       stopWatch
     * @param responsePayload responsePayload
     * @return Mono<Payload>
     */
    private Mono<Payload> logResponse(StopWatch stopWatch, Mono<Payload> responsePayload) {
        return responsePayload.doOnSuccess(response -> {
            ResponseLogInfo.ResponseLogInfoBuilder builder = ResponseLogInfo.builder();
            stopWatch.stop();
            builder.data(response.getDataUtf8()).spentTime(stopWatch.getTotalTimeMillis());
            builder.build().log(responsePrefix);
        }).doFinally(signalType -> {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
        });
    }
}