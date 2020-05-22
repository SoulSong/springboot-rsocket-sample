package com.shf.rsocket.log;

import io.rsocket.RSocket;
import io.rsocket.plugins.LimitRateInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.util.Assert;

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
    private final MetadataExtractor metadataExtractor;

    public AbstractRSocketLogInterceptor(String appName, MetadataExtractor metadataExtractor) {
        Assert.notNull(metadataExtractor, "metadataExtractor must not be null.");
        this.appName = appName;
        this.metadataExtractor = metadataExtractor;
    }

    public String getAppName() {
        return appName;
    }

    abstract String getRequestPrefix();

    abstract String getResponsePrefix();

    @Override
    public RSocket apply(RSocket rSocket) {
        return new PayloadLogRSocket(rSocket, metadataExtractor, getRequestPrefix(), getResponsePrefix());
    }
}
