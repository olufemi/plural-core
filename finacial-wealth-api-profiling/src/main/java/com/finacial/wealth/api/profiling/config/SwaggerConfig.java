package com.finacial.wealth.api.profiling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * @author HRH
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.finacial.wealth.api.rest"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        String apiName = "Next gen API";
        String description = "Nexgen Backend";

        String version = "1.0";
        String tosUrl = "urn:tos";
        String license = "Nextgen 1.0";
        String licenseUrl = "";
        Contact apiContact = new Contact("Fin-Wealth-Solutions T Development team ", "", " ,  ");

        ApiInfo apiInfo = new ApiInfo(
                apiName, description, version, tosUrl, apiContact, license, licenseUrl
        );
        return apiInfo;
    }
}