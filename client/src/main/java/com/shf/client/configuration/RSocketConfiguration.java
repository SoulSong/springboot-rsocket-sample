package com.shf.client.configuration;

import com.shf.client.responder.annotation.RSocketClientResponder2;
import com.shf.client.responder.controller.Requester1ResponderController;
import com.shf.lease.LeaseReceiver;
import com.shf.lease.LeaseSender;
import com.shf.lease.NoopStats;
import com.shf.lease.ServerRoleEnum;
import io.rsocket.core.Resume;
import io.rsocket.lease.Leases;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import reactor.util.annotation.NonNull;
import reactor.util.retry.Retry;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.shf.mimetype.MimeTypes.MAP_MIME_TYPE;
import static com.shf.mimetype.MimeTypes.REFRESH_TOKEN_MIME_TYPE;
import static com.shf.mimetype.MimeTypes.SECURITY_TOKEN_MIME_TYPE;

/**
 * Description:
 * Client side configuration.
 *
 * @author songhaifeng
 * @date 2019/11/18 11:26
 */
@Configuration
@Slf4j
public class RSocketConfiguration {

    /**
     * Collect all metadata extracts which defines a name
     */
    public static final List<MetadataToExtractRef> METADATA_TO_EXTRACT_REF_LIST = Arrays.asList(
            new MetadataToExtractRef(MimeTypeUtils.APPLICATION_JSON, List.class, null, "connect-metadata"),
            new MetadataToExtractRef(SECURITY_TOKEN_MIME_TYPE, String.class, null, "securityToken"),
            new MetadataToExtractRef(REFRESH_TOKEN_MIME_TYPE, String.class, null, "refreshToken"),
            new MetadataToExtractRef(MAP_MIME_TYPE, null, new ParameterizedTypeReference<Map<String, Object>>() {
            }, "properties")
    );

    @Configuration
    static class CommonRequesterConfiguration {

        /**
         * Add resume ability for RSocketRequester. Here we can customize any thing here for our business.
         * The RSocketRequester.Builder instance is a prototype bean, meaning each injection point will provide you with a new instance .
         * This is done on purpose since this builder is stateful and you shouldn’t create requesters with different setups using the same instance.
         * Like requester and requester2 as below.
         * <p>
         * Native API implements as follows：
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
                    .rsocketConnector(rSocketConnector ->
                            rSocketConnector
                                    .resume(new Resume()
                                            .sessionDuration(Duration.ofMinutes(5))
                                            .streamTimeout(Duration.ofSeconds(60))
                                            .retry(
                                                    Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                                                            .doBeforeRetry(s -> log.warn("Server disconnected. Trying to resume connection..."))
                                            )
                                    )
                    );
        }

        /**
         * Customize the rSocket-Strategy for metadata. It can be used for both client-side and server-side.
         *
         * @return RSocketStrategiesCustomizer
         */
        @Bean
        public RSocketStrategiesCustomizer addMetadataExtractMimeTypeCustomizer() {
            return (strategyBuilder) -> {
                strategyBuilder
                        // Already set as the default routeMatcher in {@Code RSocketStrategiesAutoConfiguration#rSocketStrategies}
                        .routeMatcher(new PathPatternRouteMatcher())
                        .metadataExtractorRegistry(register -> {
                            // register all metadata extracts
                            METADATA_TO_EXTRACT_REF_LIST.forEach(metadataToExtractRef -> {
                                if (null != metadataToExtractRef.targetType) {
                                    register.metadataToExtract(metadataToExtractRef.mimeType, metadataToExtractRef.targetType, metadataToExtractRef.name);
                                } else if (null != metadataToExtractRef.parameterizedTypeReference) {
                                    register.metadataToExtract(metadataToExtractRef.mimeType, metadataToExtractRef.parameterizedTypeReference, metadataToExtractRef.name);
                                }
                            });
                        });
            };
        }

    }

