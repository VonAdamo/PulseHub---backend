package com.pulsehub.bffservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidatorTest {

    private static final String SECRET = "test-secret";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtValidator jwtValidator = new JwtValidator(objectMapper, SECRET);

    @Test
    void returnsTrueForValidToken() throws Exception {
        String token = tokenWithExpiration(Instant.now().plusSeconds(60));

        assertThat(jwtValidator.isValid(token)).isTrue();
    }

    @Test
    void returnsClaimsForValidToken() throws Exception {
        String token = tokenWithExpiration(Instant.now().plusSeconds(60));

        assertThat(jwtValidator.validate(token))
                .hasValue(new JwtClaims("user-id", "milla"));
    }

    @Test
    void returnsFalseForExpiredToken() throws Exception {
        String token = tokenWithExpiration(Instant.now().minusSeconds(60));

        assertThat(jwtValidator.isValid(token)).isFalse();
    }

    @Test
    void returnsFalseForInvalidSignature() throws Exception {
        String token = tokenWithExpiration(Instant.now().plusSeconds(60)) + "changed";

        assertThat(jwtValidator.isValid(token)).isFalse();
    }

    @Test
    void returnsFalseForMalformedToken() {
        assertThat(jwtValidator.isValid("not-a-jwt")).isFalse();
    }

    private String tokenWithExpiration(Instant expiresAt) throws Exception {
        String header = base64Url(objectMapper.writeValueAsBytes(Map.of(
                "alg", "HS256",
                "typ", "JWT"
        )));
        String payload = base64Url(objectMapper.writeValueAsBytes(Map.of(
                "sub", "user-id",
                "username", "milla",
                "iat", Instant.now().getEpochSecond(),
                "exp", expiresAt.getEpochSecond()
        )));
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
