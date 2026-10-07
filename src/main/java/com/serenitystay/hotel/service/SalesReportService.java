package com.serenitystay.hotel.service;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;
import com.serenitystay.hotel.report.GeneratedReport;
import com.serenitystay.hotel.report.ReportGenerator;
import com.serenitystay.hotel.report.SalesReportData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DESIGN PATTERN: Strategy (Behavioral) - Managerial Dashboard & Analytics.
 *
 * This class is the CONTEXT. It calculates the report figures once
 * ({@link SalesReportData}) and then hands them to the {@link ReportGenerator}
 * strategy chosen by the requested format ("docx", "xlsx" or "csv").
 * Adding a new format = adding one new ReportGenerator class; this service
 * does not change.
 */
@Service
public class SalesReportService {

    private static final String DEFAULT_FORMAT = "docx";

    private final PaymentService paymentService;
    private final Map<String, ReportGenerator> generators = new LinkedHashMap<>();

    /** Spring injects every ReportGenerator bean (in @Order sequence). */
    public SalesReportService(PaymentService paymentService, List<ReportGenerator> generatorBeans) {
        this.paymentService = paymentService;
        for (ReportGenerator generator : generatorBeans) {
            generators.put(generator.format().toLowerCase(), generator);
        }
    }

    /** Backward compatible: the original Word report. */
    public byte[] generateSalesReport(LocalDate startDate, LocalDate endDate) throws IOException {
        return generate(startDate, endDate, DEFAULT_FORMAT).content();
    }

    /** Builds the report in the requested format. */
    public GeneratedReport generate(LocalDate startDate, LocalDate endDate, String format) throws IOException {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Please choose a valid report period.");
        }

        String key = format == null ? DEFAULT_FORMAT : format.toLowerCase();
        ReportGenerator generator = generators.get(key);
        if (generator == null) {
            throw new IllegalArgumentException(
                    "Unsupported report format '" + format + "'. Supported formats: " + generators.keySet());
        }

        SalesReportData data = buildData(startDate, endDate);
        byte[] content = generator.generate(data);
        String fileName = "Serenity_Stay_Sales_Report_" + startDate + "_to_" + endDate
                + "." + generator.fileExtension();
        return new GeneratedReport(content, generator.contentType(), fileName);
    }

    // =========================================================
    // Shared calculations (the same for every format)
    // =========================================================
    private SalesReportData buildData(LocalDate startDate, LocalDate endDate) {

        List<Payment> allPayments = paymentService.findPaymentsBetween(startDate, endDate);

        // Only VERIFIED payments are treated as income.
        List<Payment> verifiedPayments = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.VERIFIED)
                .toList();

        List<Payment> refundedPayments = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.REFUNDED)
                .toList();

        BigDecimal totalIncome = sum(verifiedPayments);
        BigDecimal totalRefunds = refundedPayments.stream()
                .map(Payment::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netIncome = totalIncome.subtract(totalRefunds);

        // Unique reservations connected to the payments
        Map<Long, Reservation> reservationMap = new LinkedHashMap<>();
        for (Payment payment : allPayments) {
            if (payment.getReservation() != null && payment.getReservation().getId() != null) {
                reservationMap.put(payment.getReservation().getId(), payment.getReservation());
            }
        }
        List<Reservation> reservations = new ArrayList<>(reservationMap.values());

        long totalBookings = reservations.size();
        long roomsBooked = reservations.stream()
                .map(Reservation::getRoom)
                .filter(Objects::nonNull)
                .map(room -> room.getId())
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long totalRoomNights = reservations.stream()
                .mapToLong(Reservation::getNumberOfNights)
                .sum();

        return new SalesReportData(
                startDate, endDate,
                allPayments, verifiedPayments, refundedPayments, reservations,
                totalIncome, totalRefunds, netIncome,
                totalBookings, roomsBooked, totalRoomNights,
                sumByMethod(verifiedPayments, Payment.PaymentMethod.CASH),
                sumByMethod(verifiedPayments, Payment.PaymentMethod.CARD),
                sumByMethod(verifiedPayments, Payment.PaymentMethod.BANK_TRANSFER));
    }

    private BigDecimal sum(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByMethod(List<Payment> payments, Payment.PaymentMethod method) {
        return sum(payments.stream().filter(p -> p.getPaymentMethod() == method).toList());
    }
}
