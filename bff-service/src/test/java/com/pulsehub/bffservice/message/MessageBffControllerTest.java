package com.pulsehub.bffservice.message;

import com.pulsehub.bffservice.message.dto.MessageResponse;
import com.pulsehub.bffservice.message.dto.MessageServiceCreateRequest;
import com.pulsehub.bffservice.security.JwtClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MessageBffControllerTest {

    private final MessageServiceClient messageServiceClient = mock(MessageServiceClient.class);
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new MessageBffController(messageServiceClient))
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void createMessageUsesJwtClaimsAsSender() throws Exception {
        MessageServiceCreateRequest serviceRequest = new MessageServiceCreateRequest(
                "user-id",
                "milla",
                "general",
                "Hej fran frontend!"
        );
        MessageResponse response = message("message-id", "user-id", "milla", "general", "Hej fran frontend!");

        when(messageServiceClient.createMessage(serviceRequest)).thenReturn(response);

        mockMvc.perform(post("/api/messages")
                        .requestAttr("jwtClaims", new JwtClaims("user-id", "milla"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channel": "general",
                                  "content": "Hej fran frontend!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("message-id"))
                .andExpect(jsonPath("$.senderId").value("user-id"))
                .andExpect(jsonPath("$.username").value("milla"))
                .andExpect(jsonPath("$.content").value("Hej fran frontend!"));

        verify(messageServiceClient).createMessage(serviceRequest);
    }

    @Test
    void createMessageRejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .requestAttr("jwtClaims", new JwtClaims("user-id", "milla"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channel": "general",
                                  "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessagesForwardsRequestToMessageService() throws Exception {
        when(messageServiceClient.getMessages(null)).thenReturn(List.of(
                message("message-1", "user-id", "milla", "general", "One"),
                message("message-2", "user-id", "milla", "general", "Two")
        ));

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("One"))
                .andExpect(jsonPath("$[1].content").value("Two"));
    }

    @Test
    void getMessagesForwardsChannelFilterToMessageService() throws Exception {
        when(messageServiceClient.getMessages("general")).thenReturn(List.of(
                message("message-1", "user-id", "milla", "general", "One")
        ));

        mockMvc.perform(get("/api/messages").param("channel", "general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].channel").value("general"));
    }

    @Test
    void getMessageByIdForwardsRequestToMessageService() throws Exception {
        when(messageServiceClient.getMessageById("message-id")).thenReturn(
                message("message-id", "user-id", "milla", "general", "One")
        );

        mockMvc.perform(get("/api/messages/{id}", "message-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("message-id"))
                .andExpect(jsonPath("$.content").value("One"));
    }

    private MessageResponse message(String id, String senderId, String username, String channel, String content) {
        return new MessageResponse(
                id,
                senderId,
                username,
                channel,
                content,
                Instant.parse("2026-06-09T10:00:00Z"),
                Instant.parse("2026-06-09T10:00:00Z")
        );
    }
}
