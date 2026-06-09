package com.pulsehub.bffservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsehub.services")
public record ServiceUrlProperties(
        String authServiceUrl,
        String userServiceUrl,
        String messageServiceUrl
) {
}
