package com.shf.configuration;

import io.rsocket.transport.netty.client.TcpClientTransport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

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

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder
                .setupData("Client2-abc")
                .setupMetadata(Arrays.asList("connect-metadata-value", "connect-metadata-value2"), MimeTypeUtils.APPLICATION_JSON)
                .connect(TcpClientTransport.create(new InetSocketAddress("127.0.0.1", 8081)))
                .block();
    }

}
