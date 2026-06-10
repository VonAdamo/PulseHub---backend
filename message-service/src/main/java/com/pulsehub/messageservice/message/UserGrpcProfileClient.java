package com.pulsehub.messageservice.message;

import com.pulsehub.grpc.user.GetUserByIdRequest;
import com.pulsehub.grpc.user.UserGrpcServiceGrpc;
import com.pulsehub.grpc.user.UserResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class UserGrpcProfileClient implements UserProfileClient {

    private final ManagedChannel channel;
    private final UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcService;

    public UserGrpcProfileClient(
            @Value("${pulsehub.grpc.user-service.host}") String host,
            @Value("${pulsehub.grpc.user-service.port}") int port
    ) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.userGrpcService = UserGrpcServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public Optional<UserProfile> findUser(UUID userId) {
        try {
            UserResponse response = userGrpcService
                    .withDeadlineAfter(2, TimeUnit.SECONDS)
                    .getUserById(GetUserByIdRequest.newBuilder()
                            .setUserId(userId.toString())
                            .build());

            return Optional.of(new UserProfile(
                    UUID.fromString(response.getUserId()),
                    response.getUsername(),
                    response.getDisplayName()
            ));
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return Optional.empty();
            }

            throw new UserProfileLookupException("Could not look up sender in user-service", exception);
        }
    }

    @PreDestroy
    void shutdown() {
        channel.shutdown();
    }
}
