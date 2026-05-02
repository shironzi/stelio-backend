package com.aaronjosh.real_estate_app.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.dto.property.PropertyCardDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyResDto;
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

        @EntityGraph(attributePaths = { "images" })
        List<PropertyEntity> findTop10ByStatus(PropertyStatus status);

        @Query("""
                        SELECT COUNT(p) FROM PropertyEntity p
                        """)
        Integer findTotalProperties();

        @Query("""
                        SELECT new com.aaronjosh.real_estate_app.dto.property.PropertyCardDto(
                        p.id, p.title, p.city, p.address, p.price, p.propertyType, i.key
                        )
                        FROM PropertyEntity p
                        LEFT JOIN p.images i ON i.isPrimary = true
                        WHERE (:address IS NULL OR (LOWER(p.address) LIKE :address OR LOWER(p.city) LIKE :address))
                        AND (:minGuests IS NULL OR p.maxGuest >= :minGuests)
                        AND (:minPrice IS NULL OR p.price >= :minPrice)
                        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                        AND (CAST(:start AS localdatetime) IS NULL OR CAST(:end AS localdatetime) IS NULL
                            OR NOT EXISTS (
                                SELECT 1 FROM BookingEntity b
                                WHERE b.property = p
                                AND b.startDateTime < :end
                                AND b.endDateTime > :start
                        ))
                        """)
        Page<PropertyCardDto> fetchPropertyCards(Pageable pageable, @Param("address") String address,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                        @Param("minGuests") Integer minGuests, @Param("maxPrice") BigDecimal maxPrice,
                        @Param("minPrice") BigDecimal minPrice);

        @Query("""
                        SELECT new com.aaronjosh.real_estate_app.dto.property.PropertyCardDto(
                        p.id, p.title, p.city, p.address, p.price, p.propertyType, i.key
                        )
                        FROM PropertyEntity p
                        LEFT JOIN p.images i ON i.isPrimary = true
                        WHERE p.host.id = :userId
                        """)
        Page<PropertyCardDto> fetchPropertyCardsByOwner(Pageable pageable, @Param("userId") UUID userId);

        @Query("""
                            SELECT new com.aaronjosh.real_estate_app.dto.property.PropertyResDto(
                                    p.id,
                                    p.title,
                                    p.description,
                                    p.price,
                                    p.propertyType,
                                    p.maxGuest,
                                    p.totalBedroom,
                                    p.totalBed,
                                    p.totalBath,
                                    p.address,
                                    p.city,
                                    p.status,
                                    CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END,
                                    (
                                        SELECT new com.aaronjosh.real_estate_app.dto.property.ImageDto(
                                            i.id,
                                            i.key
                                        ) FROM FileEntity i
                                        WHERE i.propertyEntity = p
                                    )
                            )
                            FROM PropertyEntity p
                            LEFT JOIN FavoriteEntity f ON f.property = p
                            LEFT JOIN FileEntity i ON i.propertyEntity = p
                            WHERE p.id = :propertyId
                        """)
        Optional<PropertyResDto> fetchPropertyById(@Param("propertyId") UUID propertyId);

}
