package com.aaronjosh.real_estate_app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.ParticipantEntity;

@Repository
public interface ParticipantRepo extends JpaRepository<ParticipantEntity, UUID> {
    Optional<ParticipantEntity> findByConversationIdAndWhoJoinedId(UUID conversationId, UUID userId);
}