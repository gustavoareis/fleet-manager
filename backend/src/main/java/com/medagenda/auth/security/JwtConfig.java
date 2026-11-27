package com.medagenda.auth.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * HS256 (shared secret) is enough because the same application issues and validates the tokens.
 * With separate services validating them, an asymmetric key pair (RS256) would be the better fit.
 */
@Configuration
public class JwtConfig {

    private static final int MIN_KEY_BYTES = 32; // HS256 requires a key of at least 256 bits

    @Bean
    SecretKey jwtSecretKey(SecurityProperties properties) {
        String secret = properties.jwt().secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Missing JWT secret: set the JWT_SECRET environment variable (Base64, at least 256 bits).");
        }
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("JWT secret is too short: it must have at least 256 bits.");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    /** Validates signature, expiration ("exp", with a small clock skew) and issuer ("iss"). */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey, SecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.jwt().issuer()));
        return decoder;
    }
}
