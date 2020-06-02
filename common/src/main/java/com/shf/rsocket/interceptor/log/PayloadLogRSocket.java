package com.shf.rsocket.interceptor.log;

import com.shf.rsocket.entity.PayloadInfo;
import com.shf.rsocket.interceptor.PayloadExtractFunction;
import com.shf.rsocket.interceptor.log.entity.RequestLogInfo;
import com.shf.rsocket.interceptor.log.entity.ResponseLogInfo;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

/**
 * description :
 * Wrapper the {@link RSocket} for logging request payload(four interaction models) and response payload(only request/response model).
 *
 * @author songhaifeng
 * @date 2020/5/22 13:53
 */
@Slf4j
public class PayloadLogRSocket extends RSocketProxy {

    private final String requestPrefix;
    private final String responsePrefix;
    private final PayloadExtractFunction payloadExtractFunction;

    public PayloadLogRSocket(RSocket delegate, PayloadExtractFunction payloadExtractFunction, String requestPrefix, String responsePrefix) {
        super(delegate);
        this.payloadExtractFunction = payloadExtractFunction;
        this.requestPrefix = requestPrefix;
        this.responsePrefix = responsePrefix;
    }

    /**
     * Not care response
     *
     * @param payload payload
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        return super.fireAndForget(payload)
                .doFirst(() -> logRequest(payload));
    }

    /**
     * log request and response
     *
     * @param payload payload
     * @return Mono<Payload>
     */
    @Override
    public Mono<Payload> requestResponse(final Payload payload) {
        long startTime = System.currentTimeMillis();
        return logResponse(startTime, super.requestResponse(payload))
                .doFirst(() -> logRequest(payload));
    }

    /**
     * log request and response status
     *
     * @param payload payload
     * @return Flux<Payload>
     */
    @Override
    public Flux<Payload> requestStream(Payload payload) {
        long startTime = System.currentTimeMillis();
        return super.requestStream(payload)
                .doFirst(() -> logRequest(payload))
                .doFinally(signalType -> logResponseStatus(signalType, startTime));
    }

    /**
     * log response status
     *
     * @param payloads payloads
     * @return Flux<Payload>
     */
    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        long startTime = System.currentTimeMillis();
        return super.requestChannel(payloads)
                .doFinally(signalType -> logResponseStatus(signalType, startTime));
    }

    /**
     * log request and response status
     *
     * @param payload payload
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> metadataPush(Payload payload) {
        long startTime = System.currentTimeMillis();
        return super.metadataPush(payload)
                .doFirst(() -> logRequest(payload))
                .doFinally(signalType -> logResponseStatus(signalType, startTime));
    }

    /**
     * Log request payload.
     *
     * @param payload payload
     * @return RequestLogInfo
     */
    private void logRequest(Payload payload) {
        RequestLogInfo.RequestLogInfoBuilder builder = RequestLogInfo.builder();
        PayloadInfo payloadInfo = payloadExtractFunction.extract(payload, true, true);
        builder.data(payloadInfo.getData())
                .metadata(payloadInfo.getMetadata())
                .build().log(requestPrefix);
    }

    /**
     * Log response payload.
     *
     * @param startTime       startTime
     * @param responsePayload responsePayload
     * @return Mono<Payload>
     */
    private Mono<Payload> logResponse(long startTime, Mono<Payload> responsePayload) {
        return responsePayload.doOnSuccess(response -> {
            ResponseLogInfo.ResponseLogInfoBuilder builder = ResponseLogInfo.builder();
            builder.data(response.getDataUtf8()).spentTime(System.currentTimeMillis() - startTime);
            builder.build().log(responsePrefix);
        }).doFinally(signalType -> logResponseStatus(signalType, startTime));
    }

    /**
     * Log common info for response
     *
     * @param signalType signalType
     * @param startTime  startTime
     */
    private void logResponseStatus(SignalType signalType, long startTime) {
        log.info("[{}], responding status:[{}]; spentTime:[{}]ms", responsePrefix, signalType.toString(), System.currentTimeMillis() - startTime);
    }
}