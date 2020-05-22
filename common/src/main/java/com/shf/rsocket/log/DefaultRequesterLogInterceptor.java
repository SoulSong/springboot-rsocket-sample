package com.shf.rsocket.log;

import io.rsocket.plugins.InterceptorRegistry;
import io.rsocket.plugins.RSocketInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;

/**
 * description :
 * Be used with {@link InterceptorRegistry#forRequester(RSocketInterceptor)}, log the payload and metadata for the requester or server-requester.
 *
 * @author songhaifeng
 * @date 2020/5/20 23:28
 */
@Slf4j
public class DefaultRequesterLogInterceptor extends AbstractRSocketLogInterceptor {

    public DefaultRequesterLogInterceptor(@NonNull String appName, @NonNull MetadataExtractor metadataExtractor) {
        super(appName, metadataExtractor);
    }

    /**
     * As a requester, `send` request.
     *
     * @return String
     */
    @Override
    String getRequestPrefix() {
        return getAppName() + SEND;
    }

    /**
     * As a requester, `receive` response.
     *
     * @return String
     */
    @Override
    String getResponsePrefix() {
        return getAppName() + RECEIVE;
    }
}
