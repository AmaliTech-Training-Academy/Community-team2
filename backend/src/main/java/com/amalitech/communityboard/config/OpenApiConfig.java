package com.amalitech.communityboard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Community Builder API")
                        .version("v1.0.0")
                        .description(
                                "REST API for a community discussion board.\n\n" +
                                        "- User registration and profile management\n" +
                                        "- Email/username based login with JWT access tokens\n" +
                                        "- Refresh tokens stored in HTTP-only cookies\n" +
                                        "- Secure logout with token blacklist\n" +
                                        "- Pagination support for listing users and content"
                        )
                        .contact(new Contact()
                                .name("Amalitech Team")
                                .email("support@amalitech.com")
                        )
                );


        return openAPI;
    }
}
