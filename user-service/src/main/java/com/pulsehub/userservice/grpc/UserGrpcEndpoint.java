package com.pulsehub.userservice.grpc;

import com.pulsehub.grpc.user.GetUserByIdRequest;
import com.pulsehub.grpc.user.UserGrpcServiceGrpc;
import com.pulsehub.grpc.user.UserResponse;
import com.pulsehub.userservice.user.User;
import com.pulsehub.userservice.user.UserNotFoundException;
import com.pulsehub.userservice.user.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserGrpcEndpoint extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private final UserService userService;

    public UserGrpcEndpoint(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        UUID userId;
        try {
            userId = UUID.fromString(request.getUserId());
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid userId: " + request.getUserId())
                    .asRuntimeException());
            return;
        }

        try {
            User user = userService.getUser(userId);
            responseObserver.onNext(UserResponse.newBuilder()
                    .setUserId(user.getId().toString())
                    .setUsername(user.getUsername())
                    .setDisplayName(user.getDisplayName())
                    .build());
            responseObserver.onCompleted();
        } catch (UserNotFoundException exception) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(exception.getMessage())
                    .asRuntimeException());
        }
    }
}
