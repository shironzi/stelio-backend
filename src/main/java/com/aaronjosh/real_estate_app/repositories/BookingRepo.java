package com.aaronjosh.real_estate_app.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingRepo extends JpaRepository<BookingEntity, UUID> {
  public List<BookingEntity> findByUser_id(UUID id);

  public List<BookingEntity> findAllByPropertyId(UUID id);

  boolean existsByUser_IdAndStatus(UUID userId, BookingStatus status);

  public List<BookingEntity> findByProperty_Host_Id(UUID hostId);

  @Query("""
          SELECT b FROM BookingEntity b
          WHERE b.property.id = :propertyId
            AND b.startDateTime <= :requestEnd
            AND b.endDateTime >= :requestStart
            AND b.status = :status
      """)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<BookingEntity> findOverlappingBookingsForUpdate(
      @Param("propertyId") UUID propertyId,
      @Param("requestEnd") LocalDateTime requestEnd,
      @Param("requestStart") LocalDateTime requestStart,
      @Param("status") BookingStatus status);

  @Query(" SELECT b\n" + //
      "    FROM BookingEntity b\n" + //
      "    WHERE b.property.id = :propertyId AND b.status IN :statuses AND b.expiresAt > :now \n")
  Optional<BookingEntity> findByStatusInAndExpiresAtBefore(@Param("statuses") List<BookingStatus> statuses,
      @Param("now") LocalDateTime now, @Param("propertyId") UUID propertyId);
}
