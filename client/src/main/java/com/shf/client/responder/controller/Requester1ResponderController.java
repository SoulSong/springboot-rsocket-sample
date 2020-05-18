package com.shf.client.responder.controller;

import com.shf.client.responder.annotation.RSocketClientResponder1;
import com.shf.entity.User;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Description:
 * The first client responder.
 *
 * @author: songhaifeng
 * @date: 2019/11/27 22:36
 */
@RSocketClientResponder1("requester1ResponderController")
@Slf4j
public class Requester1ResponderController {

    @MessageMapping("client.responder.user")
    public Mono<String> respondToServer(User user, @Header String securityToken) {
        log.info("ClientResponder1 --> receive payload:[{}] ,header:[securityToken:{}]", user.toString(), securityToken);
        return Mono.just(user.getName());
    }
}
