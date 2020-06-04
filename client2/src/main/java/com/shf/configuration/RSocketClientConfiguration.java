package com.shf.configuration;

import com.shf.rsocket.entity.RSocketRole;
import com.shf.rsocket.interceptor.PayloadExtractFunction;
import com.shf.rsocket.interceptor.log.DefaultConnectionSetUpLogInterceptor;
import com.shf.rsocket.interceptor.log.DefaultRequesterLogInterceptor;
import com.shf.rsocket.lease.LeaseReceiver;
import com.shf.rsocket.lease.LeaseSender;
import com.shf.rsocket.lease.NoopStats;
import com.shf.rsocket.lease.ServerRoleEnum;
import com.shf.rsocket.spring.PayloadHandler;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.lease.Leases;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeTypeUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Description:
 * Client side configuration.
 * - Integrated with spring-security for authentication and authorization.
 * - Enable lease： Client side check itself whether has valid leases.
 * If has no, it will invoke exception inside and never send the request to the server side.
 *
 * @author songhaifeng
 * @date 2019/11/18 11:26
 */
@Configuration
@Slf4j
public class RSocketClientConfiguration {

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder, RSocketStrategies rSocketStrategies, @Value("${spring.application.name}") String appName) {
        // Test `setup().hasRole("SETUP")` which is configured on the server side.
        final UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "654321");
        final PayloadExtractFunction payloadExtractFunction = PayloadHandler.payloadExtractFunction(rSocketStrategies.metadataExtractor());
        return builder
                .rsocketConnector(rSocketConnector ->
                        rSocketConnector.lease(() ->
                                Leases.<NoopStats>create()
                                        .receiver(new LeaseReceiver(ServerRoleEnum.CLIENT))
                                        .sender(new LeaseSender(ServerRoleEnum.CLIENT, 3_000, 5)))
                                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                                .interceptors(interceptorRegistry ->
                                        interceptorRegistry.forRequester(new DefaultRequesterLogInterceptor(appName, payloadExtractFunction)))
                                .interceptors(interceptorRegistry ->
                                        interceptorRegistry.forSocketAcceptor(new DefaultConnectionSetUpLogInterceptor(appName, RSocketRole.RSOCKET_CONNECTOR, payloadExtractFunction)))
                )
                .setupData("Client2-abc")
                // could send multiple metadata in a setup frame.
                .setupMetadata(Arrays.asList("connect-metadata-value", "connect-metadata-value2"), MimeTypeUtils.APPLICATION_JSON)
                // Authentication metadata
                .setupMetadata(credentials, MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
                .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 8081)))
                .block();
    }

    /**
     * Add simple authentication decoder on the client side.
     *
     * @return RSocketStrategiesCustomizer
     */
    @Bean
    public RSocketStrategiesCustomizer authenticationStrategyCustomizer() {
        return (strategyBuilder) -> strategyBuilder.encoder(new SimpleAuthenticationEncoder());
    }

}
