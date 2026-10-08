package com.serenitystay.hotel.service;

import com.serenitystay.hotel.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    List<Reservation> findAll();

    Reservation findById(Long id);

    Reservation createReservation(Reservation reservation);

    Reservation update(Long id, Reservation updated);

    void cancel(Long id);

    void deleteById(Long id);

    List<Reservation> findByGuestId(Long guestId);

    List<Reservation> findPendingOrConfirmed();

    /** Pending / confirmed reservations that pass the check-in rules today. */
    List<Reservation> findEligibleForCheckIn();

    Reservation checkIn(Long id);

    List<Reservation> findCheckedIn();

    Reservation checkOut(Long id);

    List<Reservation> searchReservations(String guestName, String roomNumber,
                                         LocalDate checkInFrom, LocalDate checkInTo,
                                         LocalDate checkOutFrom, LocalDate checkOutTo);
}