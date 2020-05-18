package com.shf.pingpong;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Description:
 *
 * @author: songhaifeng
 * @date: 2019/11/19 16:41
 */
@SpringBootApplication
public class PingPong {

    static String reply(String in) {
        if (in.equalsIgnoreCase("ping")) {
            return "pong";
        }
        if (in.equalsIgnoreCase("pong")) {
            return "ping";
        }
        throw new IllegalArgumentException("incoming value must be either 'ping' or 'pong'! ");
    }

    public static void main(String[] args) {
        SpringApplication.run(PingPong.class, args);
    }
}

/**
 * Client side
 */
@Slf4j
@Component
class Ping implements ApplicationListener<ApplicationReadyEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        log.info("starting " + this.getClass().getName());

        Mono<RSocket> start = RSocketConnector.create()
                .connect(TcpClientTransport.create(7000));

        start.flatMapMany(socket ->
                // send a requestChannel type request, it's the bi-directional streams mode.
                socket.requestChannel(
                        Flux.interval(Duration.ofSeconds(1)).map(i -> DefaultPayload.create("ping")))
                        .map(Payload::getDataUtf8)
                        .doOnNext(str -> log.info("Client side received " + str + " in " + getClass()))
                        // send 10 messages then close
                        .take(10)
                        .doFinally(signal -> socket.dispose())
        ).then().block();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

/**
 * Server side
 */
@Slf4j
@Component
class Pong implements SocketAcceptor, Ordered, ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        RSocketServer.create()
                .acceptor(this)
                .bind(TcpServerTransport.create(7000))
                .block();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload, RSocket rSocket) {

        RSocket rs = new RSocket() {
            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                return Flux
                        .from(payloads)
                        .map(Payload::getDataUtf8)
                        .doOnNext(str -> log.info("Server side received " + str + " in " + getClass()))
                        .map(PingPong::reply)
                        .map(DefaultPayload::create);
            }
        };

        return Mono.just(rs);
    }
}