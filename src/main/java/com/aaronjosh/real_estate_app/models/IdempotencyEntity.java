package com.aaronjosh.real_estate_app.models;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "idempotency")
@Data
public class IdempotencyEntity {

    public enum IdempotencyStatus {
        COMPLETED,
        PENDING,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private transient Map<String, Object> responseMap;

    @Column(nullable = false)
    private IdempotencyStatus status = IdempotencyStatus.PENDING;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusHours(24);
    }
}
