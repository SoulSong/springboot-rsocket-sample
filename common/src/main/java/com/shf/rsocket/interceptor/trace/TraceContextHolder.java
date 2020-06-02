package com.shf.rsocket.interceptor.trace;

import com.shf.entity.context.MetadataContextHolder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/28 2:28
 */
@Slf4j
public class TraceContextHolder {

    /**
     * Get the traceId from the {@link reactor.util.context.Context}, if no setting then return default value.
     *
     * @return Mono<String>
     */
    public static Mono<String> getTraceId() {
        return MetadataContextHolder.getContext()
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    Map<String, Object> metadata = new HashMap<>(2);
                    metadata.put(TraceConstant.TRACE_ID, TraceConstant.UNKNOWN);
                    return metadata;
                }))
                .map(metadata -> metadata.getOrDefault(TraceConstant.TRACE_ID, TraceConstant.UNKNOWN).toString());
    }

}
