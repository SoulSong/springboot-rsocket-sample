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
public class DefaultResponderLog extends AbstractRSocketLog {

    public DefaultResponderLog(@NonNull String appName, @NonNull MetadataExtractor metadataExtractor) {
        super(appName, metadataExtractor);
    }

    @Override
    String getPrefix() {
        return getAppName() + RECEIVE;
    }

    @Override
    String getResponsePrefix() {
        return getAppName() + SEND;
    }
}
