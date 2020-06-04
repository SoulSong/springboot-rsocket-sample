package com.shf.reactive.mdc;

import com.shf.entity.context.MetadataContextHolder;
import com.shf.rsocket.interceptor.trace.TraceConstant;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.MDC;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/6/5 0:43
 */
public class MdcReactiveUtils {

    public static <T> Consumer<Signal<T>> mdcOnEach(Consumer<Signal<T>> mdcStatement) {
        return signal -> {
            // except the complete and error events
            if (signal.isOnComplete() || signal.isOnError()) {
                Map<String, String> map = MetadataContextHolder.readFromContext(signal.getContext(), Collections.singleton(TraceConstant.TRACE_ID));
                if (MapUtils.isNotEmpty(map)) {
                    MDC.setContextMap(map);
                    mdcStatement.accept(signal);
                    MDC.clear();
                }
            }
        };
    }

    public static Runnable mdcOnComplete(Runnable mdcStatement, Context context) {
        Map<String, String> map = MetadataContextHolder.readFromContext(context, Collections.singleton(TraceConstant.TRACE_ID));
        if (MapUtils.isNotEmpty(map)) {
            MDC.setContextMap(map);
            mdcStatement.run();
            MDC.clear();
        }
        return mdcStatement;
    }

    public static void mdcOnError(Consumer<? super Throwable> onError, Throwable throwable, Context context) {
        Map<String, String> map = MetadataContextHolder.readFromContext(context, Collections.singleton(TraceConstant.TRACE_ID));
        if (MapUtils.isNotEmpty(map)) {
            MDC.setContextMap(map);
            onError.accept(throwable);
            MDC.clear();
        }
    }
}
