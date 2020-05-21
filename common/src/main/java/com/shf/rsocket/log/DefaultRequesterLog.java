package com.shf.rsocket.log;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/20 23:28
 */
@Slf4j
public class DefaultRequesterLog extends AbstractRSocketLog {

    public DefaultRequesterLog(@NonNull String appName, @NonNull MetadataExtractor metadataExtractor) {
        super(appName, metadataExtractor);
    }

    @Override
    String getPrefix() {
        return getAppName() + SEND;
    }

    @Override
    String getResponsePrefix() {
        return getAppName() + RECEIVE;
    }
}
