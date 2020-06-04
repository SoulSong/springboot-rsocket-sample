package com.shf.reactive.mdc;

import com.shf.entity.context.MetadataContextHolder;
import org.apache.commons.collections4.MapUtils;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Map;
import java.util.Set;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/6/3 18:54
 */
public class MdcContextLifter<T> implements CoreSubscriber<T> {
    private final CoreSubscriber<T> coreSubscriber;
    private final Set<String> keysToCopy;

    public MdcContextLifter(CoreSubscriber<T> coreSubscriber, Set<String> keysToCopy) {
        this.coreSubscriber = coreSubscriber;
        this.keysToCopy = keysToCopy;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        coreSubscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T t) {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onNext(t);
    }

    @Override
    public void onError(Throwable throwable) {
        coreSubscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        coreSubscriber.onComplete();
    }

    @Override
    public Context currentContext() {
        return coreSubscriber.currentContext();
    }

    private void copyToMdc(Context context) {
        if (!context.isEmpty()) {
            Map<String, String> map = MetadataContextHolder.readFromContext(context, keysToCopy);
            if (MapUtils.isNotEmpty(map)) {
                MDC.setContextMap(map);
            }
        } else {
            MDC.clear();
        }
    }
}
