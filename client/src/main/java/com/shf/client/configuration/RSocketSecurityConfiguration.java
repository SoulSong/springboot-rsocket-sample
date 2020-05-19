package com.shf.client.configuration;

import com.shf.client.server.log.interceptor.DefaultRequestLogPayloadInterceptor;

import org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

/**
 * Description:
 * Auto_configuration forwards to {@link RSocketSecurityAutoConfiguration}.
 *
 * @author songhaifeng
 * @date 2019/12/17 01:12
 */
@Configuration
public class RSocketSecurityConfiguration {
    /**
     * See default configuration in {@code org.springframework.security.config.annotation.rsocket.SecuritySocketAcceptorInterceptorConfiguration}.
     * In production, we need to customize it.
     *
     * @param rSocket {@link RSocketSecurity} is register in {@code org.springframework.security.config.annotation.rsocket.RSocketSecurityConfiguration}.
     *                It is a stateful instance.
     * @return PayloadSocketAcceptorInterceptor
     */
    @Bean
    public PayloadSocketAcceptorInterceptor rSocketInterceptor(RSocketSecurity rSocket, RSocketStrategies rSocketStrategies) {
        rSocket.authorizePayload(authorize -> {
            authorize
                    // must have ROLE_SETUP to make connection
                    .setup().hasRole("SETUP")
                    // must have ROLE_ADMIN for routes starting with "user"
                    .route("user.*").hasRole("ADMIN")
                    // any other request must be authenticated
                    .anyRequest().authenticated()
                    // payloads that have no metadata have no authorization rules.
                    .anyExchange().permitAll();
        }).simpleAuthentication(Customizer.withDefaults())
                // Add customized payload interceptor for logging request
                .addPayloadInterceptor(new DefaultRequestLogPayloadInterceptor(rSocketStrategies));
        return rSocket.build();
    }

    /**
     * Define three users for testing.
     * {@link MapReactiveUserDetailsService} is the default {@link ReactiveUserDetailsService}, it is autowired in {@link UserDetailsRepositoryReactiveAuthenticationManager}.
     * We could implement {@link ReactiveUserDetailsService} to customize another {@link ReactiveUserDetailsService}, such as `JdbcReactiveUserDetailsService`
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

}
