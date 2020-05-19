package com.shf.configuration;

import com.shf.lease.LeaseReceiver;
import com.shf.lease.LeaseSender;
import com.shf.lease.NoopStats;
import com.shf.lease.ServerRoleEnum;
import io.rsocket.lease.Leases;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
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
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        // Test `setup().hasRole("SETUP")` which is configured on the server side.
        final UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "654321");
        return builder
                .rsocketConnector(rSocketConnector ->
                        rSocketConnector.lease(() ->
                                Leases.<NoopStats>create()
                                        .receiver(new LeaseReceiver(ServerRoleEnum.CLIENT))
                                        .sender(new LeaseSender(ServerRoleEnum.CLIENT, 3_000, 5))
                        )
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
     * Add basic authentication decoder on the client side.
     *
     * @return RSocketStrategiesCustomizer
     */
    @Bean
    public RSocketStrategiesCustomizer authenticationStrategyCustomizer() {
        return (strategyBuilder) -> strategyBuilder.encoder(new SimpleAuthenticationEncoder());
    }
}
