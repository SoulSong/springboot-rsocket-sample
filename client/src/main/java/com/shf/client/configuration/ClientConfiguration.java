package com.shf.client.configuration;

import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.resume.ClientResume;
import io.rsocket.resume.PeriodicResumeStrategy;
import io.rsocket.resume.ResumeStrategy;
import io.rsocket.transport.netty.client.TcpClientTransport;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Description:
 * Client side configuration.
 *
 * @author: songhaifeng
 * @date: 2019/11/18 11:26
 */
@Configuration
@Slf4j
public class ClientConfiguration {


    /**
     * Add resume ability for RSocketRequester.
     * We can customize any thing here for our business.
     * Origin implement as follows：
     * <pre>{@code
     *      @Bean
     *     public RSocket rSocket() {
     *         return RSocketFactory
     *                 .connect()
     *                 .resume()
     *                 .resumeStrategy(() -> new VerboseResumeStrategy(new PeriodicResumeStrategy(Duration.ofSeconds(5))))
     *                 .resumeStreamTimeout(Duration.ofSeconds(30))
     *                 .mimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString(), MimeTypeUtils.APPLICATION_JSON_VALUE)
     *                 .frameDecoder(PayloadDecoder.ZERO_COPY)
     *                 .transport(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 7000)))
     *                 .start()
     *                 .block();
     *     }
     *
     *     @Bean
     *     public RSocketRequester rSocketRequester(RSocketStrategies strategies) {
     *         return RSocketRequester.wrap(rSocket(),
     *                 MimeTypeUtils.APPLICATION_JSON,
     *                 MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString()),
     *                 strategies);
     *     }
     * }</pre>
     * RSocketRequester.wrap(***) will wrapper the original RSocket as a higher level Object.
     * Here is the DefaultRSocketRequesterBuilder object.
     *
     * @param strategies RSocketStrategies
     * @return DefaultRSocketRequesterBuilder
     */
    @Bean
    @Scope("prototype")
    public RSocketRequester.Builder rSocketRequesterBuilder(RSocketStrategies strategies) {
        return RSocketRequester.builder()
                // default value is also WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA, setting in DefaultRSocketRequesterBuilder
                .metadataMimeType(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString()))
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .rsocketStrategies(strategies)
                .rsocketFactory(rSocketFactory ->
                        // Resumption is designed for loss of connectivity and assumes client and server state is maintained across connectivity loss
                        // So if restart server or client, it will resume failure. In fact, it's not always succeed.
                        rSocketFactory.resume()
                                .resumeStrategy(() -> new VerboseResumeStrategy(new PeriodicResumeStrategy(Duration.ofSeconds(5))))
                                .resumeStreamTimeout(Duration.ofSeconds(30))
                                .frameDecoder(PayloadDecoder.ZERO_COPY));
    }

    /**
     * Create a {@link RSocketRequester} for interacting with the RSocket server.
     * It will return a DefaultRSocketRequesterBuilder object by the method {@code DefaultRSocketRequesterBuilder#doConnect}.
     *
     * @param builder RSocketRequester.Builder
     * @return DefaultRSocketRequester
     */
    @Bean
    @Primary
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder
                // Link {@Code DefaultRSocketRequesterBuilder#getSetupPayload} and {@Code RSocketFactory.ClientRSocketFactory.StartClient#start}.
                // Setting payload(@Payload) for @ConnectMapping
                .setupData("Client123")
                // Setting header(metadata) for @ConnectMapping
                .setupMetadata(Arrays.asList("connect-metadata-values", "connect-metadata-values2"), MimeTypeUtils.APPLICATION_JSON)
                .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 7000)))
                .block();
    }

    /**
     * Create another {@link RSocketRequester} for testing {@link org.springframework.messaging.rsocket.annotation.ConnectMapping} with route and metadata.
     *
     * @param builder RSocketRequester.Builder
     * @return RSocketRequester
     */
    @Bean
    public RSocketRequester rSocketRequester2(RSocketRequester.Builder builder) {
        return builder
                .setupData("Client234")
                .setupMetadata(Collections.singleton("another-metadata-values"), MimeTypeUtils.APPLICATION_JSON)
                // Mapping @ConnectMapping's route in server side.
                // route could be a route template, then expand routeVars into the template.
                .setupRoute("specific.route.{id}.{id}", "1", "2")
                .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 7000)))
                .block();
    }

    /**
     * Enhance the resumeStrategy for logging.
     */
    private static class VerboseResumeStrategy implements ResumeStrategy {
        private final ResumeStrategy resumeStrategy;

        VerboseResumeStrategy(ResumeStrategy resumeStrategy) {
            this.resumeStrategy = resumeStrategy;
        }

        @Override
        public Publisher<?> apply(ClientResume clientResume, Throwable throwable) {
            return Flux.from(resumeStrategy.apply(clientResume, throwable))
                    .doOnNext(v -> log.warn("Disconnected. Trying to resume connection..."));
        }
    }
}
