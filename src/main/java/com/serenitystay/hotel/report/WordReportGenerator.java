package com.serenitystay.hotel.report;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Strategy: Word (.docx) sales & performance report. (Moved from SalesReportService.) */
@Component
@Order(1)
public class WordReportGenerator implements ReportGenerator {

    @Override
    public String format() {
        return "docx";
    }

    @Override
    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    @Override
    public String fileExtension() {
        return "docx";
    }

    @Override
    public byte[] generate(SalesReportData data) throws IOException {

        LocalDate startDate = data.startDate();
        LocalDate endDate = data.endDate();
        List<Payment> allPayments = data.allPayments();
        List<Payment> verifiedPayments = data.verifiedPayments();
        List<Payment> refundedPayments = data.refundedPayments();
        List<Reservation> reservations = data.reservations();
        BigDecimal totalIncome = data.totalIncome();
        BigDecimal totalRefunds = data.totalRefunds();
        BigDecimal netIncome = data.netIncome();
        long totalBookings = data.totalBookings();
        long roomsBooked = data.roomsBooked();
        long totalRoomNights = data.totalRoomNights();
        BigDecimal cashTotal = data.cashTotal();
        BigDecimal cardTotal = data.cardTotal();
        BigDecimal bankTransferTotal = data.bankTransferTotal();

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            // -----------------------------------------------------
            // PAGE SETTINGS
            // -----------------------------------------------------

            document.getDocument()
                    .getBody()
                    .addNewSectPr()
                    .addNewPgSz();

            // -----------------------------------------------------
            // TITLE
            // -----------------------------------------------------

            XWPFParagraph hotelName = document.createParagraph();
            hotelName.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun hotelRun = hotelName.createRun();
            hotelRun.setText("SERENITY STAY HOTELS");
            hotelRun.setBold(true);
            hotelRun.setFontSize(20);

            XWPFParagraph reportTitle = document.createParagraph();
            reportTitle.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun titleRun = reportTitle.createRun();
            titleRun.setText("Sales & Performance Report");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // -----------------------------------------------------
            // REPORT INFORMATION
            // -----------------------------------------------------

            XWPFParagraph period = document.createParagraph();

            XWPFRun periodRun = period.createRun();
            periodRun.setText(
                    "Report Period: "
                            + startDate
                            + " to "
                            + endDate
            );
            periodRun.setFontSize(11);

            XWPFParagraph generated = document.createParagraph();

            XWPFRun generatedRun = generated.createRun();
            generatedRun.setText(
                    "Generated: "
                            + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern(
                                    "yyyy-MM-dd HH:mm:ss"))
            );
            generatedRun.setFontSize(10);

            // -----------------------------------------------------
            // EXECUTIVE SUMMARY
            // -----------------------------------------------------

            addHeading(document, "Performance Summary");

            XWPFTable summaryTable =
                    document.createTable(8, 2);

            summaryTable.setWidth("100%");

            addTableCell(
                    summaryTable.getRow(0).getCell(0),
                    "Total Income"
            );

            addTableCell(
                    summaryTable.getRow(0).getCell(1),
                    formatMoney(totalIncome)
            );

            addTableCell(
                    summaryTable.getRow(1).getCell(0),
                    "Total Bookings"
            );

            addTableCell(
                    summaryTable.getRow(1).getCell(1),
                    String.valueOf(totalBookings)
            );

            addTableCell(
                    summaryTable.getRow(2).getCell(0),
                    "Rooms Booked"
            );

            addTableCell(
                    summaryTable.getRow(2).getCell(1),
                    String.valueOf(roomsBooked)
            );

            addTableCell(
                    summaryTable.getRow(3).getCell(0),
                    "Total Room Nights"
            );

            addTableCell(
                    summaryTable.getRow(3).getCell(1),
                    String.valueOf(totalRoomNights)
            );

            addTableCell(
                    summaryTable.getRow(4).getCell(0),
                    "Verified Payments"
            );

            addTableCell(
                    summaryTable.getRow(4).getCell(1),
                    String.valueOf(verifiedPayments.size())
            );

            addTableCell(
                    summaryTable.getRow(5).getCell(0),
                    "Total Refunds"
            );

            addTableCell(
                    summaryTable.getRow(5).getCell(1),
                    formatMoney(totalRefunds)
            );

            addTableCell(
                    summaryTable.getRow(6).getCell(0),
                    "Net Income"
            );

            addTableCell(
                    summaryTable.getRow(6).getCell(1),
                    formatMoney(netIncome)
            );

            addTableCell(
                    summaryTable.getRow(7).getCell(0),
                    "Total Transactions"
            );

            addTableCell(
                    summaryTable.getRow(7).getCell(1),
                    String.valueOf(allPayments.size())
            );

