package com.pulsehub.bffservice.me;

import com.pulsehub.bffservice.message.MessageServiceClient;
import com.pulsehub.bffservice.security.JwtClaims;
import com.pulsehub.bffservice.user.UserResponse;
import com.pulsehub.bffservice.user.UserServiceClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MeService {

    private final UserServiceClient userServiceClient;
    private final MessageServiceClient messageServiceClient;

    public MeService(UserServiceClient userServiceClient, MessageServiceClient messageServiceClient) {
        this.userServiceClient = userServiceClient;
        this.messageServiceClient = messageServiceClient;
    }

    public MeResponse getMe(JwtClaims claims) {
        UUID userId = UUID.fromString(claims.userId());
        UserResponse user = userServiceClient.getUser(userId);
        long sentMessagesCount = messageServiceClient.getSentMessagesCount(userId);

        return new MeResponse(
                user.id(),
                user.username(),
                user.displayName(),
                user.createdAt(),
                sentMessagesCount
        );
    }
}
