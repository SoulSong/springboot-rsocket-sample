package com.shf.client.responder.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 * Used for {@link RSocketRequester} named rSocketRequester1.
 *
 * @author: songhaifeng
 * @date: 2019/11/29 01:50
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RSocketClientResponder1 {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
