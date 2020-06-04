package com.shf.reactive.mdc;

import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

import java.util.Set;

/**
 * description :
 * <p>
 * refer to : https://www.novatec-gmbh.de/en/blog/how-can-the-mdc-context-be-used-in-the-reactive-spring-applications/
 *
 * @author songhaifeng
 * @date 2020/6/3 23:15
 */
public class MdcContextLifterHook {

    private static final String MDC_CONTEXT_REACTOR_KEY = MdcContextLifterHook.class.getName();
    private final Set<String> keysToCopy;

    public MdcContextLifterHook(Set<String> keysToCopy) {
        this.keysToCopy = keysToCopy;
    }

    public void contextOperatorHook() {
        if (CollectionUtils.isEmpty(keysToCopy)) {
            return;
        }
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY,
                Operators.lift((scannable, coreSubscriber) -> new MdcContextLifter<>(coreSubscriber, keysToCopy)));
    }

    public void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }

}
