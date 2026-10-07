package com.serenitystay.hotel.report;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Strategy: CSV sales report (summary lines followed by every payment). */
@Component
@Order(3)
public class CsvReportGenerator implements ReportGenerator {

    @Override
    public String format() {
        return "csv";
    }

    @Override
    public String contentType() {
        return "text/csv";
    }

    @Override
    public String fileExtension() {
        return "csv";
    }

    @Override
    public byte[] generate(SalesReportData d) {
        StringBuilder csv = new StringBuilder("\uFEFF");   // BOM so Excel reads UTF-8

        csv.append("SERENITY STAY HOTELS - Sales & Performance Report\n");
        row(csv, "Report Period", d.startDate() + " to " + d.endDate());
        csv.append('\n');

        csv.append("SUMMARY\n");
        row(csv, "Total Income", SalesReportData.money(d.totalIncome()));
        row(csv, "Total Refunds", SalesReportData.money(d.totalRefunds()));
        row(csv, "Net Income", SalesReportData.money(d.netIncome()));
        row(csv, "Total Bookings", String.valueOf(d.totalBookings()));
        row(csv, "Rooms Booked", String.valueOf(d.roomsBooked()));
        row(csv, "Total Room Nights", String.valueOf(d.totalRoomNights()));
        row(csv, "Verified Payments", String.valueOf(d.verifiedPayments().size()));
        row(csv, "Total Transactions", String.valueOf(d.allPayments().size()));
        csv.append('\n');

        csv.append("PAYMENT METHODS\n");
        row(csv, "Cash", SalesReportData.money(d.cashTotal()));
        row(csv, "Card", SalesReportData.money(d.cardTotal()));
        row(csv, "Bank Transfer", SalesReportData.money(d.bankTransferTotal()));
        csv.append('\n');

        csv.append("DETAILED SALES\n");
        row(csv, "Date", "Reservation", "Guest", "Room", "Payment Method", "Amount", "Status");
        for (Payment p : d.allPayments()) {
            Reservation r = p.getReservation();
            row(csv,
                    p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate().toString() : "-",
                    r != null && r.getId() != null ? String.valueOf(r.getId()) : "-",
                    r != null && r.getGuest() != null ? r.getGuest().getFullName() : "-",
                    r != null && r.getRoom() != null ? r.getRoom().getRoomNumber() : "-",
                    p.getPaymentMethod() != null ? p.getPaymentMethod().toString() : "-",
                    SalesReportData.money(p.getAmount()),
                    String.valueOf(p.getStatus()));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void row(StringBuilder sb, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(cells[i]));
        }
        sb.append('\n');
    }

    /** Quotes a cell when it contains a comma, quote or line break. */
    private String escape(String value) {
        if (value == null) {
            return "-";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

