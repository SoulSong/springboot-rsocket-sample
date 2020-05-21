package com.shf.rsocket.log;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.plugins.LimitRateInterceptor;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * description :
 * Refer to {@link LimitRateInterceptor}
 *
 * @author songhaifeng
 * @date 2020/5/20 23:57
 */
@Slf4j
public abstract class AbstractRSocketLog implements RSocketLogInterceptor {
    private final MimeType metadataMimetype = MimeTypeUtils.parseMimeType(
            WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());

    private final String appName;
    private final MetadataExtractor metadataExtractor;

    static final String SEND = " send";
    static final String RECEIVE = " receive";

    public AbstractRSocketLog(String appName, MetadataExtractor metadataExtractor) {
        Assert.notNull(metadataExtractor, "metadataExtractor must not be null.");
        this.appName = appName;
        this.metadataExtractor = metadataExtractor;
    }

    public String getAppName() {
        return appName;
    }

    abstract String getPrefix();

    abstract String getResponsePrefix();

    @Override
    public void log(Payload payload) {
        RequestLogInfo.RequestLogInfoBuilder builder = RequestLogInfo.builder();

        builder.data(payload.getDataUtf8());
        if (payload.hasMetadata()) {
            Map<String, Object> metadata = metadataExtractor.extract(payload, metadataMimetype);
            builder.metadata(metadata);
        }
        builder.build().log(getPrefix());
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
                    log.info(">>>>>>>>>>>>>>>>Log Response>>>>>>>>>>>>>>>>>>>");
                    log.info("[{}-Response payload] {}, spent:{}ms", getResponsePrefix(), response.getDataUtf8(), stopWatch.getTotalTimeMillis());
                    log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
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
