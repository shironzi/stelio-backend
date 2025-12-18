package com.aaronjosh.real_estate_app.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "conversations")
public class ConversationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToOne(mappedBy = "conversation", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ParticipantEntity> participants = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MessageEntity> messages = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "review_id", nullable = true)
    private ReviewEntity review;

    public ConversationEntity() {
    }

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
