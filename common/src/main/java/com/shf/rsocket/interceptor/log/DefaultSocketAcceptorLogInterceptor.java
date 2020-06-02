package com.shf.rsocket.interceptor.log;

import com.shf.rsocket.entity.PayloadInfo;
import com.shf.rsocket.entity.RSocketRole;
import com.shf.rsocket.interceptor.OrderRSocketInterceptor;
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
 * description :
 * Be used with {@link InterceptorRegistry#forSocketAcceptor(SocketAcceptorInterceptor)}, log the payload and metadata for
 * - connection
 * - responder
 * - server-requester
 * - client-responder
 * <p>
 * It is the same as {@link DefaultRequesterLogInterceptor} and {@link DefaultResponderLogInterceptor} mostly.
 * 1、Compared to {@link DefaultRequesterLogInterceptor} and {@link DefaultResponderLogInterceptor}, {@link DefaultSocketAcceptorLogInterceptor} has an extra connectionPayload.
 * 2、If use the {@link DefaultSocketAcceptorLogInterceptor} for the {@link io.rsocket.core.RSocketConnector},
 * must register a {@link DefaultRequesterLogInterceptor} together for logging the request payload(as a requester role).
 * <p>
 * Deprecated, use {@link DefaultConnectionSetUpLogInterceptor} instead of this for logging connectionSetup.
 * And use {@link OrderRSocketInterceptor} instead of this for the requester and responder interceptors.
 *
 * @author songhaifeng
 * @date 2020/5/22 11:33
 */
@Slf4j
@Deprecated
public class DefaultSocketAcceptorLogInterceptor implements RSocketAcceptorLogInterceptor {

    private final String appName;
    private final RSocketRole rSocketRole;
    private final PayloadExtractFunction payloadExtractFunction;

    public DefaultSocketAcceptorLogInterceptor(@NonNull String appName,
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
            // The `sendingSocket` is a server-requester(can not be a requester), so equals to {@link InterceptorRegistry#forRequester(RSocketInterceptor)}.
            return socketAcceptor.accept(connectionSetupPayload, new PayloadLogRSocket(sendingSocket, payloadExtractFunction, appName + SEND, appName + RECEIVE))
                    // The `acceptingSocket` is a responder or client-responder, so equals to {@link InterceptorRegistry#forResponder(RSocketInterceptor)}.
                    .map(acceptingSocket -> new PayloadLogRSocket(acceptingSocket, payloadExtractFunction, appName + RECEIVE, appName + SEND));
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
