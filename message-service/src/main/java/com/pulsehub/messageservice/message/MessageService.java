package com.pulsehub.messageservice.message;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private static final String DEFAULT_CHANNEL = "general";

    private final MessageRepository messageRepository;
    private final MessageEventPublisher messageEventPublisher;

    public MessageService(MessageRepository messageRepository, MessageEventPublisher messageEventPublisher) {
        this.messageRepository = messageRepository;
        this.messageEventPublisher = messageEventPublisher;
    }

    @Transactional
    public Message createMessage(CreateMessageRequest request) {
        Message message = new Message(
                request.senderId(),
                request.username().trim(),
                normalizeChannel(request.channel()),
                request.content().trim()
        );

        Message savedMessage = messageRepository.save(message);
        messageEventPublisher.publishMessagePublished(savedMessage);
        return savedMessage;
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(String channel) {
        if (channel == null || channel.isBlank()) {
            return messageRepository.findAllByOrderByCreatedAtAsc();
        }

        return messageRepository.findByChannelOrderByCreatedAtAsc(normalizeChannel(channel));
    }

    @Transactional(readOnly = true)
    public Message getMessage(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return DEFAULT_CHANNEL;
        }

        return channel.trim();
    }
}
