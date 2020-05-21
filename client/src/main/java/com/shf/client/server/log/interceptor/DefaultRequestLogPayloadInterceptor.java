package com.shf.client.server.log.interceptor;

import com.shf.client.server.log.converter.DefaultPayloadExchangeLogInfoConverter;
import com.shf.client.server.log.converter.PayloadExchangeLogInfoConverter;
import com.shf.rsocket.log.AbstractRSocketLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.api.PayloadExchange;
import org.springframework.security.rsocket.api.PayloadInterceptor;
import org.springframework.security.rsocket.api.PayloadInterceptorChain;
import org.springframework.security.rsocket.authentication.AuthenticationPayloadInterceptor;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/**
 * Description:
 * Customized a {@link PayloadInterceptor} for logging request.
 * It refers to {@link AuthenticationPayloadInterceptor}.
 * Finally it will be wried into {@link PayloadSocketAcceptorInterceptor}
 * <p>
 * Deprecated, use {@link AbstractRSocketLog} instead.
 *
 * @author songhaifeng
 * @date 2019/12/21 21:38
 */
@Slf4j
@Deprecated
public class DefaultRequestLogPayloadInterceptor implements PayloadInterceptor, Ordered {

    private PayloadExchangeLogInfoConverter converter;
    private int order = Integer.MIN_VALUE;

    public DefaultRequestLogPayloadInterceptor(RSocketStrategies rSocketStrategies) {
        Assert.notNull(rSocketStrategies, "RSocketStrategies must not be mull.");
        this.converter = new DefaultPayloadExchangeLogInfoConverter(rSocketStrategies);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * log payload
     */
    @Override
    public Mono<Void> intercept(PayloadExchange exchange, PayloadInterceptorChain chain) {
        return Mono.fromCallable(() -> converter.convert(exchange))
                .switchIfEmpty(chain.next(exchange).then(Mono.empty()))
                .map(requestLogInfo -> {
                    requestLogInfo.log(null);
                    return Mono.empty();
                }).then(chain.next(exchange));
    }
}
