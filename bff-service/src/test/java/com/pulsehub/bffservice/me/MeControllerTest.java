package com.pulsehub.bffservice.me;

import com.pulsehub.bffservice.security.JwtClaims;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeControllerTest {

    private static final JwtClaims CLAIMS = new JwtClaims("11111111-1111-1111-1111-111111111111", "milla");

    private final MeService meService = mock(MeService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new MeController(meService))
            .build();

    @Test
    void returnsMeResponseFromService() throws Exception {
        MeResponse response = new MeResponse(
                UUID.fromString(CLAIMS.userId()),
                "milla",
                "Milla",
                Instant.parse("2026-06-01T10:00:00Z"),
                12L
        );

        when(meService.getMe(CLAIMS)).thenReturn(response);

        mockMvc.perform(get("/api/me")
                        .requestAttr("jwtClaims", CLAIMS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(CLAIMS.userId()))
                .andExpect(jsonPath("$.username").value("milla"))
                .andExpect(jsonPath("$.displayName").value("Milla"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-01T10:00:00Z"))
                .andExpect(jsonPath("$.sentMessagesCount").value(12L));
    }

    @Test
    void passesJwtClaimsToService() throws Exception {
        MeResponse response = new MeResponse(
                UUID.fromString(CLAIMS.userId()),
                "milla",
                "Milla",
                Instant.parse("2026-06-01T10:00:00Z"),
                12L
        );

        when(meService.getMe(CLAIMS)).thenReturn(response);

        mockMvc.perform(get("/api/me")
                        .requestAttr("jwtClaims", CLAIMS))
                .andExpect(status().isOk());

        verify(meService).getMe(CLAIMS);
    }
}
