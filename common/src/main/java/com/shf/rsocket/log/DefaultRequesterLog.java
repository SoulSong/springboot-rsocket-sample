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
public class DefaultRequesterLog extends AbstractRSocketLog {

    private String appName;


    public DefaultRequesterLog(@NonNull String appName) {
        this.appName = appName;
    }

    @Override
    String getPrefix() {
        return appName + SEND;
    }

    @Override
    String getResponsePrefix() {
        return appName + RECEIVE;
    }
}
