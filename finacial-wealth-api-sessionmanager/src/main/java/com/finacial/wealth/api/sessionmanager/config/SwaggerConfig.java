package com.finacial.wealth.api.sessionmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 *
 * @author Oshin
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("cwg.kulean.api.sessionsmanager.controllers"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

     private ApiInfo apiInfo() {
        String apiName = "CWG Fellow Pay API";
        String description = "Fellow Pay Backend - Session Manager Service.";

        String version = "1.0";
        String tosUrl = "urn:tos";
        String license = "Fellow Pay 1.0";
        String licenseUrl = "";
        String apiContact = "CWG Fellow Pay. Development team, email:";

        ApiInfo apiInfo = new ApiInfo(
                apiName, description, version, tosUrl, apiContact, license, licenseUrl
        );
        return apiInfo;
    }
}