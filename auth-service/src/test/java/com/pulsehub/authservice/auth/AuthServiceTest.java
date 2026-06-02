package com.pulsehub.authservice.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final AuthUserRepository authUserRepository = mock(AuthUserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService authService = new AuthService(authUserRepository, passwordEncoder, jwtService);

    @Test
    void registerCreatesAuthUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest("milla", "Milla", "password123");
        AuthUser savedUser = new AuthUser(java.util.UUID.randomUUID(), request.username(), "hashed-password");

        when(authUserRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(authUserRepository.save(any(AuthUser.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(request);

        assertThat(response.userId()).isEqualTo(savedUser.getUserId());
        assertThat(response.username()).isEqualTo("milla");
        assertThat(response.token()).isNull();
        verify(authUserRepository).save(any(AuthUser.class));
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("milla", "Milla", "password123");

        when(authUserRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists: milla");
    }

    @Test
    void loginReturnsJwtWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("milla", "password123");
        AuthUser user = new AuthUser(java.util.UUID.randomUUID(), request.username(), "hashed-password");

        when(authUserRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.username()).isEqualTo("milla");
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void loginThrowsWhenUsernameDoesNotExist() {
        LoginRequest request = new LoginRequest("missing", "password123");

        when(authUserRepository.findByUsername(request.username())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void loginThrowsWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest("milla", "wrong-password");
        AuthUser user = new AuthUser(java.util.UUID.randomUUID(), request.username(), "hashed-password");

        when(authUserRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }
}
