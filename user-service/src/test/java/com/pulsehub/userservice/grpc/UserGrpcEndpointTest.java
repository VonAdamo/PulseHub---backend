package com.pulsehub.userservice.grpc;

import com.pulsehub.grpc.user.GetUserByIdRequest;
import com.pulsehub.grpc.user.UserResponse;
import com.pulsehub.userservice.user.User;
import com.pulsehub.userservice.user.UserNotFoundException;
import com.pulsehub.userservice.user.UserService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserGrpcEndpointTest {

    private final UserService userService = mock(UserService.class);
    private final UserGrpcEndpoint userGrpcEndpoint = new UserGrpcEndpoint(userService);

    @Test
    void getUserByIdReturnsUserProfile() {
        UUID userId = UUID.randomUUID();
        User user = new User("milla", "Milla");
        ReflectionTestUtils.setField(user, "id", userId);
        StreamObserver<UserResponse> responseObserver = mock(StreamObserver.class);

        when(userService.getUser(userId)).thenReturn(user);

        userGrpcEndpoint.getUserById(GetUserByIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build(), responseObserver);

        ArgumentCaptor<UserResponse> responseCaptor = ArgumentCaptor.forClass(UserResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        UserResponse response = responseCaptor.getValue();
        assertThat(response.getUserId()).isEqualTo(userId.toString());
        assertThat(response.getUsername()).isEqualTo("milla");
        assertThat(response.getDisplayName()).isEqualTo("Milla");
    }

    @Test
    void getUserByIdReturnsNotFoundWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        StreamObserver<UserResponse> responseObserver = mock(StreamObserver.class);

        when(userService.getUser(userId)).thenThrow(new UserNotFoundException(userId));

        userGrpcEndpoint.getUserById(GetUserByIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build(), responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException exception = (StatusRuntimeException) errorCaptor.getValue();
        assertThat(exception.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }
}
