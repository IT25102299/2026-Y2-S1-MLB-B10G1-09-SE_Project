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

    List<Reservation> findByStatusIn(
            List<Reservation.ReservationStatus> statuses
    );

    // =========================================================
    // CHECK ROOM OVERLAPPING RESERVATIONS
    // =========================================================

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.room = :room
              AND r.status NOT IN :excludedStatuses
              AND r.checkInDate < :checkOutDate
              AND r.checkOutDate > :checkInDate
            """)
    List<Reservation> findOverlappingReservations(
            @Param("room") Room room,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludedStatuses") List<Reservation.ReservationStatus> excludedStatuses
    );

    // Used to block deleting a room that still has reservations.
    boolean existsByRoomId(Long roomId);

    // =========================================================
    // COUNT OCCUPIED ROOMS FOR DASHBOARD
    // =========================================================

    @Query("""
            SELECT COUNT(DISTINCT r.room.id) FROM Reservation r
            WHERE r.status IN :statuses
              AND r.checkInDate <= :today
              AND r.checkOutDate > :today
            """)
    long countOccupiedRoomsForDate(
            @Param("today") LocalDate today,
            @Param("statuses")
            List<Reservation.ReservationStatus> statuses
    );

    // =========================================================
    // GET BOOKINGS FOR A PARTICULAR DATE
    // =========================================================

    @Query("""
        SELECT DISTINCT r
        FROM Reservation r
        JOIN FETCH r.room
        WHERE r.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
          AND r.checkInDate <= :date
          AND r.checkOutDate > :date
        """)
    List<Reservation> findBookedReservationsForDate(
            @Param("date") LocalDate date
    );

    // =========================================================
    // UPCOMING ROOM BOOKINGS
    // =========================================================

    @Query("""
            SELECT DISTINCT r
            FROM Reservation r
            JOIN FETCH r.room
            WHERE r.status IN ('CONFIRMED', 'CHECKED_IN')
              AND r.checkInDate < :endDate
              AND r.checkOutDate > :startDate
            ORDER BY r.checkInDate, r.room.roomNumber
            """)
    List<Reservation> findUpcomingRoomBookings(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // =========================================================
    // FIND AVAILABLE ROOMS FOR NEW RESERVATION
    // =========================================================

    @Query("""
            SELECT room
            FROM Room room
            WHERE NOT EXISTS (
                SELECT r
                FROM Reservation r
                WHERE r.room = room
                  AND r.status <> :excludedStatus
                  AND r.checkInDate < :checkOutDate
                  AND r.checkOutDate > :checkInDate
            )
            ORDER BY room.roomNumber
            """)
    List<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludedStatus")
            Reservation.ReservationStatus excludedStatus
    );

    // =========================================================
    // FIND AVAILABLE ROOMS WHEN EDITING A RESERVATION
    // =========================================================
    //
    // The current reservation is excluded from the conflict
    // check. This is important because otherwise the room that
    // the guest already has would appear unavailable to itself.
    //
    // Other PENDING, CONFIRMED and CHECKED_IN reservations still
    // block the room.
    //
    // CANCELLED reservations do not block the room.
    // =========================================================

    @Query("""
            SELECT room
            FROM Room room
            WHERE NOT EXISTS (
                SELECT r
                FROM Reservation r
                WHERE r.room = room
                  AND r.id <> :reservationId
                  AND r.status <> :excludedStatus
                  AND r.checkInDate < :checkOutDate
                  AND r.checkOutDate > :checkInDate
            )
            ORDER BY room.roomNumber
            """)
    List<Room> findAvailableRoomsForEdit(
            @Param("reservationId") Long reservationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludedStatus")
            Reservation.ReservationStatus excludedStatus
    );

    // =========================================================
    // SEARCH RESERVATIONS
    // =========================================================

    @Query("""
            SELECT r FROM Reservation r
            WHERE (:guestName IS NULL OR LOWER(r.guest.fullName)
                    LIKE LOWER(CONCAT('%', :guestName, '%')))
              AND (:roomNumber IS NULL OR r.room.roomNumber = :roomNumber)
              AND (:checkInFrom IS NULL OR r.checkInDate >= :checkInFrom)
              AND (:checkInTo IS NULL OR r.checkInDate <= :checkInTo)
              AND (:checkOutFrom IS NULL OR r.checkOutDate >= :checkOutFrom)
              AND (:checkOutTo IS NULL OR r.checkOutDate <= :checkOutTo)
            """)
    List<Reservation> searchReservations(
            @Param("guestName") String guestName,
            @Param("roomNumber") String roomNumber,
            @Param("checkInFrom") LocalDate checkInFrom,
            @Param("checkInTo") LocalDate checkInTo,
            @Param("checkOutFrom") LocalDate checkOutFrom,
            @Param("checkOutTo") LocalDate checkOutTo
    );
}
