package com.pulsehub.bffservice.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthBffController {

    private final AuthServiceClient authServiceClient;
    private final AuthRegistrationService authRegistrationService;

    public AuthBffController(AuthServiceClient authServiceClient, AuthRegistrationService authRegistrationService) {
        this.authServiceClient = authServiceClient;
        this.authRegistrationService = authRegistrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authRegistrationService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authServiceClient.login(request);
    }
}
