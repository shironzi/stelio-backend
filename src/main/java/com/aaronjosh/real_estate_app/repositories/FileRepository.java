package com.aaronjosh.real_estate_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.dto.property.PropertyImageDto;
import com.aaronjosh.real_estate_app.models.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    @Query("""
                SELECT new com.aaronjosh.real_estate_app.dto.property.PropertyImageDto(f.id, f.key)
                FROM FileEntity f
                WHERE f.propertyEntity.id = :propertyId
            """)
    List<PropertyImageDto> fetchImagesByPropertyId(@Param("propertyId") UUID propertyId);
}
