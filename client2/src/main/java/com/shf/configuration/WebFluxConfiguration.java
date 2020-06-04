package com.shf.configuration;

import com.shf.rsocket.spring.webflux.filter.TraceIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/6/3 23:37
 */
@Configuration
public class WebFluxConfiguration extends WebFluxConfigurationSupport {

    @Bean
    public TraceIdFilter traceIdFilter(){
        return new TraceIdFilter();
    }
}
