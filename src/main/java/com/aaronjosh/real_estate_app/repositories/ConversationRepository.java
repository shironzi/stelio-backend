package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.ConversationEntity;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {
    @Query("SELECT DISTINCT c " +
            "FROM ConversationEntity c " +
            "JOIN c.participants p " +
            "WHERE p.whoJoined.id = :userId")
    List<ConversationEntity> findConversationsByParticipantId(@Param("userId") UUID userId);
}
