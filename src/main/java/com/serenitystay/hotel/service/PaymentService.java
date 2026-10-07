package com.serenitystay.hotel.service;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    Payment createPayment(Reservation reservation, String slipReference);

    Payment verifyPayment(Long paymentId, String verifiedBy);

    Payment refundPayment(Long paymentId);

    Payment findByReservation(Reservation reservation);

    List<Payment> findPendingPayments();

    List<Payment> findPaymentsForDate(LocalDate date);

    List<Payment> findAll();

    List<Payment> findPaymentsBetween(LocalDate startDate, LocalDate endDate);

    // New method to save a payment (used by guest booking)
    Payment save(Payment payment);
}