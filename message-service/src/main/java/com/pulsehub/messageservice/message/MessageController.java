package com.pulsehub.messageservice.message;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(@Valid @RequestBody CreateMessageRequest request) {
        return MessageResponse.from(messageService.createMessage(request));
    }

    @GetMapping
    public List<MessageResponse> getMessages(@RequestParam(required = false) String channel) {
        return messageService.getMessages(channel).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MessageResponse getMessage(@PathVariable UUID id) {
        return MessageResponse.from(messageService.getMessage(id));
    }
}
