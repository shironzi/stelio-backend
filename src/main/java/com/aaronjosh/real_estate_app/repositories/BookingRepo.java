package com.aaronjosh.real_estate_app.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.aaronjosh.real_estate_app.dto.stats.PropertyResPerformanceDto;
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

  @Query("""
          SELECT COALESCE(SUM(b.price), 0)
          FROM BookingEntity b
          JOIN b.property p
          WHERE b.status = 'COMPLETED'
          AND p.host.id = :userId
      """)
  BigDecimal getTotalRevenue(@Param("userId") UUID userId);

  @Query("""
          SELECT COALESCE(SUM(b.price), 0)
          FROM BookingEntity b
          JOIN b.property p
          WHERE b.status = 'COMPLETED'
          AND p.host.id = :userId
          AND (b.startDateTime <= :end AND b.endDateTime >= :start)
      """)
  BigDecimal getMonthlyRevenue(@Param("userId") UUID userId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("""
          SELECT COALESCE(SUM(b.totalNights), 0)
          FROM BookingEntity b
          JOIN b.property p
          WHERE b.status = 'COMPLETED'
          AND p.host.id = :userId
          AND(b.startDateTime <= :end AND b.endDateTime >= :start)
      """)
  Integer getMonthlyTotalBooked(@Param("userId") UUID userId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("""
          SELECT COUNT(b)
          FROM BookingEntity b
          JOIN b.property p
          WHERE b.status = 'CONFIRMED'
          AND p.host.id = :userId
          AND (b.startDateTime <= :end AND b.endDateTime >= :start)
      """)
  Integer getActiveBookings(@Param("userId") UUID userId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("""
          SELECT COUNT(b)
          FROM BookingEntity b
          JOIN b.property p
          WHERE b.status = 'CONFIRMED'
          AND p.host.id = :userId
          AND (b.startDateTime <= :end AND b.endDateTime >= :start)
      """)
  Integer getTodaysCheckins(@Param("userId") UUID userId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("""
          SELECT p.title, p.address,
                 COUNT(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE NULL END) AS completedBookings,
                 COALESCE(SUM(CASE WHEN b.status = 'COMPLETED' THEN b.price ELSE 0 END), 0) AS totalRevenue,
                 COALESCE(SUM(CASE WHEN b.status = 'COMPLETED' THEN b.totalNights ELSE 0 END), 0) AS totalNights,
                 p.createdAt
          FROM PropertyEntity p
          LEFT JOIN p.bookings b
          WHERE p.host.id = :userId
          GROUP BY p.id
          ORDER BY totalRevenue DESC
          LIMIT 3
      """)
  List<Object[]> getTopRevenueProperties(@Param("userId") UUID userId);
}
