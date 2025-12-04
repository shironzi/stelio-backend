package com.aaronjosh.real_estate_app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.FavoriteEntity;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, UUID> {
    Optional<FavoriteEntity> findByProperty_IdAndUser_Id(UUID propertyId, UUID userId);
}
