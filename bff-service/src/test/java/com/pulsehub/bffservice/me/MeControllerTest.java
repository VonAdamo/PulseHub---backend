package com.pulsehub.bffservice.me;

import com.pulsehub.bffservice.security.JwtClaims;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new MeController())
            .build();

    @Test
    void returnsAuthenticatedUserFromJwtClaims() throws Exception {
        mockMvc.perform(get("/api/me")
                        .requestAttr("jwtClaims", new JwtClaims("user-id", "milla")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.username").value("milla"));
    }
}
