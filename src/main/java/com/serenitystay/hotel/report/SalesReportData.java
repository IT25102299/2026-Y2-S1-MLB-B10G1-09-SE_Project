package com.serenitystay.hotel.report;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * All figures for one sales report, calculated once by SalesReportService and
 * shared by every {@link ReportGenerator} strategy.
 */
public record SalesReportData(
        LocalDate startDate,
        LocalDate endDate,
        List<Payment> allPayments,
        List<Payment> verifiedPayments,
        List<Payment> refundedPayments,
        List<Reservation> reservations,
        BigDecimal totalIncome,
        BigDecimal totalRefunds,
        BigDecimal netIncome,
        long totalBookings,
        long roomsBooked,
        long totalRoomNights,
        BigDecimal cashTotal,
        BigDecimal cardTotal,
        BigDecimal bankTransferTotal) {

    /** Money with two decimals; null counts as zero. */
    public static String money(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
