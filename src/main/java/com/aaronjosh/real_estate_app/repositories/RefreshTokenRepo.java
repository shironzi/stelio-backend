package com.aaronjosh.real_estate_app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.RefreshTokensEntity;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshTokensEntity, UUID> {
    Optional<RefreshTokensEntity> findByRefreshToken(String refreshToken);
}
