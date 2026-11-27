package com.medagenda.auth;

import com.medagenda.auth.security.SecurityProperties;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    /** Claim read by the authorities converter in SecurityConfig. */
    public static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties.Jwt jwtProperties;

    public TokenService(JwtEncoder jwtEncoder, SecurityProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = properties.jwt();
    }

    public record IssuedToken(String value, long expiresInSeconds) {
    }

    /**
     * The subject is the user id (stable even if the e-mail changes). The token carries no
     * sensitive data: a JWT is only signed, so anyone holding it can read its claims.
     */
    public IssuedToken issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim(ROLE_CLAIM, user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(value, jwtProperties.expiration().toSeconds());
    }
}
