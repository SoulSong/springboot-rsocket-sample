package com.shf.controller;

import com.shf.entity.Foo;
import com.shf.entity.User;

import io.rsocket.metadata.WellKnownMimeType;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.shf.rsocket.mimetype.MimeTypes.MAP_MIME_TYPE;

/**
 * Description:
 *
 * @author songhaifeng
 * @date 2019/11/18 15:13
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
    /**
     * Test for
     * - customized payloadInterceptor for logging request in server side
     * - authentication by username and password
     *
     * @param id id for user
     * @return User
     */
    @GetMapping(value = "{id}")
    public Publisher<User> user(@PathVariable("id") int id) {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("shf", "123456");
        Map<String, Object> properties = new HashMap<>(2);
        properties.put("property_1", "value_1");
        properties.put("property_2", Foo.builder().name("a").build());
        return rSocketRequester
                .route("user." + id)
                .metadata(credentials, MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
                .metadata(properties, MAP_MIME_TYPE)
                .data(User.builder().age(100).name("宋海锋").id(12).build())
                .retrieveMono(User.class);
    }

    /**
     * Test for ` .route("user.*").hasRole("ADMIN")` which is configured on the server side.
     *
     * @return {@link User}
     */
    @GetMapping(value = "role_not_match_authentication")
    public Publisher<User> roleNotMatchAuthentication() {
        // User(shf_2)'s role is `USER`, need the `ADMIN` role
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("shf_2", "123456");
        return rSocketRequester
                .route("user.2")
                .metadata(credentials, MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
                .data(User.builder().age(100).name("user_123").id(12).build())
                .retrieveMono(User.class);
    }

    /**
     * Test for `anyRequest().authenticated()` which is configured on the server side.
     *
     * @return {@link User}
     */
    @GetMapping(value = "user_not_authentication")
    public Publisher<User> userNotAuthentication() {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("shf_2", "111111");
        return rSocketRequester
                .route("user.3")
                .metadata(credentials, MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
                .data(User.builder().age(100).name("user_123").id(12).build())
                .retrieveMono(User.class);
    }
}
