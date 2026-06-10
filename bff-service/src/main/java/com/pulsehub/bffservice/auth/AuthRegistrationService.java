package com.pulsehub.bffservice.auth;

import com.pulsehub.bffservice.user.CreateUserRequest;
import com.pulsehub.bffservice.user.UserResponse;
import com.pulsehub.bffservice.user.UserServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class AuthRegistrationService {

    private final AuthServiceClient authServiceClient;
    private final UserServiceClient userServiceClient;

    public AuthRegistrationService(AuthServiceClient authServiceClient, UserServiceClient userServiceClient) {
        this.authServiceClient = authServiceClient;
        this.userServiceClient = userServiceClient;
    }

    public RegisterResponse register(RegisterRequest request) {
        AuthResponse authResponse = authServiceClient.register(request);

        try {
            UserResponse userResponse = userServiceClient.createUser(new CreateUserRequest(
                    authResponse.userId(),
                    authResponse.username(),
                    request.displayName()
            ));

            return new RegisterResponse(
                    authResponse.userId(),
                    authResponse.username(),
                    userResponse.displayName()
            );
        } catch (RestClientException exception) {
            throw new RegistrationProfileCreationException(authResponse.userId(), authResponse.username(), exception);
        }
    }
}
