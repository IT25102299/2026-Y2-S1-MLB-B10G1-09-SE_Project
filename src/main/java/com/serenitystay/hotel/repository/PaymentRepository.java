package com.serenitystay.hotel.repository;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByReservation(Reservation reservation);

    List<Payment> findByReservationAndStatus(
            Reservation reservation,
            Payment.PaymentStatus status
    );

    List<Payment> findByStatus(Payment.PaymentStatus status);

    List<Payment> findByPaymentDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}