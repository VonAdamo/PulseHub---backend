package com.pulsehub.bffservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ServiceUrlProperties.class)
public class RestClientConfig {

    @Bean
    RestClient authRestClient(ServiceUrlProperties serviceUrlProperties) {
        return RestClient.builder()
                .baseUrl(serviceUrlProperties.authServiceUrl())
                .build();
    }

    @Bean
    RestClient userRestClient(ServiceUrlProperties serviceUrlProperties) {
        return RestClient.builder()
                .baseUrl(serviceUrlProperties.userServiceUrl())
                .build();
    }

    @Bean
    RestClient messageRestClient(ServiceUrlProperties serviceUrlProperties) {
        return RestClient.builder()
                .baseUrl(serviceUrlProperties.messageServiceUrl())
                .build();
    }
}
