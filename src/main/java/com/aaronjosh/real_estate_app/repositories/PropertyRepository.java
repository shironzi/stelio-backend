package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID> {
    @EntityGraph(attributePaths = { "images" })
    @NonNull
    Optional<PropertyEntity> findById(@NonNull UUID id);

    List<PropertyEntity> findByHostId(UUID userId);

    List<PropertyEntity> findByStatus(PropertyStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PropertyEntity p WHERE p.id = :id")
    Optional<PropertyEntity> findAndLockById(@Param("id") UUID id);
}
