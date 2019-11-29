package com.shf;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * WebFlux server
 *
 * @author songhaifeng
 */
@SpringBootApplication
public class Client2Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Client2Application.class)
                .web(WebApplicationType.REACTIVE)
                .build()
                .run(args);
    }

}
