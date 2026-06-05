package com.pulsehub.bffservice.user;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserBffControllerTest {

    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new UserBffController(userServiceClient))
            .build();

    @Test
    void getUsersForwardsRequestToUserService() throws Exception {
        when(userServiceClient.getUsers()).thenReturn(List.of(
                new UserResponse(UUID.randomUUID(), "adam", "Adam", Instant.parse("2026-06-01T10:00:00Z"), Instant.parse("2026-06-01T10:00:00Z")),
                new UserResponse(UUID.randomUUID(), "milla", "Milla", Instant.parse("2026-06-01T11:00:00Z"), Instant.parse("2026-06-01T11:00:00Z"))
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("adam"))
                .andExpect(jsonPath("$[1].username").value("milla"));
    }

    @Test
    void getUserForwardsRequestToUserService() throws Exception {
        UUID id = UUID.randomUUID();

        when(userServiceClient.getUser(id)).thenReturn(
                new UserResponse(id, "milla", "Milla", Instant.parse("2026-06-01T11:00:00Z"), Instant.parse("2026-06-01T11:00:00Z"))
        );

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("milla"))
                .andExpect(jsonPath("$.displayName").value("Milla"));
    }
}
