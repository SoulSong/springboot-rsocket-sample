package com.shf.rsocket.log;

import io.rsocket.plugins.LimitRateInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/20 23:28
 */
@Slf4j
public class DefaultResponderLog extends AbstractRSocketLog {
    private String appName;

    public DefaultResponderLog(@NonNull String appName) {
        this.appName = appName;
    }

    @Override
    String getPrefix() {
        return appName + RECEIVE;
    }

    @Override
    String getResponsePrefix() {
        return appName + SEND;
    }
}
