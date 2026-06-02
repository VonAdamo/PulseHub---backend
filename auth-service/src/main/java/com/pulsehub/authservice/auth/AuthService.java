package com.pulsehub.authservice.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        AuthUser user = new AuthUser(
                UUID.randomUUID(),
                request.username(),
                passwordEncoder.encode(request.password())
        );

        return AuthResponse.registered(authUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return AuthResponse.authenticated(user, jwtService.generateToken(user));
    }
}
