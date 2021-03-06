package com.shf.client;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Expose 8080 for the webFlux server;
 * Expose 8081 for the RSocket server;
 * Contain two client requester.
 *
 * @author songhaifeng
 */
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.REACTIVE)
                .build()
                .run(args);
    }

}