    /**
     * Config the first requester. It is a clientResponder, handles requests by {@link Requester1ResponderController}.
     * When the current application is in scenarios with client and server, or contains multiple clients.
     * We need a custom annotation such as {@link com.shf.client.responder.annotation.RSocketClientResponder1} vs
     * the default @Controller to switch to a different strategy for detecting client responders.
     * <p>
     * At the same time, we need to create a new {@link RSocketMessageHandler} for handling the requests.
     * Here via {@link RSocketMessageHandler#clientResponder(RSocketStrategies, Object...)} to create a new instance,
     * set the specific {@link Requester1ResponderController} for handling requests.
     */
    @Configuration
    static class Requester1Configuration {
        /**
         * Create a {@link RSocketRequester} for interacting with the RSocket server.
         * It will return a DefaultRSocketRequesterBuilder object by the method {@code DefaultRSocketRequesterBuilder#doConnect}.
         *
         * @param builder RSocketRequester.Builder
         * @return DefaultRSocketRequester
         */
        @Bean("rSocketRequester1")
        public RSocketRequester rSocketRequester1(RSocketRequester.Builder builder,
                                                  RSocketStrategies rSocketStrategies,
                                                  Requester1ResponderController requester1ResponderController) {
            return builder
                    // requester and responder come in pairs. When any requester needs to responded, it need to config the specific handlers.
                    // Here suggest to create a new {@Code RSocketMessageHandler} instance. The default {@code RSocketMessageHandler} instance used as a server not a responder.
                    .rsocketConnector(rSocketConnector ->
                            rSocketConnector.acceptor(RSocketMessageHandler.responder(rSocketStrategies, requester1ResponderController))
                    )
                    // Link {@Code DefaultRSocketRequesterBuilder#getSetupPayload} and {@Code RSocketFactory.ClientRSocketFactory.StartClient#start}.
                    // Setting payload(@Payload) for @ConnectMapping
                    .setupData("Client-123")
                    // Setting header(metadata) for @ConnectMapping
                    .setupMetadata(Arrays.asList("connect-metadata-values", "connect-metadata-values2"), MimeTypeUtils.APPLICATION_JSON)
                    .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 7000)))
                    .retry(5)
                    .cache()
                    .block();
        }
    }

    /**
     * Config the second requester. Create a new {@link RSocketMessageHandler} instance named handler4Requester2 for the requester to be a clientResponder.
     * Here via setting {@link RSocketMessageHandler#setHandlerPredicate(Predicate)} to specify the handlers which annotated by {@link RSocketClientResponder2}.
     */
    @Configuration
    static class Requester2Configuration {

        @Bean("handler4Requester2")
        public RSocketMessageHandler handler4Requester2(RSocketStrategies strategies) {
            RSocketMessageHandler handler = new RSocketMessageHandler();
            handler.setHandlerPredicate(type -> AnnotatedElementUtils.hasAnnotation(type, RSocketClientResponder2.class));
            handler.setRSocketStrategies(strategies);
            return handler;
        }

        /**
         * Create another {@link RSocketRequester} for testing {@link org.springframework.messaging.rsocket.annotation.ConnectMapping} with route and metadata.
         *
         * @param builder RSocketRequester.Builder
         * @return RSocketRequester
         */
        @Bean("rSocketRequester2")
        public RSocketRequester rSocketRequester2(RSocketRequester.Builder builder, @Qualifier("handler4Requester2") RSocketMessageHandler rSocketMessageHandler) {
            return builder
                    .rsocketConnector(rSocketConnector -> {
                        rSocketConnector.acceptor(rSocketMessageHandler.responder());
                    })
                    .setupData("Client-234")
                    .setupMetadata(Collections.singleton("another-metadata-values"), MimeTypeUtils.APPLICATION_JSON)
                    // Mapping @ConnectMapping's route in server side.
                    // route could be a route template, then expand routeVars into the template.
                    .setupRoute("specific.route.{id}.{id}", "1", "2")
                    .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 7000)))
                    .block();
        }


    }

    @Configuration
    static class Requester3Configuration {
        /**
         * Create another {@link RSocketRequester} for testing reject client which clientId is 'Client999'.
         *
         * @param builder RSocketRequester.Builder
         * @return RSocketRequester
         */
        @Bean("rSocketRequester3")
        public RSocketRequester rSocketRequester3(RSocketRequester.Builder builder) {
            return builder
                    .setupData("Client999")
                    .setupMetadata(Collections.singleton("another-metadata-values"), MimeTypeUtils.APPLICATION_JSON)
                    .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 7000)))
                    .block();
        }
    }


    /**
     * If this client is also act as a server. Need to create a new {@link RSocketMessageHandler} instance
     * for handling RSocket requests with {@link ConnectMapping @ConnectMapping}
     * and {@link MessageMapping @MessageMapping} methods.
     */
    @Configuration
    static class ServerConfiguration {
        /**
         * {@link RSocketMessagingAutoConfiguration}
         *
         * @param strategies strategies
         * @return RSocketMessageHandler
         */
        @Bean
        @Primary
        public RSocketMessageHandler rSocketMessageHandler(RSocketStrategies strategies) {
            RSocketMessageHandler handler = new RSocketMessageHandler();
            handler.setRSocketStrategies(strategies);
            return handler;
        }

        /**
         * A ServerRSocketFactoryCustomizer to add the emission (and retrieval) of leases to (and from) clients.
         * Leases can be used to limit the number of accepted clients on server side. This will keep the server responsive for
         * more, distinct clients, and keeps it from being overwhelmed with requests.
         * <p>
         * since  boot2.2.7 or rSocket1.0.0, ServerRSocketFactoryProcessor is Deprecated.instead to use {@link RSocketServerCustomizer}
         *
         * @return RSocketServerCustomizer
         */
        @Bean
        RSocketServerCustomizer leaseCustomizer() {
            return rSocketServer -> rSocketServer.lease(() ->
                    Leases.<NoopStats>create()
                            // receive the lease from the server side.
                            .receiver(new LeaseReceiver(ServerRoleEnum.SERVER))
                            // issue 5 leases to each client, the timeToLiveMillis is 7s.
                            .sender(new LeaseSender(ServerRoleEnum.SERVER, 70_000, 5))
            );
        }
    }

    /**
     * Define a metadata-extract must have a name.
     */
    @Data
    @AllArgsConstructor
    @Validated
    public static class MetadataToExtractRef {
        @NonNull
        private MimeType mimeType;
        private Class<?> targetType;
        private ParameterizedTypeReference<?> parameterizedTypeReference;
        @NonNull
        private String name;
    }
}
