package com.pulsehub.authservice.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

    boolean existsByUsername(String username);

    Optional<AuthUser> findByUsername(String username);
}
