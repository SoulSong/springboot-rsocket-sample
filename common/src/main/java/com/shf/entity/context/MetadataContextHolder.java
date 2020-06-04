package com.shf.entity.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description :
 * Store metadata in {@link Context}
 *
 * @author songhaifeng
 * @date 2020/5/28 0:45
 */
@Slf4j
public class MetadataContextHolder {
    public static final String METADATA_CONTEXT_KEY = MetadataContextHolder.class.getName();

    public static Mono<Map<String, Object>> getContext() {
        return Mono.subscriberContext()
                .filter(context -> context.hasKey(METADATA_CONTEXT_KEY))
                .map(c -> c.get(METADATA_CONTEXT_KEY));
    }

    public static Context setContext(Map<String, Object> metadata) {
        log.info("Load metadata into context.");
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

    /**
     * Copy some special keys from {@link Context} into a {@link Map}.
     *
     * @param context    {@link Context}
     * @param keysToCopy a set collection of keys which need to be copy.
     * @return {@link Map}
     */
    public static Map<String, String> readFromContext(Context context, Set<String> keysToCopy) {
        if (CollectionUtils.isEmpty(keysToCopy)) {
            return new HashMap<>(1);
        }
        Map<String, String> map = context.stream()
                .filter(entry -> keysToCopy.contains(entry.getKey().toString()) && null != entry.getValue())
                .collect(Collectors.toMap(entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()));
        // Read from metadata if there are still some keys not found.
        if (map.size() < keysToCopy.size() && context.hasKey(MetadataContextHolder.METADATA_CONTEXT_KEY)) {
            Map<String, Object> metadata = context.get(MetadataContextHolder.METADATA_CONTEXT_KEY);
            metadata.forEach((key, value) -> {
                if (keysToCopy.contains(key) && null != value) {
                    map.put(key, value.toString());
                }
            });
        }
        return map;
    }
}
