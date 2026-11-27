package com.medagenda.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfo user) {

    public static AuthResponse bearer(TokenService.IssuedToken token, UserInfo user) {
        return new AuthResponse(token.value(), "Bearer", token.expiresInSeconds(), user);
    }
}
