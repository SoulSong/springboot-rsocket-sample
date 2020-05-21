package com.shf.rsocket.lease;

import io.rsocket.lease.Lease;

import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;


/**
 * Class responsible for receiving leases.
 * The information received here can be used by 'decent' clients
 * to stop requesting new connections when their leases are depleted.
 * Clients that do not honor leases will receive an error if they
 * try to received a new connection without a valid lease. But it
 * would be better if they do not even try, if they know they will fail.
 *
 * <p>
 * The implementation is based on this sample:
 * https://github.com/rsocket/rsocket-java/blob/master/rsocket-examples/src/main/java/io/rsocket/examples/transport/tcp/lease/LeaseExample.java
 *
 * @author songhaifeng
 * @date 2019/12/25 17:23
 */
@Slf4j
public class LeaseReceiver implements Consumer<Flux<Lease>> {

    private final ServerRoleEnum serverRole;

    public LeaseReceiver(ServerRoleEnum serverRole) {
        this.serverRole = serverRole;
    }

    @Override
    public void accept(Flux<Lease> receivedLeases) {
        receivedLeases.subscribe(
                lease -> log.debug("{} : requester receives leases - ttl: {}, requests: {}, availability: {}",
                        serverRole, lease.getTimeToLiveMillis(), lease.getAllowedRequests(), lease.availability()
                )
        );


    }
}
