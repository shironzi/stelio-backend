package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyStatus;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID> {
    List<PropertyEntity> findByHostId(UUID userId);

    List<PropertyEntity> findByStatus(PropertyStatus status);
}
