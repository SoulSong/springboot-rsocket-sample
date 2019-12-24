package com.shf.configuration;

import io.rsocket.transport.netty.client.TcpClientTransport;

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeTypeUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 * Client side configuration.
 * - Integrated with spring-security for authentication and authorization.
 *
 * @author songhaifeng
 * @date 2019/11/18 11:26
 */
@Configuration
@Slf4j
public class RSocketClientConfiguration {

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        // Test `setup().hasRole("SETUP")` which is configured on the server side.
        final UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "654321");
        return builder
                .setupData("Client2-abc")
                // could send multiple metadata in a setup frame.
                .setupMetadata(Arrays.asList("connect-metadata-value", "connect-metadata-value2"), MimeTypeUtils.APPLICATION_JSON)
                // Authentication metadata
                .setupMetadata(credentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 8081)))
                .block();
    }

    /**
     * Add basic authentication decoder on the client side.
     *
     * @return RSocketStrategiesCustomizer
     */
    @Bean
    public RSocketStrategiesCustomizer authenticationStrategyCustomizer() {
        return (strategyBuilder) -> {
            strategyBuilder.encoder(new BasicAuthenticationEncoder());
        };
    }
}
