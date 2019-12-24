package com.shf.client.server.log;

import com.shf.client.server.log.converter.DefaultPayloadExchangeLogInfoConverter;
import com.shf.client.server.log.converter.PayloadExchangeLogInfoConverter;

import org.apache.commons.collections4.MapUtils;
import org.springframework.core.Ordered;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.api.PayloadExchange;
import org.springframework.security.rsocket.api.PayloadInterceptor;
import org.springframework.security.rsocket.api.PayloadInterceptorChain;
import org.springframework.security.rsocket.authentication.AuthenticationPayloadInterceptor;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Description:
 * Refer to {@link AuthenticationPayloadInterceptor}
 *
 * @author songhaifeng
 * @date 2019/12/21 21:38
 */
@Slf4j
public class DefaultRequestLogPayloadInterceptor implements PayloadInterceptor, Ordered {

    private PayloadExchangeLogInfoConverter converter;
    private int order = 1;

    public DefaultRequestLogPayloadInterceptor(RSocketStrategies rSocketStrategies) {
        this.converter = new DefaultPayloadExchangeLogInfoConverter(rSocketStrategies);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> intercept(PayloadExchange exchange, PayloadInterceptorChain chain) {
        return converter.convert(exchange)
                .switchIfEmpty(Mono.just(new RequestLogInfo())).map(requestLogInfo -> {
                    log.info("Current request payload data : {}", requestLogInfo.getData());
                    Map<String, Object> metadata = requestLogInfo.getMetadata();
                    if (MapUtils.isNotEmpty(metadata)) {
                        log.info("Current request payload metadata : {}", metadata.size());
                        metadata.forEach((key, value) -> {
                            log.info("key : {} >>>> value : {}", key, value);
                        });
                    }
                    return Mono.empty();
                }).then(chain.next(exchange));
    }
}
