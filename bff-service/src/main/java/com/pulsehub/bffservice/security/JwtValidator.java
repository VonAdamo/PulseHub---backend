package com.pulsehub.bffservice.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtValidator {

    private final ObjectMapper objectMapper;
    private final String secret;

    public JwtValidator(ObjectMapper objectMapper, @Value("${auth.jwt.secret}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    public boolean isValid(String token) {
        return validate(token).isPresent();
    }

    public Optional<JwtClaims> validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!sign(unsignedToken).equals(parts[2])) {
                return Optional.empty();
            }

            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );

            Object expiresAt = payload.get("exp");
            if (!(expiresAt instanceof Number expirationEpochSeconds)) {
                return Optional.empty();
            }

            if (Instant.now().getEpochSecond() >= expirationEpochSeconds.longValue()) {
                return Optional.empty();
            }

            Object subject = payload.get("sub");
            Object username = payload.get("username");
            if (!(subject instanceof String userId) || !(username instanceof String usernameValue)) {
                return Optional.empty();
            }

            return Optional.of(new JwtClaims(userId, usernameValue));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
