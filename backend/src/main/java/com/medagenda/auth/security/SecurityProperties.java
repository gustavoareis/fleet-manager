package com.medagenda.auth.security;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed view of the {@code medagenda.security.*} settings. */
@ConfigurationProperties(prefix = "medagenda.security")
public record SecurityProperties(Jwt jwt, Cors cors) {

    public record Jwt(String secret, String issuer, Duration expiration) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
