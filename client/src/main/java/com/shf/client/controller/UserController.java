package com.shf.client.controller;

import com.shf.entity.User;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Description:
 * Mock as a rSocket server.
 *
 * @author: songhaifeng
 * @date: 2019/11/18 11:35
 */
@Controller
@Slf4j
public class UserController {

    /***********************************request/response******************************/
    @MessageMapping("user.{id}")
    public Mono<User> user(@DestinationVariable int id) {
        return Mono.just(User.builder().id(id).age(11).name("john").build());
    }

    /***********************************ConnectMapping******************************/
    @ConnectMapping
    Mono<Void> allConnect(RSocketRequester rSocketRequester, @Payload String clientId, @Header(value = "connect-metadata") List<String> metadatas) {
        log.info("Default ConnectMapping, match all connect.Client_id: {} . metadata: {}", clientId, metadatas.toArray(new String[0]));
        return Mono.empty();
    }

}
