package com.shf.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shf.entity.Foo;
import com.shf.entity.User;
import com.shf.entity.UserRequest;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.rsocket.util.ByteBufPayload;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.shf.rsocket.mimetype.MimeTypes.FOO_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.MAP_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.PARAMETERIZED_TYPE_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.REFRESH_TOKEN_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.SECURITY_TOKEN_MIME_TYPE;
import static com.shf.rsocket.mimetype.MimeTypes.TRACE_ID_MIME_TYPE;

/**
 * Description:
 * WebFlux server.
 *
 * @author: songhaifeng
 * @date: 2019/11/18 15:13
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserRestController {

    private final RSocketRequester rSocketRequester1;

    private final RSocketRequester rSocketRequester2;

    private final ObjectMapper objectMapper;

    @Autowired
    public UserRestController(@Qualifier("rSocketRequester1") RSocketRequester rSocketRequester1,
                              @Qualifier("rSocketRequester2") RSocketRequester rSocketRequester2,
                              ObjectMapper objectMapper) {
        this.rSocketRequester1 = rSocketRequester1;
        this.rSocketRequester2 = rSocketRequester2;
        this.objectMapper = objectMapper;
    }

    /***********************************request/response ******************************/
    /**
     * call the retrieveMono() method, Spring Boot initiates a request/response interaction.
     *
     * @param id id
     * @return user
     */
    @GetMapping(value = "{id}")
    public Publisher<User> user(@PathVariable("id") int id) {
        if (id == 1) {
            return rSocketRequester1
                    .route("user")
                    .metadata("TRACE_ID_" + id, TRACE_ID_MIME_TYPE)
                    .data(new UserRequest(id))
                    .retrieveMono(User.class);
        } else {
            return rSocketRequester1
                    .route("user")
                    .data(new UserRequest(id))
                    .retrieveMono(User.class);
        }
    }

    /***********************************Fire And Forget******************************/

    /**
     * using the send() method to initiate the request instead of retrieveMono(),
     * the interaction model becomes fire-and-forget.
     *
     * @return void
     */
    @GetMapping(value = "add")
    public Publisher<Void> add() {
        return rSocketRequester1
                .route("add.user")
                .metadata("TRACE_ID_SAMPLE", TRACE_ID_MIME_TYPE)
                .data(User.builder().id(4).age(12).name("ball").build())
                .send();
    }

    /***********************************Request Stream******************************/
    /**
     * Defining our response expectation with the retrieveFlux() method call.
     * This is the part which determines the interaction model.
     * <p>
     * Also note that, since our client is also a REST server,
     * it defines response media type as MediaType.TEXT_EVENT_STREAM_VALUE.
     *
     * @return
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Publisher<User> list() {
        return rSocketRequester1
                .route("list")
                .metadata("TRACE_ID_SAMPLE", TRACE_ID_MIME_TYPE)
                .retrieveFlux(User.class);
    }

    /***********************************Request Channel******************************/

    /**
     * If the data(payload) is a stream, then retrieveFlux() method call will be Request-Channel interaction model.
     *
     * @return User
     */
    @GetMapping(value = "request/channel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Publisher<User> requestChannel() {
        return rSocketRequester1
                .route("request.channel")
                .metadata("TRACE_ID_SAMPLE", TRACE_ID_MIME_TYPE)
                .data(Flux.interval(Duration.ofSeconds(1))
                        .map(i -> User.builder().id(i.intValue() + 5).name("bob").age(11).build())
                        .take(10))
                .retrieveFlux(User.class);
    }

    /***********************************Metadata Push******************************/

    /**
     * In production, we could send a json as metadata.
     * We can encode string to {@link io.netty.buffer.ByteBuf} or {@link org.springframework.core.io.buffer.DataBuffer} or {@link java.nio.ByteBuffer} or {@link io.rsocket.Payload} by
     * {@link ByteBufUtil} and {@link org.springframework.messaging.rsocket.PayloadUtils} and {@link NettyDataBufferFactory}.
     *
     * @return User
     * @see org.springframework.messaging.rsocket.PayloadUtils
     * @see org.springframework.core.io.buffer.NettyDataBufferFactory
     * @see ByteBufUtil
     * @see Unpooled
     */
    @GetMapping(value = "metadata/push", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Void> metadataPush() {
        return rSocketRequester1.rsocket()
                .metadataPush(ByteBufPayload.create(Unpooled.EMPTY_BUFFER, Unpooled.wrappedBuffer("metadata update".getBytes())));
    }

    /***********************************Invoke Error******************************/
    @GetMapping("error")
    public Publisher<User> error() {
        return rSocketRequester1
                .route("user.error")
                .metadata("TRACE_ID_SAMPLE", TRACE_ID_MIME_TYPE)
                .retrieveMono(User.class);
    }


    /***********************************Send metadata(header)******************************/
    /**
     * Send a String type of request header(metadata).
     *
     * @return String
     */
    @GetMapping(value = "send/string/header")
    public Publisher<String> sendStringHeader() {
        final String securityToken = "bearer token_001";
        final String refreshToken = "refresh_token_001";
        return rSocketRequester1
                .route("send.string.header")
                .metadata(securityToken, SECURITY_TOKEN_MIME_TYPE)
                .metadata(refreshToken, REFRESH_TOKEN_MIME_TYPE)
                .data(new UserRequest(1))
                .retrieveMono(String.class);
    }

    /**
     * Send three types of request headers(metadata), as String ,custom entity, Map
     *
     * @return void
     */
    @GetMapping(value = "send/headers")
    public Mono<Void> sendHeaders() {
        final String securityToken = "bearer token_001";
        return rSocketRequester1
                .route("send.headers")
                .metadata(securityToken, SECURITY_TOKEN_MIME_TYPE)
                .metadata(Foo.builder().name("foo001").build(), PARAMETERIZED_TYPE_MIME_TYPE)
                .metadata(buildMapHeader(), PARAMETERIZED_TYPE_MIME_TYPE)
                .send();
    }

    /**
     * Send custom entity for request header(metadata)
     *
     * @return String
     */
    @GetMapping(value = "send/entity/header")
    public Publisher<String> sendEntityHeader() {
        return rSocketRequester1
                .route("send.entity.header")
                .metadata(Foo.builder().name("foo001").build(), FOO_MIME_TYPE)
                .retrieveMono(String.class);
    }

    /**
     * Send a Map type of request header(metadata).
     *
     * @return String
     */
    @GetMapping(value = "send/map/header")
    public Publisher<String> sendMapHeader() {
        return rSocketRequester1
                .route("send.map.header")
                .metadata(buildMapHeader(), MAP_MIME_TYPE)
                .retrieveMono(String.class);
    }

    /**
     * Send a json-string of request header(metadata).
     *
     * @return String
     */
    @GetMapping(value = "send/json/header")
    public Publisher<String> sendJsonHeader() throws JsonProcessingException {
        return rSocketRequester1
                .route("send.entity.header")
                .metadata(objectMapper.writeValueAsString(Foo.builder().name("foo001").build()), FOO_MIME_TYPE)
                .retrieveMono(String.class);
    }

    /***********************************DestinationVariable******************************/
    /**
     * Test destination variable in route.
     *
     * @return User
     */
    @GetMapping(value = "another/{id}")
    public Publisher<User> destinationVariable(@PathVariable int id) {
        return rSocketRequester1
                .route("user." + id)
                .retrieveMono(User.class);
    }

    /***********************************Test Responder******************************/
    @GetMapping(value = "requester1/responder")
    public Publisher<String> replayRequester1() {
        return rSocketRequester1
                .route("requester.responder")
                .metadata("TRACE_ID_SAMPLE", TRACE_ID_MIME_TYPE)
                .metadata("bearer token_001", SECURITY_TOKEN_MIME_TYPE)
                .data(new UserRequest(1))
                .retrieveMono(String.class);
    }

    @GetMapping(value = "requester2/responder")
    public Publisher<String> replayRequester2() {
        return rSocketRequester2
                .route("requester.responder")
                .metadata("TRACE_ID_SAMPLE2", TRACE_ID_MIME_TYPE)
                .metadata("bearer token_002", SECURITY_TOKEN_MIME_TYPE)
                .data(new UserRequest(2))
                .retrieveMono(String.class);
    }

    private Map<String, Object> buildMapHeader() {
        Map<String, Object> map = new HashMap<>(2);
        map.put("foo", "bar");
        map.put("fto", Foo.builder().name("car").build());
        return map;
    }

    /*************************graceful shutdown***********************/
    @GetMapping(value = "slow/handler")
    public Publisher<String> slowHandler() throws InterruptedException {
        log.info("Receive a 'slow/handler' request.");
        Thread.sleep(10 * 1000);
        return Mono.justOrEmpty("complete");
    }
}
