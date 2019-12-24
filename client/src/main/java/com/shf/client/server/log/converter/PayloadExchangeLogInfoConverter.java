package com.shf.client.server.log.converter;

import com.shf.client.server.log.RequestLogInfo;

import org.springframework.security.rsocket.api.PayloadExchange;

import reactor.core.publisher.Mono;

/**
 * Description:
 *
 * @author songhaifeng
 * @date 2019/12/23 12:44
 */
public interface PayloadExchangeLogInfoConverter {

    /**
     * Converts from a {@link PayloadExchange} to an {@link RequestLogInfo}
     *
     * @param exchange PayloadExchange
     * @return RequestLogInfo
     */
    Mono<RequestLogInfo> convert(PayloadExchange exchange);
}
