package com.pulsehub.authservice.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class JwtService {

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(AuthUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        String header = encodeJson(Map.of(
                "alg", "HS256",
                "typ", "JWT"
        ));
        String payload = encodeJson(Map.of(
                "sub", user.getUserId().toString(),
                "username", user.getUsername(),
                "iat", now.getEpochSecond(),
                "exp", expiresAt.getEpochSecond()
        ));
        String unsignedToken = header + "." + payload;

        return unsignedToken + "." + sign(unsignedToken);
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return base64Url(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not create JWT payload", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign JWT", exception);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
