package com.shf.client.responder.controller;

import com.shf.client.responder.annotation.RSocketClientResponder2;
import com.shf.entity.User;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Description:
 * The second client responder.
 *
 * @author: songhaifeng
 * @date: 2019/11/27 22:36
 */
@RSocketClientResponder2("requester2ResponderController")
@Slf4j
public class Requester2ResponderController {

    @MessageMapping("responder.user")
    public Mono<String> respondToServer(User user,
                                        @Header String securityToken,
                                        @Header String refreshToken) {
        log.info("Responder2 --> user_name:[{}] securityToken:[{}] refreshToken:[{}]", user.getName(), securityToken, refreshToken);
        return Mono.just(user.getName());
    }
}
