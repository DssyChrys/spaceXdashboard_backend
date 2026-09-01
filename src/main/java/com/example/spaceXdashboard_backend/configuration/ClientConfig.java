package com.example.spaceXdashboard_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Bean("spaceXRestClient")
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://gateway.pipeworx.io/spacex/v4")
                .build();
    }

    @Bean("spaceDevsRestClient")
    public RestClient restClient2() {
        return RestClient.builder()
                .baseUrl("https://ll.thespacedevs.com/2.3.0")
                .build();
    }

    //obliger de les nommées vu que jutilise 2 endpoint de base
}
