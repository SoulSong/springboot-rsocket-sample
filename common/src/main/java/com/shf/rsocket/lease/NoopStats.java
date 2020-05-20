package com.shf.rsocket.lease;

import io.rsocket.lease.LeaseStats;

/**
 * Description:
 *
 * @author songhaifeng
 * @date 2019/12/25 17:14
 */
public class NoopStats implements LeaseStats {
    @Override
    public void onEvent(EventType eventType) {
    }
}
