package com.shf.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/7/24 0:40
 */
@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .genericModelSubstitutes(ResponseEntity.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(true)
                .pathMapping("/")
                .select()
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo("Client2-Service", "Client2-Service REST API"))
                .globalRequestParameters(requestParameters());
    }

    private ApiInfo apiInfo(final String title, final String description) {
        return new ApiInfoBuilder().title(title)
                .description(description)
                .contact(new Contact("songhaifeng", "", "songhaifengshuaige@gmail.com"))
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .version("1.0.0")
                .build();
    }

    private List<RequestParameter> requestParameters() {
        return Collections.singletonList(
                new RequestParameterBuilder()
                        .name("traceId")
                        .description("trace id")
                        .in(ParameterType.HEADER)
                        .required(false)
                        .parameterIndex(0)
                        .build()
        );
    }
}
