package com.nextalk.realtime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI realtimeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NexTalk Realtime Service API")
                        .version("v1")
                        .description("WebSocket STOMP events for typing, reads, presence and reactions"));
    }
}
