package com.pulsehub.messageservice.message;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageServiceTest {

    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final MessageEventPublisher messageEventPublisher = mock(MessageEventPublisher.class);
    private final MessageService messageService = new MessageService(messageRepository, messageEventPublisher);

    @Test
    void createMessageUsesGeneralChannelWhenChannelIsBlank() {
        CreateMessageRequest request = new CreateMessageRequest(
                UUID.randomUUID(),
                "milla",
                " ",
                "Hej fran PulseHub"
        );
        Message savedMessage = new Message(request.senderId(), request.username(), "general", request.content());

        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class))).thenReturn(savedMessage);

        Message message = messageService.createMessage(request);

        assertThat(message.getChannel()).isEqualTo("general");
        verify(messageRepository).save(org.mockito.ArgumentMatchers.any(Message.class));
    }

    @Test
    void createMessagePublishesEventAfterSaving() {
        CreateMessageRequest request = new CreateMessageRequest(
                UUID.randomUUID(),
                "milla",
                "general",
                "Hej fran PulseHub"
        );
        Message savedMessage = new Message(request.senderId(), request.username(), request.channel(), request.content());

        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class))).thenReturn(savedMessage);

        messageService.createMessage(request);

        verify(messageEventPublisher).publishMessagePublished(savedMessage);
    }

    @Test
    void getMessagesReturnsAllMessagesWhenChannelIsMissing() {
        messageService.getMessages(null);

        verify(messageRepository).findAllByOrderByCreatedAtAsc();
    }

    @Test
    void getMessagesFiltersByNormalizedChannel() {
        messageService.getMessages(" general ");

        verify(messageRepository).findByChannelOrderByCreatedAtAsc("general");
    }

    @Test
    void getMessageReturnsExistingMessage() {
        UUID id = UUID.randomUUID();
        Message savedMessage = new Message(UUID.randomUUID(), "milla", "general", "Hej");

        when(messageRepository.findById(id)).thenReturn(Optional.of(savedMessage));

        assertThat(messageService.getMessage(id)).isSameAs(savedMessage);
    }

    @Test
    void getMessageThrowsWhenMessageDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(messageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessage(id))
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessage("Message not found: " + id);
    }
}
