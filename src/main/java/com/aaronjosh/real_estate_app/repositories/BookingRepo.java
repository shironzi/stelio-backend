package com.aaronjosh.real_estate_app.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

@Repository
public interface BookingRepo extends JpaRepository<BookingEntity, UUID> {
        public List<BookingEntity> findByUser_id(UUID id);

        boolean existsByUser_IdAndStatus(UUID userId, BookingStatus status);

        public List<BookingEntity> findByProperty_Host_Id(UUID hostId);

        @Query("""
                            SELECT b FROM BookingEntity b
                            WHERE b.property.id = :propertyId
                              AND b.startDateTime <= :requestEnd
                              AND b.endDateTime >= :requestStart
                              AND b.status NOT IN ('APPROVED', 'CANCELED')
                        """)
        List<BookingEntity> findOverlappingBookings(
                        @Param("propertyId") UUID propertyId,
                        @Param("requestEnd") LocalDateTime requestEnd,
                        @Param("requestStart") LocalDateTime requestStart);
}
