package com.shf.rsocket.interceptor.context;

import com.shf.rsocket.interceptor.PayloadExtractFunction;

/**
 * description :
 * As a responder, store the metadata in the context in first.
 *
 * @author songhaifeng
 * @date 2020/5/28 0:23
 */
public class DefaultResponderContextInterceptor extends AbstractRSocketContextInterceptor {

    public DefaultResponderContextInterceptor(PayloadExtractFunction payloadExtractFunction) {
        super(payloadExtractFunction);
    }

    /**
     * Suggest to run first of all.
     *
     * @return order value
     */
    @Override
    public int getOrder() {
        return CONTEXT_RESPONDER_PRECEDENCE;
    }

}
