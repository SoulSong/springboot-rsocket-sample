package com.shf.rsocket.lease;

import io.rsocket.lease.Lease;

import java.util.Optional;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import static java.time.Duration.ofSeconds;

/**
 * Description:
 * Class responsible for issuing leases.
 *
 * @author songhaifeng
 * @date 2019/12/25 17:07
 */
@Slf4j
public class LeaseSender implements Function<Optional<NoopStats>, Flux<Lease>> {
    private final ServerRoleEnum serverRole;
    private final int ttlMillis;
    private final int allowedRequests;

    public LeaseSender(ServerRoleEnum serverRole, int ttlMillis, int allowedRequests) {
        this.serverRole = serverRole;
        this.ttlMillis = ttlMillis;
        this.allowedRequests = allowedRequests;
    }

    @Override
    public Flux<Lease> apply(Optional<NoopStats> leaseStats) {
        log.info("{} : stats are {}", serverRole, leaseStats.isPresent() ? "present" : "absent");
        // Lease is renewed every 10 seconds. Suggest the frequency smaller then ttl.
        return Flux.interval(ofSeconds(1), ofSeconds(10))
                .onBackpressureLatest()
                .map(tick -> {
                    log.debug("{} : responder sends new leases: ttl: {}, requests: {}",
                            serverRole, ttlMillis, allowedRequests);
                    return Lease.create(ttlMillis, allowedRequests);
                });
    }
}
