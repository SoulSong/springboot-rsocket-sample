package com.shf.rsocket.interceptor.log;

import com.shf.rsocket.entity.PayloadInfo;
import com.shf.rsocket.entity.RSocketRole;
import com.shf.rsocket.interceptor.PayloadExtractFunction;
import com.shf.rsocket.interceptor.PayloadUtils;
import com.shf.rsocket.interceptor.log.entity.ConnectionSetupLogInfo;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.SocketAcceptor;
import io.rsocket.plugins.InterceptorRegistry;
import io.rsocket.plugins.SocketAcceptorInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Be used with {@link InterceptorRegistry#forSocketAcceptor(SocketAcceptorInterceptor)},
 * log the payload and metadata for connectionSetUp.
 *
 * @author songhaifeng
 * @date 2020/6/2 1:03
 */
@Slf4j
public class DefaultConnectionSetUpLogInterceptor implements RSocketAcceptorLogInterceptor {

    private final String appName;
    private final RSocketRole rSocketRole;
    private final PayloadExtractFunction payloadExtractFunction;

    public DefaultConnectionSetUpLogInterceptor(@NonNull String appName,
                                                @NonNull RSocketRole rSocketRole,
                                                @NonNull PayloadExtractFunction payloadExtractFunction) {
        this.appName = appName;
        this.rSocketRole = rSocketRole;
        this.payloadExtractFunction = payloadExtractFunction;
    }

    /**
     * Decorate a {@link SocketAcceptor}
     *
     * @param socketAcceptor socketAcceptor
     * @return socketAcceptor
     */
    @Override
    public SocketAcceptor apply(SocketAcceptor socketAcceptor) {
        return (connectionSetupPayload, sendingSocket) -> {
            logConnect(connectionSetupPayload);
            return socketAcceptor.accept(connectionSetupPayload, sendingSocket);
        };
    }

    /**
     * Log connection payload.
     *
     * @param setupPayload setupPayload
     */
    private void logConnect(ConnectionSetupPayload setupPayload) {
        PayloadInfo payloadInfo = PayloadUtils.extractPayload(payloadExtractFunction, setupPayload);
        ConnectionSetupLogInfo.builder().data(payloadInfo.getData())
                .metadata(payloadInfo.getMetadata())
                .build().log(appName, rSocketRole);
    }

}
