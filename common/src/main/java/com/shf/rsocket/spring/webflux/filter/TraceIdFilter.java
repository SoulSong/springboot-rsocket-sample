package com.shf.rsocket.spring.webflux.filter;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static com.shf.rsocket.interceptor.trace.TraceConstant.TRACE_ID;
import static com.shf.rsocket.interceptor.trace.TraceConstant.UNKNOWN;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/24 16:10
 */
public class TraceIdFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        return webFilterChain.filter(serverWebExchange)
                .subscriberContext(ctx ->
                        ctx.put(TRACE_ID, serverWebExchange
                                .getRequest()
                                .getHeaders()
                                .getOrEmpty(TRACE_ID)
                                .stream().findFirst().orElse(UNKNOWN)));
    }
}