            // -----------------------------------------------------
            // PAYMENT METHOD SUMMARY
            // -----------------------------------------------------

            addHeading(document, "Payment Method Summary");

            XWPFTable paymentMethodTable =
                    document.createTable(4, 2);

            addTableCell(
                    paymentMethodTable.getRow(0).getCell(0),
                    "Payment Method"
            );

            addTableCell(
                    paymentMethodTable.getRow(0).getCell(1),
                    "Amount"
            );

            addTableCell(
                    paymentMethodTable.getRow(1).getCell(0),
                    "Cash"
            );

            addTableCell(
                    paymentMethodTable.getRow(1).getCell(1),
                    formatMoney(cashTotal)
            );

            addTableCell(
                    paymentMethodTable.getRow(2).getCell(0),
                    "Card"
            );

            addTableCell(
                    paymentMethodTable.getRow(2).getCell(1),
                    formatMoney(cardTotal)
            );

            addTableCell(
                    paymentMethodTable.getRow(3).getCell(0),
                    "Bank Transfer"
            );

            addTableCell(
                    paymentMethodTable.getRow(3).getCell(1),
                    formatMoney(bankTransferTotal)
            );

            // -----------------------------------------------------
            // BOOKING / ROOM SUMMARY
            // -----------------------------------------------------

            addHeading(document, "Booking & Room Summary");

            XWPFTable bookingTable =
                    document.createTable(
                            Math.max(1, reservations.size() + 1),
                            6
                    );

            addTableCell(
                    bookingTable.getRow(0).getCell(0),
                    "Reservation"
            );

            addTableCell(
                    bookingTable.getRow(0).getCell(1),
                    "Guest"
            );

            addTableCell(
                    bookingTable.getRow(0).getCell(2),
                    "Room"
            );

            addTableCell(
                    bookingTable.getRow(0).getCell(3),
                    "Room Type"
            );

            addTableCell(
                    bookingTable.getRow(0).getCell(4),
                    "Check-in"
            );

            addTableCell(
                    bookingTable.getRow(0).getCell(5),
                    "Check-out"
            );

            for (int i = 0; i < reservations.size(); i++) {

                Reservation reservation =
                        reservations.get(i);

                int rowIndex = i + 1;

                addTableCell(
                        bookingTable.getRow(rowIndex).getCell(0),
                        String.valueOf(reservation.getId())
                );

                String guestName = "-";

                if (reservation.getGuest() != null) {
                    guestName =
                            reservation.getGuest().getFullName();
                }

                addTableCell(
                        bookingTable.getRow(rowIndex).getCell(1),
                        guestName
                );

                String roomNumber = "-";
                String roomType = "-";

                if (reservation.getRoom() != null) {

                    roomNumber =
                            reservation.getRoom().getRoomNumber();

                    roomType =
                            reservation.getRoom().getRoomTypeName();
                }

                addTableCell(
                        bookingTable.getRow(rowIndex).getCell(2),
                        roomNumber
                );

                addTableCell(
                        bookingTable.getRow(rowIndex).getCell(3),
                        roomType
                );

                addTableCell(
                        bookingTable.getRow(rowIndex).getCell(4),
                        String.valueOf(
                                reservation.getCheckInDate())
                );

                addTableCell(
                        bookingTable.getRow(rowIndex).getCell(5),
                        String.valueOf(
                                reservation.getCheckOutDate())
                );
            }

            // -----------------------------------------------------
            // REFUND SUMMARY
            // -----------------------------------------------------

            addHeading(document, "Refund Summary");

            XWPFTable refundTable =
                    document.createTable(
                            Math.max(1, refundedPayments.size() + 1),
                            4
                    );

            addTableCell(
                    refundTable.getRow(0).getCell(0),
                    "Reservation"
            );

            addTableCell(
                    refundTable.getRow(0).getCell(1),
                    "Refund Date"
            );

            addTableCell(
                    refundTable.getRow(0).getCell(2),
                    "Refund Amount"
            );

            addTableCell(
                    refundTable.getRow(0).getCell(3),
                    "Status"
            );

            for (int i = 0;
                 i < refundedPayments.size();
                 i++) {

                Payment payment =
                        refundedPayments.get(i);

                int rowIndex = i + 1;

                String reservationId =
                        payment.getReservation() != null
                                ? String.valueOf(
                                payment.getReservation().getId())
                                : "-";

                String refundDate =
                        payment.getRefundDate() != null
                                ? payment.getRefundDate().toString()
                                : "-";

                String refundAmount =
                        payment.getRefundAmount() != null
                                ? formatMoney(
                                payment.getRefundAmount())
                                : "0.00";

                addTableCell(
                        refundTable.getRow(rowIndex).getCell(0),
                        reservationId
                );

                addTableCell(
                        refundTable.getRow(rowIndex).getCell(1),
                        refundDate
                );

                addTableCell(
                        refundTable.getRow(rowIndex).getCell(2),
                        refundAmount
                );

                addTableCell(
                        refundTable.getRow(rowIndex).getCell(3),
                        payment.getStatus().toString()
                );
            }

