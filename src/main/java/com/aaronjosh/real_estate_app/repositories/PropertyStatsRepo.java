package com.aaronjosh.real_estate_app.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.PropertyStats;

@Repository
public interface PropertyStatsRepo extends JpaRepository<PropertyStats, UUID> {

    public PropertyStats findByProperty_id(UUID propertyId);

}
