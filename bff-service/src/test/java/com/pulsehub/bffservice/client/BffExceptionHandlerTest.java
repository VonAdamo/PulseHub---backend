package com.pulsehub.bffservice.client;

import com.pulsehub.bffservice.auth.RegistrationProfileCreationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BffExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new DownstreamErrorController())
            .setControllerAdvice(new BffExceptionHandler())
            .build();

    @Test
    void forwardsDownstreamStatusAndJsonBody() throws Exception {
        mockMvc.perform(get("/downstream-error"))
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {
                          "title": "Username already exists",
                          "detail": "Username already exists: milla"
                        }
                        """));
    }

    @Test
    void returnsClearErrorWhenRegistrationProfileCreationFails() throws Exception {
        mockMvc.perform(get("/registration-profile-error"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Registration profile creation failed"))
                .andExpect(jsonPath("$.detail").value("Auth user was created, but user profile could not be created. This can leave a partially created account."))
                .andExpect(jsonPath("$.userId").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.username").value("milla"))
                .andExpect(jsonPath("$.downstreamService").value("user-service"))
                .andExpect(jsonPath("$.downstreamResponse").value("user-service down"));
    }

    @RestController
    private static class DownstreamErrorController {

        @GetMapping("/downstream-error")
        String downstreamError() {
            throw new HttpClientErrorException(
                    HttpStatus.CONFLICT,
                    "Conflict",
                    "{\"title\":\"Username already exists\",\"detail\":\"Username already exists: milla\"}".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            );
        }

        @GetMapping("/registration-profile-error")
        String registrationProfileError() {
            throw new RegistrationProfileCreationException(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    "milla",
                    new RuntimeException("user-service down")
            );
        }
    }
}
