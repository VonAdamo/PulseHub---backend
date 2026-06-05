package com.pulsehub.bffservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    }
}
