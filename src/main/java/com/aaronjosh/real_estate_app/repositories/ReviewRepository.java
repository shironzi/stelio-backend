package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aaronjosh.real_estate_app.models.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    public List<ReviewEntity> findByProperty_id(UUID id);

}
