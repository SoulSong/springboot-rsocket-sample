package com.shf.server.repository;

import com.shf.entity.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Description:
 *
 * @author: songhaifeng
 * @date: 2019/11/18 14:57
 */
@Repository
@Slf4j
public class UserRepository {
    private static final Map<Integer, User> USERS = new HashMap<>(3);

    static {
        USERS.put(1, User.builder().id(1).age(18).name("foo").build());
        USERS.put(2, User.builder().id(2).age(20).name("bar").build());
        USERS.put(3, User.builder().id(3).age(25).name("car").build());
    }

    public Mono<User> getOne(int id) {
        return Mono.justOrEmpty(USERS.get(id));
    }

    public Mono<Boolean> add(User user) {
        log.info("add a user : {}", user.toString());
        if (USERS.containsKey(user.getId())) {
            return Mono.just(Boolean.FALSE);
        }
        USERS.put(user.getId(), user);
        return Mono.just(Boolean.TRUE);
    }

    public Flux<User> list() {
        return Flux.fromStream(USERS.values().stream())
                .delayElements(Duration.ofSeconds(3)).log();
    }
}
