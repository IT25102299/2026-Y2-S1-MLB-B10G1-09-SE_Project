package com.serenitystay.hotel.service.impl;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;
import com.serenitystay.hotel.model.Room;
import com.serenitystay.hotel.repository.PaymentRepository;
import com.serenitystay.hotel.repository.ReservationRepository;
import com.serenitystay.hotel.repository.RoomRepository;
import com.serenitystay.hotel.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ReservationRepository reservationRepository,
                              RoomRepository roomRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment createPayment(Reservation reservation, String slipReference) {
        // Check if payment already exists for this reservation
        List<Payment> existingPayments = paymentRepository.findByReservation(reservation);
        if (!existingPayments.isEmpty()) {
            throw new IllegalStateException("Payment already exists for this reservation.");
        }

        // Amount is the total cost of the reservation
        BigDecimal amount = reservation.getTotalCost();
        Payment payment = new Payment(reservation, amount, slipReference);
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    public Payment verifyPayment(Long paymentId, String verifiedBy) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is already " + payment.getStatus());
        }
        payment.setStatus(Payment.PaymentStatus.VERIFIED);
        payment.setVerifiedBy(verifiedBy);

        // Only a PENDING reservation becomes CONFIRMED. A cancelled or
        // already checked-in reservation must keep its status.
        Reservation reservation = payment.getReservation();
        if (reservation.getStatus() == Reservation.ReservationStatus.PENDING) {
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
        }
        return paymentRepository.save(payment);
    }

    @Override
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        if (payment.getStatus() != Payment.PaymentStatus.VERIFIED) {
            throw new IllegalStateException("Only verified payments can be refunded.");
        }
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundDate(LocalDateTime.now());

        // Cancel the associated reservation.
        Reservation reservation = payment.getReservation();

        // Only touch the room if the guest was actually in it. Previously the
        // room was always set to READY, even if another guest was staying
        // there or it was under maintenance.
        if (reservation.getStatus() == Reservation.ReservationStatus.CHECKED_IN) {
            Room room = reservation.getRoom();
            room.setStatus(Room.RoomStatus.CLEANING);
            roomRepository.save(room);
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findByReservation(Reservation reservation) {
        List<Payment> payments = paymentRepository.findByReservation(reservation);
        // Return the first payment if exists, otherwise null
        return payments.isEmpty() ? null : payments.get(0);
    }

    @Override
    public List<Payment> findPendingPayments() {
        return paymentRepository.findByStatus(Payment.PaymentStatus.PENDING);
    }

    @Override
    public List<Payment> findPaymentsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return paymentRepository.findByPaymentDateBetween(start, end);
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public List<Payment> findPaymentsBetween(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }
}
