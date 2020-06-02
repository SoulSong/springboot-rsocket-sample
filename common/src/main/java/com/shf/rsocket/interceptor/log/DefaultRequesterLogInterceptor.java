package com.shf.rsocket.interceptor.log;

import com.shf.rsocket.interceptor.PayloadExtractFunction;
import io.rsocket.plugins.InterceptorRegistry;
import io.rsocket.plugins.RSocketInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;
import reactor.util.annotation.NonNull;

/**
 * description :
 * Be used with {@link InterceptorRegistry#forRequester(RSocketInterceptor)}, log the payload and metadata for the requester or server-requester.
 *
 * @author songhaifeng
 * @date 2020/5/20 23:28
 */
@Slf4j
public class DefaultRequesterLogInterceptor extends AbstractRSocketLogInterceptor {

    public DefaultRequesterLogInterceptor(@NonNull String appName, @NonNull PayloadExtractFunction payloadExtractFunction) {
        super(appName, payloadExtractFunction);
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

    @Override
    public int getOrder() {
        return LOG_REQUEST_PRECEDENCE;
    }
}
