package com.shf.server.controller;

import com.shf.entity.Foo;
import com.shf.entity.User;
import com.shf.entity.UserRequest;
import com.shf.server.repository.UserRepository;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.shf.rsocket.mimetype.MimeTypes.SECURITY_TOKEN_MIME_TYPE;

/**
 * Description:
 *
 * @author: songhaifeng
 * @date: 2019/11/18 11:35
 */
@Controller
@Slf4j
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /***********************************request/response******************************/
    @MessageMapping("user")
    public Mono<User> user(UserRequest userRequest) {
        return userRepository.getOne(userRequest.getId());
    }

    /***********************************Fire And Forget******************************/
    @MessageMapping("add.user")
    public Mono<Void> add(User user) {
        userRepository.add(user);
        return Mono.empty();
    }

    /***********************************Request Stream******************************/
    /**
     * Returning a Flux<User> instead of a Mono<User>.
     * In the end, our RSocket server will send multiple responses to the client.
     *
     * @return
     */
    @MessageMapping("list")
    public Flux<User> list() {
        return userRepository.list();
    }


    /***********************************Request Channel******************************/
    @MessageMapping("request.channel")
    public Flux<User> requestChannel(Flux<User> users) {
        return users;
    }

    /***********************************Invoke Error******************************/

    @MessageMapping("user.error")
    public Mono<User> invokeError() {
        throw new IllegalArgumentException();
    }

    @MessageExceptionHandler
    public Mono<User> handleException(Exception e) {
        log.error(e.getMessage());
        return Mono.just(User.builder().id(-1).build());
    }


    /***********************************Send metadata(header)******************************/
    /**
     * <p>@Header — access to a metadata value registered for extraction, as described in MetadataExtractor.</p>
     *
     * @param securityToken securityToken
     * @param refreshToken  refreshToken
     * @param userRequest   userRequest
     * @return Mono<String>
     */
    @MessageMapping("send.string.header")
    public Mono<String> sendStringHeader(@Header String securityToken, @Header String refreshToken, UserRequest userRequest) {
        return userRepository.getOne(userRequest.getId()).map(user ->
                "Your(" + user.getName() + ") securityToken is '" + securityToken + "' and refreshToken is '" + refreshToken + "'"
        );
    }

    /**
     * <p>@Headers Map<String, Object> — access to all metadata values registered for extraction, as described in MetadataExtractor.</p>
     *
     * @param headers headers
     * @return Mono<Void>
     */
    @MessageMapping("send.headers")
    public Mono<Void> sendHeaders(@Headers Map<String, Object> headers) {
        headers.forEach((key, value) -> {
            log.info("{} ==> {}", key, value);
        });
        return Mono.empty();
    }

    /**
     * Mapping 'register.metadataToExtract(MimeType.valueOf("application/vnd.foo.metadata+json"), Foo.class, "foo");'
     *
     * @param foo metadata
     * @return String
     */
    @MessageMapping("send.entity.header")
    public Mono<String> sendEntityHeader(@Header Foo foo) {
        return Mono.just(foo.getName());
    }

    /**
     * Mapping 'register.metadataToExtract(MimeType.valueOf("application/vnd.map.metadata+json"),
     * new ParameterizedTypeReference<Map<String, Object>>() {}, "properties");'
     *
     * @param map metadata
     * @return String
     */
    @MessageMapping("send.map.header")
    public Mono<String> sendMapHeader(@Header(name = "properties") Map<String, Object> map) {
        if (MapUtils.isNotEmpty(map)) {
            return Mono.just(map.values().toString());
        }
        return Mono.empty();
    }

    /***********************************DestinationVariable******************************/
    /**
     * <p>@DestinationVariable — the value for a variable from the pattern, e.g. @MessageMapping("find.radar.{id}").</p>
     *
     * @param id id
     * @return Mono<User>
     */
    @MessageMapping("user.{id}")
    public Mono<User> destinationVariable(@DestinationVariable(value = "id") int id) {
        return userRepository.getOne(id);
    }

    /***********************************ConnectMapping******************************/
    private static final Map<String, RSocketRequester> REQUESTER_MAP = new HashMap<>();

    /**
     * Matches all connects without route.
     * Keep in mind that @ConnectMapping methods are essentially handlers of the SETUP frame which must be handled before requests can begin.
     *
     * @param rSocketRequester The requester for the connection associated with the request, to make requests to the remote end.
     * @param clientId         {@link io.rsocket.ConnectionSetupPayload} send with the connection
     * @param metadatas        metadata send with the connection
     * @return Must return void or Mono<Void>
     */
    @ConnectMapping
    Mono<Void> allConnect(RSocketRequester rSocketRequester,
                          @Payload String clientId,
                          @Header(value = "connect-metadata") List<String> metadatas) {
        log.info("Default ConnectMapping, match all connect.Client_id: {} . metadata: {}", clientId, metadatas.toArray(new String[0]));
        rSocketRequester.rsocket()
                // Invoke when the RSocket is closed.
                // A {@code RSocket} can be closed by explicitly calling {@link RSocket#dispose()}
                // or when the underlying transport connection is closed.
                .onClose()
                .doFirst(() -> {
                    log.info("Client: {} CONNECTED.", clientId);
                    // clientID authentication for security
                    // mock case：Client999 will be rejected.
                    if ("Client999".equalsIgnoreCase(clientId)) {
                        log.warn("Reject client({}), disconnect.", clientId);
                        rSocketRequester.rsocket().dispose();
                    } else {
                        REQUESTER_MAP.put(clientId, rSocketRequester);
                    }
                })
                .doOnError(error -> {
                    log.warn("Channel to client {} CLOSED", clientId);
                })
                .doFinally(consumer -> {
                    REQUESTER_MAP.remove(clientId, rSocketRequester);
                    log.info("Client {} DISCONNECTED", clientId);
                })
                .subscribe();
        return Mono.empty();
    }

    /**
     * Matches specific connects with the route of the setup payload({@link io.rsocket.ConnectionSetupPayload}).
     *
     * @param rSocketRequester The requester for the connection associated with the request, to make requests to the remote end.
     * @param clientId         {@link io.rsocket.ConnectionSetupPayload} send with the connection
     * @param metadatas        metadata send with the connection
     * @return Must return void or Mono<Void>
     */
    @ConnectMapping("specific.route.1.2")
    Mono<Void> onConnect1(RSocketRequester rSocketRequester,
                          @Payload String clientId,
                          @Header(value = "connect-metadata") List<String> metadatas) {
        log.info("Specific ConnectMapping. Client_id:{}. metadata: {}", clientId, metadatas.toArray(new String[0]));
        log.info("Client: {} CONNECTED.", clientId);
        return Mono.empty();
    }

    /***********************************Demo Server Requester******************************/
    /**
     * @param rSocketRequester The requester for the connection associated with the request, to make requests to the remote end.
     *                         Here is used for requesting to the client side.
     * @param securityToken    metadata named securityToken
     * @param userRequest      request data
     * @return String
     */
    @MessageMapping("requester.responder")
    public Mono<String> requestCallback(RSocketRequester rSocketRequester,
                                        @Header String securityToken,
                                        UserRequest userRequest) {
        return userRepository.getOne(userRequest.getId()).flatMap(user -> {
            log.info("Your(" + user.getName() + ") securityToken is '" + securityToken + "'");
            return rSocketRequester.route("client.responder.user")
                    .metadata(securityToken, SECURITY_TOKEN_MIME_TYPE)
                    .data(User.builder().id(1).age(12).name("coco").build())
                    .retrieveMono(String.class);
        });
    }


}
