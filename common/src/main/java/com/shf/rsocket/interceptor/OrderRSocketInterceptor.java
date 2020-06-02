package com.shf.rsocket.interceptor;

import io.rsocket.plugins.RSocketInterceptor;

import java.util.Comparator;

/**
 * description :
 * Run interceptors from min to max.
 *
 * @author songhaifeng
 * @date 2020/5/27 23:58
 */
public interface OrderRSocketInterceptor extends RSocketInterceptor {

    /**
     * Useful constant for the highest precedence value.
     *
     * @see java.lang.Integer#MIN_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * Useful constant for the lowest precedence value.
     *
     * @see java.lang.Integer#MAX_VALUE
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int CONTEXT_REQUEST_PRECEDENCE = -500;
    int CONTEXT_RESPONDER_PRECEDENCE = 500;

    int LOG_REQUEST_PRECEDENCE = -1000;
    int LOG_RESPONDER_PRECEDENCE = 1000;

    /**
     * sort from max to min, then run from min to max
     * eg: {@link com.shf.rsocket.interceptor.context.DefaultResponderContextInterceptor} run before {@link com.shf.rsocket.interceptor.log.DefaultResponderLogInterceptor}
     */
    Comparator<RSocketInterceptor> DEFAULT_INTERCEPTOR_SORT = (o1, o2) -> ((OrderRSocketInterceptor) o2).getOrder() - ((OrderRSocketInterceptor) o1).getOrder();

    /**
     * Get all interceptors for sorting.
     *
     * @return order value
     */
    int getOrder();


}
