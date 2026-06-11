package com.pulsehub.bffservice.me;

import com.pulsehub.bffservice.message.MessageServiceClient;
import com.pulsehub.bffservice.security.JwtClaims;
import com.pulsehub.bffservice.user.UserResponse;
import com.pulsehub.bffservice.user.UserServiceClient;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeServiceTest {

    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private final MessageServiceClient messageServiceClient = mock(MessageServiceClient.class);
    private final MeService meService = new MeService(userServiceClient, messageServiceClient);

    @Test
    void getMeCombinesUserProfileAndMessageCount() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        JwtClaims claims = new JwtClaims(userId.toString(), "milla");
        UserResponse user = new UserResponse(
                userId,
                "milla",
                "Milla",
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-01T10:00:00Z")
        );

        when(userServiceClient.getUser(userId)).thenReturn(user);
        when(messageServiceClient.getSentMessagesCount(userId)).thenReturn(12L);

        MeResponse response = meService.getMe(claims);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("milla");
        assertThat(response.displayName()).isEqualTo("Milla");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-06-01T10:00:00Z"));
        assertThat(response.sentMessagesCount()).isEqualTo(12L);
    }
}
