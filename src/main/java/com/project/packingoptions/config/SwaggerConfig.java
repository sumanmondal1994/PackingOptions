package com.project.packingoptions.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${openapi.title:Grocery Shop API}")
    private String apiTitle;

    @Value("${openapi.version:1.0.0}")
    private String apiVersion;

    @Value("${openapi.description:API Documentation}")
    private String apiDescription;

    @Value("${openapi.contact.name:API Support}")
    private String contactName;

    @Value("${openapi.contact.email:support@example.com}")
    private String contactEmail;

    @Value("${openapi.license.name:Apache 2.0}")
    private String licenseName;

    @Value("${openapi.license.url:http://www.apache.org/licenses/LICENSE-2.0.html}")
    private String licenseUrl;

    @Value("${openapi.server.description:Development Server}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(apiTitle)
                        .version(apiVersion)
                        .description(apiDescription)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description(serverDescription)
                ));
    }
}

