package com.aaronjosh.real_estate_app.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

@Repository
public interface BookingRepo extends JpaRepository<BookingEntity, UUID> {
  @EntityGraph(attributePaths = { "property", "user" })
  public List<BookingEntity> findByUser_id(UUID id, Sort sort);

  public List<BookingEntity> findAllByPropertyId(UUID id);

  boolean existsByUser_IdAndStatus(UUID userId, BookingStatus status);

  @EntityGraph(attributePaths = { "property" })
  public List<BookingEntity> findByProperty_Host_Id(UUID hostId);

  @EntityGraph(attributePaths = { "property" })
  @Query("""
          SELECT b FROM BookingEntity b
          WHERE b.property.id = :propertyId
            AND b.startDateTime < :requestEnd
            AND b.endDateTime > :requestStart
            AND (
                  b.status = 'CONFIRMED'
                  OR (b.status IN ('PENDING_PAYMENT', 'PENDING_APPROVAL') AND b.expiresAt > CURRENT_TIMESTAMP)
                )
      """)
  List<BookingEntity> findOverlappingBookingsForUpdate(
      @Param("propertyId") UUID propertyId,
      @Param("requestStart") LocalDateTime requestStart,
      @Param("requestEnd") LocalDateTime requestEnd);

  Optional<BookingEntity> findByStripePaymentIntentId(String id);
}
