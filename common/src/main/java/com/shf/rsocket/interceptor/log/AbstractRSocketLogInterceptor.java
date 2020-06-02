package com.shf.rsocket.interceptor.log;

import com.shf.rsocket.interceptor.PayloadExtractFunction;
import io.rsocket.RSocket;
import io.rsocket.plugins.LimitRateInterceptor;
import lombok.extern.slf4j.Slf4j;
import reactor.util.annotation.NonNull;

/**
 * description :
 * Log interceptor, refer to {@link LimitRateInterceptor}
 *
 * @author songhaifeng
 * @date 2020/5/20 23:57
 */
@Slf4j
public abstract class AbstractRSocketLogInterceptor implements RSocketLogInterceptor {
    private final String appName;
    private final PayloadExtractFunction payloadExtractFunction;

    public AbstractRSocketLogInterceptor(String appName, @NonNull PayloadExtractFunction payloadExtractFunction) {
        this.appName = appName;
        this.payloadExtractFunction = payloadExtractFunction;
    }

    public String getAppName() {
        return appName;
    }

    abstract String getRequestPrefix();

    abstract String getResponsePrefix();

    @Override
    public RSocket apply(RSocket rSocket) {
        return new PayloadLogRSocket(rSocket, payloadExtractFunction, getRequestPrefix(), getResponsePrefix());
    }
}
