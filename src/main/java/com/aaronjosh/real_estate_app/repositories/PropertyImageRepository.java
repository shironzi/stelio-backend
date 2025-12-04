package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.PropertyImageEntity;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImageEntity, UUID> {
    List<PropertyImageEntity> findAllByPropertyEntity_id(UUID id);
}
