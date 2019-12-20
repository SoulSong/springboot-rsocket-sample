package com.shf.client.configuration;

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.BasicAuthenticationDecoder;

/**
 * Description:
 *
 * @author songhaifeng
 * @date 2019/12/17 01:12
 */
@Configuration
@EnableRSocketSecurity
public class RSocketSecurityConfiguration {
    /**
     * See default configuration in {@code SecuritySocketAcceptorInterceptorConfiguration}
     *
     * @param rSocket rSocket
     * @return PayloadSocketAcceptorInterceptor
     */
    @Bean
    public PayloadSocketAcceptorInterceptor rSocketInterceptor(RSocketSecurity rSocket) {
        rSocket.authorizePayload(authorize -> {
            authorize
                    // must have ROLE_SETUP to make connection
                    .setup().hasRole("SETUP")
                    // must have ROLE_ADMIN for routes starting with "user"
                    .route("user.*").hasRole("ADMIN")
                    // any other request must be authenticated
                    .anyRequest().authenticated()
                    .anyExchange().permitAll();
        }).basicAuthentication(Customizer.withDefaults());
        return rSocket.build();
    }

    /**
     * Add basic authentication decoder on the server side
     *
     * @return RSocketStrategiesCustomizer
     */
    @Bean
    public RSocketStrategiesCustomizer authenticationStrategyCustomizer() {
        return (strategyBuilder) -> {
            strategyBuilder.decoder(new BasicAuthenticationDecoder());
        };
    }

    /**
     * Define 3 users for testing.
     *
     * @return MapReactiveUserDetailsService
     */
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder().username("shf").password("123456").roles("ADMIN").build();
        UserDetails user = User.withDefaultPasswordEncoder().username("shf_2").password("123456").roles("USER").build();
        UserDetails setupUser = User.withDefaultPasswordEncoder().username("setup").password("654321").roles("SETUP").build();

        return new MapReactiveUserDetailsService(admin, user, setupUser);
    }

    /**
     * Add the {@link PayloadSocketAcceptorInterceptor} instance into the SocketAcceptorPlugin.
     *
     * @param rSocketAcceptorInterceptor rSocketAcceptorInterceptor
     * @return ServerRSocketFactoryProcessor
     */
    @Bean
    ServerRSocketFactoryProcessor payloadSocketAcceptorFactoryCustomizer(PayloadSocketAcceptorInterceptor rSocketAcceptorInterceptor) {
        return (factory) -> factory.addSocketAcceptorPlugin(rSocketAcceptorInterceptor);
    }
}
