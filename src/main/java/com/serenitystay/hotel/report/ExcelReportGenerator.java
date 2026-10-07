package com.serenitystay.hotel.report;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Strategy: Excel (.xlsx) sales report with a sheet per section. */
@Component
@Order(2)
public class ExcelReportGenerator implements ReportGenerator {

    @Override
    public String format() {
        return "xlsx";
    }

    @Override
    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String fileExtension() {
        return "xlsx";
    }

    @Override
    public byte[] generate(SalesReportData d) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Font bold = wb.createFont();
            bold.setBold(true);
            CellStyle header = wb.createCellStyle();
            header.setFont(bold);

            // ---- Summary ----
            Sheet summary = wb.createSheet("Summary");
            line(summary, 0, header, "SERENITY STAY HOTELS - Sales & Performance Report");
            line(summary, 1, null, "Report Period", d.startDate() + " to " + d.endDate());
            line(summary, 3, header, "Summary");
            line(summary, 4, null, "Total Income", SalesReportData.money(d.totalIncome()));
            line(summary, 5, null, "Total Refunds", SalesReportData.money(d.totalRefunds()));
            line(summary, 6, null, "Net Income", SalesReportData.money(d.netIncome()));
            line(summary, 7, null, "Total Bookings", String.valueOf(d.totalBookings()));
            line(summary, 8, null, "Rooms Booked", String.valueOf(d.roomsBooked()));
            line(summary, 9, null, "Total Room Nights", String.valueOf(d.totalRoomNights()));
            line(summary, 10, null, "Verified Payments", String.valueOf(d.verifiedPayments().size()));
            line(summary, 11, null, "Total Transactions", String.valueOf(d.allPayments().size()));
            line(summary, 13, header, "Payment Method", "Amount");
            line(summary, 14, null, "Cash", SalesReportData.money(d.cashTotal()));
            line(summary, 15, null, "Card", SalesReportData.money(d.cardTotal()));
            line(summary, 16, null, "Bank Transfer", SalesReportData.money(d.bankTransferTotal()));
            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            // ---- Bookings ----
            Sheet bookings = wb.createSheet("Bookings");
            line(bookings, 0, header, "Reservation", "Guest", "Room", "Room Type", "Check-in", "Check-out");
            int r = 1;
            for (Reservation res : d.reservations()) {
                line(bookings, r++, null,
                        String.valueOf(res.getId()),
                        res.getGuest() != null ? res.getGuest().getFullName() : "-",
                        res.getRoom() != null ? res.getRoom().getRoomNumber() : "-",
                        res.getRoom() != null ? res.getRoom().getRoomTypeName() : "-",
                        String.valueOf(res.getCheckInDate()),
                        String.valueOf(res.getCheckOutDate()));
            }
            sizeColumns(bookings, 6);

            // ---- Refunds ----
            Sheet refunds = wb.createSheet("Refunds");
            line(refunds, 0, header, "Reservation", "Refund Date", "Refund Amount", "Status");
            r = 1;
            for (Payment p : d.refundedPayments()) {
                line(refunds, r++, null,
                        p.getReservation() != null ? String.valueOf(p.getReservation().getId()) : "-",
                        p.getRefundDate() != null ? p.getRefundDate().toString() : "-",
                        SalesReportData.money(p.getRefundAmount()),
                        String.valueOf(p.getStatus()));
            }
            sizeColumns(refunds, 4);

            // ---- Detailed sales ----
            Sheet sales = wb.createSheet("Detailed Sales");
            line(sales, 0, header, "Date", "Reservation", "Guest", "Room", "Payment Method", "Amount", "Status");
            r = 1;
            for (Payment p : d.allPayments()) {
                Reservation res = p.getReservation();
                line(sales, r++, null,
                        p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate().toString() : "-",
                        res != null && res.getId() != null ? String.valueOf(res.getId()) : "-",
                        res != null && res.getGuest() != null ? res.getGuest().getFullName() : "-",
                        res != null && res.getRoom() != null ? res.getRoom().getRoomNumber() : "-",
                        p.getPaymentMethod() != null ? p.getPaymentMethod().toString() : "-",
                        SalesReportData.money(p.getAmount()),
                        String.valueOf(p.getStatus()));
            }
            sizeColumns(sales, 7);

            wb.write(out);
            return out.toByteArray();
        }
    }

    /** Writes one row of text cells, optionally with a style. */
    private void line(Sheet sheet, int rowIndex, CellStyle style, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            var cell = row.createCell(i);
            cell.setCellValue(values[i] == null ? "-" : values[i]);
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }

    private void sizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}

