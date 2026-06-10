package com.pulsehub.bffservice.auth;

import com.pulsehub.bffservice.user.CreateUserRequest;
import com.pulsehub.bffservice.user.UserResponse;
import com.pulsehub.bffservice.user.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthRegistrationServiceTest {

    private final AuthServiceClient authServiceClient = mock(AuthServiceClient.class);
    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private final AuthRegistrationService authRegistrationService = new AuthRegistrationService(authServiceClient, userServiceClient);

    @Test
    void registerCreatesAuthUserThenUserProfile() {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest("milla", "Milla", "password123");

        when(authServiceClient.register(request)).thenReturn(new AuthResponse(userId, "milla", null));
        when(userServiceClient.createUser(new CreateUserRequest(userId, "milla", "Milla"))).thenReturn(
                new UserResponse(userId, "milla", "Milla", Instant.parse("2026-06-10T10:00:00Z"), Instant.parse("2026-06-10T10:00:00Z"))
        );

        RegisterResponse response = authRegistrationService.register(request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("milla");
        assertThat(response.displayName()).isEqualTo("Milla");
        verify(userServiceClient).createUser(new CreateUserRequest(userId, "milla", "Milla"));
    }

    @Test
    void registerThrowsClearErrorWhenProfileCreationFailsAfterAuthUserWasCreated() {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest("milla", "Milla", "password123");

        when(authServiceClient.register(request)).thenReturn(new AuthResponse(userId, "milla", null));
        when(userServiceClient.createUser(new CreateUserRequest(userId, "milla", "Milla")))
                .thenThrow(new ResourceAccessException("user-service down"));

        assertThatThrownBy(() -> authRegistrationService.register(request))
                .isInstanceOf(RegistrationProfileCreationException.class)
                .hasMessage("Auth user was created, but user profile could not be created")
                .extracting("userId", "username")
                .containsExactly(userId, "milla");
    }
}
