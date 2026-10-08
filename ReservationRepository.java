package com.serenitystay.hotel.repository;

import com.serenitystay.hotel.model.Reservation;
import com.serenitystay.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByGuestId(Long guestId);

    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    List<Reservation> findByStatusIn(List<Reservation.ReservationStatus> statuses);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.room = :room
              AND r.status <> :excludedStatus
              AND r.checkInDate < :checkOutDate
              AND r.checkOutDate > :checkInDate
            """)
    List<Reservation> findOverlappingReservations(@Param("room") Room room,
                                                  @Param("checkInDate") LocalDate checkInDate,
                                                  @Param("checkOutDate") LocalDate checkOutDate,
                                                  @Param("excludedStatus") Reservation.ReservationStatus excludedStatus);

    @Query("""
            SELECT COUNT(DISTINCT r.room.id) FROM Reservation r
            WHERE r.status IN :statuses
              AND r.checkInDate <= :today
              AND r.checkOutDate > :today
            """)
    long countOccupiedRoomsForDate(@Param("today") LocalDate today,
                                   @Param("statuses") List<Reservation.ReservationStatus> statuses);

    @Query("""
            SELECT r FROM Reservation r
            WHERE (:guestName IS NULL OR LOWER(r.guest.fullName) LIKE LOWER(CONCAT('%', :guestName, '%')))
              AND (:roomNumber IS NULL OR r.room.roomNumber = :roomNumber)
              AND (:checkInFrom IS NULL OR r.checkInDate >= :checkInFrom)
              AND (:checkInTo IS NULL OR r.checkInDate <= :checkInTo)
              AND (:checkOutFrom IS NULL OR r.checkOutDate >= :checkOutFrom)
              AND (:checkOutTo IS NULL OR r.checkOutDate <= :checkOutTo)
            """)
    List<Reservation> searchReservations(@Param("guestName") String guestName,
                                         @Param("roomNumber") String roomNumber,
                                         @Param("checkInFrom") LocalDate checkInFrom,
                                         @Param("checkInTo") LocalDate checkInTo,
                                         @Param("checkOutFrom") LocalDate checkOutFrom,
                                         @Param("checkOutTo") LocalDate checkOutTo);
}