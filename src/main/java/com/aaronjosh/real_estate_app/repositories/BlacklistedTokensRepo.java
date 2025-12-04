package com.aaronjosh.real_estate_app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aaronjosh.real_estate_app.models.BlacklistedTokens;

public interface BlacklistedTokensRepo extends JpaRepository<BlacklistedTokens, UUID> {
    Optional<BlacklistedTokens> findByToken(String token);
}
