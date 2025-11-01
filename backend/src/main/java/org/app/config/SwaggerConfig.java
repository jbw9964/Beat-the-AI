package org.app.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.*;
import org.springframework.context.annotation.*;

@Configuration
class SwaggerConfig {

    @Bean
    public OpenAPI swaggerOpenAPI() {
        return new OpenAPI()
                .components(components())
                .info(info())
                .addServersItem(server())
                .addSecurityItem(
                        new SecurityRequirement().addList("Authorization")
                );
    }

    @Bean
    public Server server() {
        return new Server()
                .url("/")
                .description("Swagger will use same protocol (http/https) that current page uses.");
    }

    @Bean
    public Components components() {
        return new Components()
                .addSecuritySchemes(
                        "Authorization", jwtSecurityScheme()
                );
    }

    @Bean
    public SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }

    @Bean
    public Info info() {
        return new Info()
                .title("Beat the AI backend")
                .version("0.1")
                .description("Beat the AI backend API specification");
    }
}
