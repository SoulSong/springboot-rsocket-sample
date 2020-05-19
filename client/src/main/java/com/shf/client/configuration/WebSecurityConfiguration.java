package com.shf.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Description:
 * Disable http basic-authentication.
 *
 * @author songhaifeng
 * @date 2019/12/21 20:30
 */
@Configuration
public class WebSecurityConfiguration {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.httpBasic().disable()
                .authorizeExchange(exchange ->
                        exchange
                                .anyExchange().permitAll()
                ).csrf().disable();
        return http.build();
    }

}
