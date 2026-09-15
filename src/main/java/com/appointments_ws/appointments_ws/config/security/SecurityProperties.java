package com.appointments_ws.appointments_ws.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.UUID;

@ConfigurationProperties("app.security")
public record SecurityProperties(String username, String password, UUID tenantId, String jwtSecret, Duration tokenTtl) {
}