            // -----------------------------------------------------
            // DETAILED SALES
            // -----------------------------------------------------

            addHeading(document, "Detailed Sales");

            XWPFTable salesTable =
                    document.createTable(
                            Math.max(1, allPayments.size() + 1),
                            7
                    );

            addTableCell(
                    salesTable.getRow(0).getCell(0),
                    "Date"
            );

            addTableCell(
                    salesTable.getRow(0).getCell(1),
                    "Reservation"
            );

            addTableCell(
                    salesTable.getRow(0).getCell(2),
                    "Guest"
            );

            addTableCell(
                    salesTable.getRow(0).getCell(3),
                    "Room"
            );

            addTableCell(
                    salesTable.getRow(0).getCell(4),
                    "Payment Method"
            );

            addTableCell(
                    salesTable.getRow(0).getCell(5),
                    "Amount"
            );

            addTableCell(
                    salesTable.getRow(0).getCell(6),
                    "Status"
            );

            for (int i = 0;
                 i < allPayments.size();
                 i++) {

                Payment payment =
                        allPayments.get(i);

                int rowIndex = i + 1;

                String date = "-";

                if (payment.getPaymentDate() != null) {
                    date =
                            payment.getPaymentDate()
                                    .toLocalDate()
                                    .toString();
                }

                String reservationId = "-";
                String guestName = "-";
                String roomNumber = "-";

                if (payment.getReservation() != null) {

                    Reservation reservation =
                            payment.getReservation();

                    if (reservation.getId() != null) {
                        reservationId =
                                String.valueOf(
                                        reservation.getId());
                    }

                    if (reservation.getGuest() != null) {
                        guestName =
                                reservation.getGuest()
                                        .getFullName();
                    }

                    if (reservation.getRoom() != null) {
                        roomNumber =
                                reservation.getRoom()
                                        .getRoomNumber();
                    }
                }

                String method = "-";

                if (payment.getPaymentMethod() != null) {
                    method =
                            payment.getPaymentMethod()
                                    .toString();
                }

                addTableCell(
                        salesTable.getRow(rowIndex).getCell(0),
                        date
                );

                addTableCell(
                        salesTable.getRow(rowIndex).getCell(1),
                        reservationId
                );

                addTableCell(
                        salesTable.getRow(rowIndex).getCell(2),
                        guestName
                );

                addTableCell(
                        salesTable.getRow(rowIndex).getCell(3),
                        roomNumber
                );

                addTableCell(
                        salesTable.getRow(rowIndex).getCell(4),
                        method
                );

                addTableCell(
                        salesTable.getRow(rowIndex).getCell(5),
                        formatMoney(payment.getAmount())
                );

                addTableCell(
                        salesTable.getRow(rowIndex).getCell(6),
                        payment.getStatus().toString()
                );
            }

            // -----------------------------------------------------
            // FOOTER
            // -----------------------------------------------------

            XWPFParagraph footerText =
                    document.createParagraph();

            footerText.setAlignment(
                    ParagraphAlignment.CENTER);

            XWPFRun footerRun =
                    footerText.createRun();

            footerRun.setText(
                    "Serenity Stay Hotels - Sales & Performance Report"
            );

            footerRun.setFontSize(9);
            footerRun.setItalic(true);

            // -----------------------------------------------------
            // WRITE DOCUMENT
            // -----------------------------------------------------

            document.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =============================================================
    // HELPER METHODS
    // =============================================================

    private void addHeading(
            XWPFDocument document,
            String text) {

        XWPFParagraph paragraph =
                document.createParagraph();

        paragraph.setSpacingBefore(12);
        paragraph.setSpacingAfter(6);

        XWPFRun run =
                paragraph.createRun();

        run.setText(text);
        run.setBold(true);
        run.setFontSize(14);
    }

    private void addTableCell(
            XWPFTableCell cell,
            String text) {

        cell.removeParagraph(0);

        XWPFParagraph paragraph =
                cell.addParagraph();

        paragraph.setSpacingAfter(0);

        XWPFRun run =
                paragraph.createRun();

        run.setText(
                text == null ? "-" : text
        );

        run.setFontSize(9);
    }

    private String formatMoney(BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return amount
                .setScale(2, RoundingMode.HALF_UP)
                .toString();
    }
}

