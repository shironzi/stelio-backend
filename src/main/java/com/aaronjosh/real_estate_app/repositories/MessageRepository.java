package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.MessageEntity;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    @Query("SELECT m FROM MessageEntity m " +
            "WHERE m.conversation.id = :conversationId")
    List<MessageEntity> findAllMessagesByConversationId(@Param("conversationId") UUID conversationId);

    @Query("INSERT INTO MessageEntity () VALUES")
    public void sendMessage(@Param("conversationId") UUID conversationId);
}
