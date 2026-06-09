package com.pulsehub.bffservice.message;

import com.pulsehub.bffservice.message.dto.MessageCreateRequest;
import com.pulsehub.bffservice.message.dto.MessageResponse;
import com.pulsehub.bffservice.message.dto.MessageServiceCreateRequest;
import com.pulsehub.bffservice.security.JwtClaims;
import jakarta.servlet.http.HttpServletRequest;
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

@RestController
@RequestMapping("/api/messages")
public class MessageBffController {

    private final MessageServiceClient messageServiceClient;

    public MessageBffController(MessageServiceClient messageServiceClient) {
        this.messageServiceClient = messageServiceClient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(
            @Valid @RequestBody MessageCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        JwtClaims claims = (JwtClaims) httpServletRequest.getAttribute("jwtClaims");

        MessageServiceCreateRequest serviceRequest = new MessageServiceCreateRequest(
                claims.userId(),
                claims.username(),
                request.channel(),
                request.content()
        );

        return messageServiceClient.createMessage(serviceRequest);
    }

    @GetMapping
    public List<MessageResponse> getMessages(@RequestParam(required = false) String channel) {
        return messageServiceClient.getMessages(channel);
    }

    @GetMapping("/{id}")
    public MessageResponse getMessageById(@PathVariable String id) {
        return messageServiceClient.getMessageById(id);
    }
}
