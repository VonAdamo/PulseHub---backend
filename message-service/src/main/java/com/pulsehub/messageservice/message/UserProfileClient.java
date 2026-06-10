package com.pulsehub.messageservice.message;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileClient {

    Optional<UserProfile> findUser(UUID userId);
}
