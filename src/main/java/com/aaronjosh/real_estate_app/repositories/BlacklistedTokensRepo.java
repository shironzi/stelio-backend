package com.aaronjosh.real_estate_app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.BlacklistedTokens;

@Repository
public interface BlacklistedTokensRepo extends JpaRepository<BlacklistedTokens, UUID> {
    Optional<BlacklistedTokens> findByToken(String token);
}
