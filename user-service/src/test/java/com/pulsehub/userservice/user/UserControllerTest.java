package com.pulsehub.userservice.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new UserExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("adam", "Adam");
        User user = new User(request.username(), request.displayName());

        when(userService.createUser(request)).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "adam",
                                  "displayName": "Adam"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("adam"))
                .andExpect(jsonPath("$.displayName").value("Adam"));
    }

    @Test
    void createUserAcceptsProvidedId() throws Exception {
        UUID id = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest(id, "adam", "Adam");
        User user = new User(id, request.username(), request.displayName());

        when(userService.createUser(request)).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "%s",
                                  "username": "adam",
                                  "displayName": "Adam"
                                }
                                """.formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("adam"))
                .andExpect(jsonPath("$.displayName").value("Adam"));
    }

    @Test
    void createUserRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "displayName": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserReturnsNotFoundProblem() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.getUser(id)).thenThrow(new UserNotFoundException(id));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("User not found"))
                .andExpect(jsonPath("$.detail").value("User not found: " + id));
    }

    @Test
    void getUsersReturnsUsers() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(
                new User("adam", "Adam"),
                new User("sara", "Sara")
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("adam"))
                .andExpect(jsonPath("$[1].username").value("sara"));
    }
}
