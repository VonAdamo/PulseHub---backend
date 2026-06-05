package com.pulsehub.bffservice.security;

public record JwtClaims(
        String userId,
        String username
) {
}
