package com.pulsehub.botservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsehub.services")
public record ServiceUrlProperties(
        String messageServiceUrl
) {
}
