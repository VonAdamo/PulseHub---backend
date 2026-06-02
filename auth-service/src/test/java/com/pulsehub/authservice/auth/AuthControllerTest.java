package com.pulsehub.authservice.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new AuthExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void registerReturnsCreatedAuthUser() throws Exception {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest("milla", "Milla", "password123");

        when(authService.register(request)).thenReturn(new AuthResponse(userId, "milla", null));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "milla",
                                  "displayName": "Milla",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("milla"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void registerRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "displayName": "",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerReturnsConflictWhenUsernameExists() throws Exception {
        RegisterRequest request = new RegisterRequest("milla", "Milla", "password123");

        when(authService.register(request)).thenThrow(new UsernameAlreadyExistsException("milla"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "milla",
                                  "displayName": "Milla",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Username already exists"))
                .andExpect(jsonPath("$.detail").value("Username already exists: milla"));
    }

    @Test
    void loginReturnsJwtWhenCredentialsAreValid() throws Exception {
        UUID userId = UUID.randomUUID();
        LoginRequest request = new LoginRequest("milla", "password123");

        when(authService.login(request)).thenReturn(new AuthResponse(userId, "milla", "jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "milla",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("milla"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("milla", "wrong-password");

        when(authService.login(request)).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "milla",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid credentials"))
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }
}
