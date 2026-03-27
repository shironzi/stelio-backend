package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.ConversationEntity;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @EntityGraph(attributePaths = { "participants", "participants.whoJoined" })
    List<ConversationEntity> findByParticipantsWhoJoinedId(UUID userId);
}
