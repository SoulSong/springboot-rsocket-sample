package com.shf.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.reactive.PayloadMethodArgumentResolver;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Mock as a rSocket server.
 *
 * @author songhaifeng
 * @date 2019/11/18 11:35
 */
@Controller
@Slf4j
public class UserController {

    /***********************************request/response******************************/
    /**
     * Test for
     * - customized payloadInterceptor for logging request in server side
     * - authentication by username and password
     * - ReactiveSecurityContextHolder
     * <p>
     * Payload extract with {@link PayloadMethodArgumentResolver}
     *
     * @param user       payload data
     * @param id         variable template
     * @param properties metadata
     * @return {@link com.shf.entity.User}
     */
    @MessageMapping("user.{id}")
    public Mono<com.shf.entity.User> user(com.shf.entity.User user
            , @DestinationVariable int id
            , @Header Map<String, Object> properties) {
        // log metadata keys
        log.info("keys of properties : {}", String.join("、", properties.keySet()));
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(au -> (User) au.getPrincipal())
                .map(User::getUsername).map(username -> {
                    // read from security-context
                    return com.shf.entity.User.builder().id(id).age(user.getAge()).name(user.getName()).build();
                });
    }

    /***********************************ConnectMapping******************************/
    private static final Map<String, RSocketRequester> REQUESTER_MAP = new ConcurrentHashMap<>();

    @ConnectMapping
    Mono<Void> allConnect(RSocketRequester rSocketRequester, @Payload String clientId, @Header(value = "connect-metadata") List<String> metadatas) {
        log.info("Default ConnectMapping, match all connect.Client_id: {} . metadata: {}", clientId, metadatas.toArray(new String[0]));
        REQUESTER_MAP.put(clientId, rSocketRequester);
        return Mono.empty();
    }

    @PreDestroy
    void shutdown() {
        log.info("Detaching all remaining clients...");
        REQUESTER_MAP.values().forEach(requester -> requester.rsocket().dispose());
        log.info("Shutting down.");
    }

}
