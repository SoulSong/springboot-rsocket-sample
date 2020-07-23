package com.shf.configuration;

import com.shf.rsocket.spring.webflux.filter.TraceIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/6/3 23:37
 */
@Configuration
public class WebFluxConfiguration implements WebFluxConfigurer {

    @Bean
    public TraceIdFilter traceIdFilter(){
        return new TraceIdFilter();
    }
}
