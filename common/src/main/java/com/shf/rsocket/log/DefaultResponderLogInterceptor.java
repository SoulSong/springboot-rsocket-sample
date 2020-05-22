package com.shf.rsocket.log;

import io.rsocket.plugins.InterceptorRegistry;
import io.rsocket.plugins.RSocketInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;

/**
 * description :
 * Be used with {@link InterceptorRegistry#forResponder(RSocketInterceptor)}, log the payload and metadata for the responder or client-responder.
 *
 * @author songhaifeng
 * @date 2020/5/20 23:28
 */
@Slf4j
public class DefaultResponderLogInterceptor extends AbstractRSocketLogInterceptor {

    public DefaultResponderLogInterceptor(@NonNull String appName, @NonNull MetadataExtractor metadataExtractor) {
        super(appName, metadataExtractor);
    }

    /**
     * As a responder, `receive` request.
     *
     * @return String
     */
    @Override
    String getRequestPrefix() {
        return getAppName() + RECEIVE;
    }

    /**
     * As a responder, `send` response.
     *
     * @return String
     */
    @Override
    String getResponsePrefix() {
        return getAppName() + SEND;
    }
}
