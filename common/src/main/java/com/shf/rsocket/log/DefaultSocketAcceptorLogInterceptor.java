package com.shf.rsocket.log;

import com.shf.rsocket.entity.RSocketRole;
import com.shf.rsocket.log.entity.ConnectionSetupLogInfo;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.SocketAcceptor;
import io.rsocket.plugins.InterceptorRegistry;
import io.rsocket.plugins.SocketAcceptorInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.util.Assert;

import java.util.Map;

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
 * must register a {@link DefaultRequesterLogInterceptor} together for logging the request payload.
 *
 * @author songhaifeng
 * @date 2020/5/22 11:33
 */
@Slf4j
public class DefaultSocketAcceptorLogInterceptor implements RSocketAcceptorLogInterceptor {

    private final String appName;
    private final MetadataExtractor metadataExtractor;
    private final RSocketRole rSocketRole;

    public DefaultSocketAcceptorLogInterceptor(@NonNull String appName, @NonNull MetadataExtractor metadataExtractor, @NonNull RSocketRole rSocketRole) {
        Assert.notNull(metadataExtractor, "metadataExtractor must not be null.");
        this.appName = appName;
        this.metadataExtractor = metadataExtractor;
        this.rSocketRole = rSocketRole;
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
            return socketAcceptor.accept(connectionSetupPayload, new PayloadLogRSocket(sendingSocket, metadataExtractor, appName + SEND, appName + RECEIVE))
                    // The `acceptingSocket` is a responder or client-responder, so equals to {@link InterceptorRegistry#forResponder(RSocketInterceptor)}.
                    .map(acceptingSocket -> new PayloadLogRSocket(acceptingSocket, metadataExtractor, appName + RECEIVE, appName + SEND));
        };
    }

    /**
     * Log connection payload.
     *
     * @param setupPayload setupPayload
     */
    private void logConnect(ConnectionSetupPayload setupPayload) {
        ConnectionSetupLogInfo.ConnectionSetupLogInfoBuilder builder = ConnectionSetupLogInfo.builder();
        builder.data(setupPayload.getDataUtf8());

        if (setupPayload.hasMetadata()) {
            Map<String, Object> metadata = metadataExtractor.extract(setupPayload, METADATA_MIME_TYPE);
            builder.metadata(metadata);
        }
        builder.build().log(appName, rSocketRole);
    }
}
