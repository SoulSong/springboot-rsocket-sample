package com.shf.server.configuration;

import com.shf.entity.Foo;
import com.shf.rsocket.entity.RSocketRole;
import com.shf.rsocket.interceptor.PayloadExtractFunction;
import com.shf.rsocket.interceptor.context.DefaultResponderContextInterceptor;
import com.shf.rsocket.interceptor.log.DefaultConnectionSetUpLogInterceptor;
import com.shf.rsocket.interceptor.log.DefaultRequesterLogInterceptor;
import com.shf.rsocket.interceptor.log.DefaultResponderLogInterceptor;
import com.shf.rsocket.interceptor.trace.TraceConstant;
import com.shf.rsocket.spring.PayloadHandler;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.shf.rsocket.interceptor.OrderRSocketInterceptor.DEFAULT_INTERCEPTOR_SORT;
import static com.shf.rsocket.mimetype.MimeTypes.FOO_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.MAP_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.PARAMETERIZED_TYPE_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.REFRESH_TOKEN_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.SECURITY_TOKEN_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.TRACE_ID_MIME_TYPE;

/**
 * Description:
 * Server side configuration.
 *
 * @author songhaifeng
 * @date 2019/11/20 18:06
 */
@Configuration
@Slf4j
public class RSocketServerConfiguration {
    @Value("${spring.application.name}")
    private String appName;

    /**
     * Add a {@link RSocketStrategiesCustomizer} to registry metadataExtractMimeType.
     * See more in {@link RSocketStrategiesAutoConfiguration}
     *
     * @return RSocketStrategiesCustomizer
     */
    @Bean
    public RSocketStrategiesCustomizer addMetadataExtractMimeTypeCustomizer() {
        return (strategyBuilder) -> {
            strategyBuilder.metadataExtractorRegistry(register -> {
                register.metadataToExtract(MimeTypeUtils.APPLICATION_JSON, List.class, "connect-metadata");
                // the name is used with @header, each mimeType can be mapped a single name.
                register.metadataToExtract(SECURITY_TOKEN_MIME_TYPE, String.class, "securityToken");
                register.metadataToExtract(REFRESH_TOKEN_MIME_TYPE, String.class, "refreshToken");
                register.metadataToExtract(TRACE_ID_MIME_TYPE, String.class, TraceConstant.TRACE_ID);
                register.metadataToExtract(FOO_MIME_TYPE, Foo.class, "foo");
                // registry with ParameterizedTypeReference for the special type.
                register.metadataToExtract(MAP_MIME_TYPE, new ParameterizedTypeReference<Map<String, Object>>() {
                }, "properties");

                // Only can be used with @Headers, there is no name definition.
                // Add the custom logic to add the decoded value(foo) to the output map
                register.metadataToExtract(PARAMETERIZED_TYPE_MIME_TYPE,
                        new ParameterizedTypeReference<Foo>() {
                        },
                        (foo, outputMap) -> {
                            outputMap.put("name", foo.getName());
                        });

                // The same MimeType can register more than one ParameterizedTypeReference.
                // Add a new attribute for map metadata
                register.metadataToExtract(PARAMETERIZED_TYPE_MIME_TYPE,
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        },
                        (inputMap, outputMap) -> {
                            outputMap.putAll(inputMap);
                            outputMap.put("additional_map_attr", "other_attr");
                        });
            });
        };
    }

    /**
     * Add resume ability for RSocketServer
     *
     * @return RSocketServerCustomizer
     */
    @Bean
    RSocketServerCustomizer resumeServerCustomizer() {
        return (rSocketServer) ->
                rSocketServer.resume(new Resume()
                        .streamTimeout(Duration.ofSeconds(60))
                        .sessionDuration(Duration.ofMinutes(5))
                        .retry(
                                Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                                        .doBeforeRetry(s -> log.warn("Client disconnected. Trying to resume connection..."))
                        )
                );
    }

    /**
     * customize RSocketServer
     *
     * @return RSocketServerCustomizer
     */
    @Bean
    RSocketServerCustomizer rSocketServerCustomizer(RSocketStrategies rSocketStrategies) {
        PayloadExtractFunction payloadExtractFunction = PayloadHandler.payloadExtractFunction(rSocketStrategies.metadataExtractor());
        return (rSocketServer) ->
                rSocketServer.payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .interceptors(interceptorRegistry ->
                                interceptorRegistry.forResponder(new DefaultResponderLogInterceptor(appName, payloadExtractFunction)))
                        .interceptors(interceptorRegistry ->
                                interceptorRegistry.forResponder(new DefaultResponderContextInterceptor(payloadExtractFunction)))
                        // sort must be set after the register operation.
                        .interceptors(interceptorRegistry -> interceptorRegistry.forResponder(list -> {
                            list.sort(DEFAULT_INTERCEPTOR_SORT);
                        }))
                        .interceptors(interceptorRegistry ->
                                interceptorRegistry.forRequester(new DefaultRequesterLogInterceptor(appName, payloadExtractFunction)))
                        .interceptors(interceptorRegistry ->
                                interceptorRegistry.forSocketAcceptor(new DefaultConnectionSetUpLogInterceptor(appName, RSocketRole.RSOCKET_SERVER,
                                        payloadExtractFunction)));
    }


}
