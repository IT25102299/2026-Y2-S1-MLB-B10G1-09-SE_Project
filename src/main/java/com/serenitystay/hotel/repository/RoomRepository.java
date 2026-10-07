package com.serenitystay.hotel.repository;

import com.serenitystay.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByStatus(Room.RoomStatus status);

    List<Room> findByStatusIn(List<Room.RoomStatus> statuses);

    boolean existsByRoomNumber(String roomNumber);

    // Rooms a given housekeeping staff member is assigned to.
    List<Room> findByAssignedStaffId(Long staffId);

    // Used to block deleting a room type that rooms still use.
    boolean existsByRoomTypeLookupId(Long roomTypeLookupId);

    // ==========================================================
    // NORMAL ROOM AVAILABILITY
    // ==========================================================
    //
    // Price filtering is done in RoomServiceImpl on the real nightly
    // rate (base x multiplier + fee), not on the base price here.
    //
    @Query("""
        SELECT r FROM Room r
        WHERE r.status <> 'MAINTENANCE'
          AND r.id NOT IN (
              SELECT res.room.id FROM Reservation res
              WHERE res.status NOT IN ('CANCELLED', 'CHECKED_OUT')
                AND res.checkInDate < :checkOutDate
                AND res.checkOutDate > :checkInDate
          )
          AND (:roomType IS NULL OR TYPE(r) = :roomType)
        """)
    List<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomType") Class<? extends Room> roomType
    );

    // ==========================================================
    // EXTENSION AVAILABILITY
    // ==========================================================
    //
    // Checks rooms for the extension period.
    //
    // IMPORTANT:
    // The current reservation is excluded from the overlap check.
    // Therefore the guest's current room can be returned if nobody
    // else has booked it for the extension period.
    //

    @Query("""
        SELECT r FROM Room r
        WHERE r.status <> 'MAINTENANCE'
          AND r.id NOT IN (
              SELECT res.room.id
              FROM Reservation res
              WHERE res.id <> :reservationId
                AND res.status NOT IN ('CANCELLED', 'CHECKED_OUT')
                AND res.checkInDate < :newCheckOutDate
                AND res.checkOutDate > :currentCheckOutDate
          )
        """)
    List<Room> findAvailableRoomsForExtension(
            @Param("reservationId") Long reservationId,
            @Param("currentCheckOutDate") LocalDate currentCheckOutDate,
            @Param("newCheckOutDate") LocalDate newCheckOutDate
    );
}
