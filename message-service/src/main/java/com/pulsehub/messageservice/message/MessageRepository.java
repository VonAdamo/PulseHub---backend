package com.pulsehub.messageservice.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByOrderByCreatedAtAsc();

    List<Message> findByChannelOrderByCreatedAtAsc(String channel);

    long countBySenderId(UUID senderId);
}
