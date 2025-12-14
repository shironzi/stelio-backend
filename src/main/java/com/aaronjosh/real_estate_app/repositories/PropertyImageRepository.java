package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.FileEntity;

@Repository
public interface PropertyImageRepository extends JpaRepository<FileEntity, UUID> {
    List<FileEntity> findAllByPropertyEntity_id(UUID id);
}
