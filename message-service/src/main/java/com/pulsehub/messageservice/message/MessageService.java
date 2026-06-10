package com.pulsehub.messageservice.message;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private static final String DEFAULT_CHANNEL = "general";
    private static final UUID PULSEBOT_SENDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String PULSEBOT_USERNAME = "PulseBot";

    private final MessageRepository messageRepository;
    private final MessageEventPublisher messageEventPublisher;
    private final UserProfileClient userProfileClient;

    public MessageService(
            MessageRepository messageRepository,
            MessageEventPublisher messageEventPublisher,
            UserProfileClient userProfileClient
    ) {
        this.messageRepository = messageRepository;
        this.messageEventPublisher = messageEventPublisher;
        this.userProfileClient = userProfileClient;
    }

    @Transactional
    public Message createMessage(CreateMessageRequest request) {
        String username = resolveUsername(request);

        Message message = new Message(
                request.senderId(),
                username,
                normalizeChannel(request.channel()),
                request.content().trim()
        );

        Message savedMessage = messageRepository.save(message);
        messageEventPublisher.publishMessagePublished(savedMessage);
        return savedMessage;
    }

    private String resolveUsername(CreateMessageRequest request) {
        return userProfileClient.findUser(request.senderId())
                .map(UserProfile::username)
                .orElseGet(() -> resolveSystemSenderOrThrow(request));
    }

    private String resolveSystemSenderOrThrow(CreateMessageRequest request) {
        if (PULSEBOT_SENDER_ID.equals(request.senderId())) {
            return PULSEBOT_USERNAME;
        }

        throw new SenderProfileNotFoundException(request.senderId());
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
