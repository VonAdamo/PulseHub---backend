package com.pulsehub.userservice.user;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = new UserService(userRepository);

    @Test
    void createUserSavesUserWhenUsernameIsAvailable() {
        CreateUserRequest request = new CreateUserRequest("adam", "Adam");
        User savedUser = new User(request.username(), request.displayName());

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(savedUser);

        User user = userService.createUser(request);

        assertThat(user.getUsername()).isEqualTo("adam");
        assertThat(user.getDisplayName()).isEqualTo("Adam");
        verify(userRepository).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void createUserThrowsWhenUsernameAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("adam", "Adam");

        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists: adam");
    }

    @Test
    void getUserReturnsExistingUser() {
        UUID id = UUID.randomUUID();
        User savedUser = new User("adam", "Adam");

        when(userRepository.findById(id)).thenReturn(Optional.of(savedUser));

        assertThat(userService.getUser(id)).isSameAs(savedUser);
    }

    @Test
    void getUserThrowsWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + id);
    }
}
