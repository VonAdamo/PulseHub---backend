package com.pulsehub.messageservice.message;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MessageControllerTest {

    private final MessageService messageService = mock(MessageService.class);
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new MessageController(messageService))
                .setControllerAdvice(new MessageExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void createMessageReturnsCreatedMessage() throws Exception {
        UUID senderId = UUID.randomUUID();
        CreateMessageRequest request = new CreateMessageRequest(senderId, "milla", "general", "Hej fran PulseHub");
        Message message = message(UUID.randomUUID(), senderId, "milla", "general", "Hej fran PulseHub");

        when(messageService.createMessage(request)).thenReturn(message);

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senderId": "%s",
                                  "username": "milla",
                                  "channel": "general",
                                  "content": "Hej fran PulseHub"
                                }
                                """.formatted(senderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(message.getId().toString()))
                .andExpect(jsonPath("$.senderId").value(senderId.toString()))
                .andExpect(jsonPath("$.username").value("milla"))
                .andExpect(jsonPath("$.channel").value("general"))
                .andExpect(jsonPath("$.content").value("Hej fran PulseHub"));
    }

    @Test
    void createMessageRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "channel": "general",
                                  "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessagesReturnsMessages() throws Exception {
        UUID senderId = UUID.randomUUID();

        when(messageService.getMessages(null)).thenReturn(List.of(
                message(UUID.randomUUID(), senderId, "milla", "general", "One"),
                message(UUID.randomUUID(), senderId, "adam", "general", "Two")
        ));

        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("One"))
                .andExpect(jsonPath("$[1].content").value("Two"));
    }

    @Test
    void getMessagesFiltersByChannel() throws Exception {
        UUID senderId = UUID.randomUUID();

        when(messageService.getMessages("general")).thenReturn(List.of(
                message(UUID.randomUUID(), senderId, "milla", "general", "One")
        ));

        mockMvc.perform(get("/messages").param("channel", "general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].channel").value("general"));
    }

    @Test
    void getMessageReturnsMessage() throws Exception {
        UUID id = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        when(messageService.getMessage(id)).thenReturn(message(id, senderId, "milla", "general", "One"));

        mockMvc.perform(get("/messages/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.content").value("One"));
    }

    @Test
    void getMessageReturnsNotFoundProblem() throws Exception {
        UUID id = UUID.randomUUID();

        when(messageService.getMessage(id)).thenThrow(new MessageNotFoundException(id));

        mockMvc.perform(get("/messages/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Message not found"))
                .andExpect(jsonPath("$.detail").value("Message not found: " + id));
    }

    @Test
    void createMessageReturnsServiceUnavailableWhenEventCannotBePublished() throws Exception {
        UUID senderId = UUID.randomUUID();
        CreateMessageRequest request = new CreateMessageRequest(senderId, "milla", "general", "Hej");

        when(messageService.createMessage(request)).thenThrow(
                new MessageEventPublishException("Could not publish message-published event", new RuntimeException("RabbitMQ down"))
        );

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senderId": "%s",
                                  "username": "milla",
                                  "channel": "general",
                                  "content": "Hej"
                                }
                                """.formatted(senderId)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Message event could not be published"))
                .andExpect(jsonPath("$.detail").value("Could not publish message-published event"));
    }

    private Message message(UUID id, UUID senderId, String username, String channel, String content) {
        Message message = new Message(senderId, username, channel, content);
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", Instant.parse("2026-06-09T10:00:00Z"));
        ReflectionTestUtils.setField(message, "updatedAt", Instant.parse("2026-06-09T10:00:00Z"));
        return message;
    }
}
