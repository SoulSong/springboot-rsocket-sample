package com.shf.entity.context;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;

/**
 * description :
 * Store metadata in {@link Context}
 *
 * @author songhaifeng
 * @date 2020/5/28 0:45
 */
@Slf4j
public class MetadataContextHolder {
    public static final String METADATA_CONTEXT_KEY = "METADATA_CONTEXT";

    public static Mono<Map<String, Object>> getContext() {
        return Mono.subscriberContext()
                .filter(context -> context.hasKey(METADATA_CONTEXT_KEY))
                .map(c -> c.get(METADATA_CONTEXT_KEY));
    }

    public static Context setContext(Map<String, Object> metadata) {
        return Context.of(METADATA_CONTEXT_KEY, metadata);
    }

    public static Mono<Context> clearContext() {
        return Mono.subscriberContext()
                .filter(context -> context.hasKey(METADATA_CONTEXT_KEY))
                .map(c -> {
                    log.info("clear `{}` from Context.", METADATA_CONTEXT_KEY);
                    return c.delete(METADATA_CONTEXT_KEY);
                });
    }

}
