package com.aaronjosh.real_estate_app.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.IdempotencyEntity;

@Repository
public interface IdempotencyRepo extends JpaRepository<IdempotencyEntity, UUID> {
    Optional<IdempotencyEntity> findByIdempotencyKey(String key);

    List<IdempotencyEntity> deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
