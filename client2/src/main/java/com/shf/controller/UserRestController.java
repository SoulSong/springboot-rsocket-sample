package com.shf.controller;

import com.shf.entity.User;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @author: songhaifeng
 * @date: 2019/11/18 15:13
 */
@RestController
@RequestMapping("/user")
public class UserRestController {

    private final RSocketRequester rSocketRequester;

    @Autowired
    public UserRestController(RSocketRequester rSocketRequester) {
        this.rSocketRequester = rSocketRequester;
    }

    /***********************************request/response ******************************/
    @GetMapping(value = "{id}")
    public Publisher<User> user(@PathVariable("id") int id) {
        return rSocketRequester
                .route("user." + id)
                .retrieveMono(User.class);
    }

}
