package com.serenitystay.hotel.controller;

import com.serenitystay.hotel.model.Payment;
import com.serenitystay.hotel.model.Reservation;
import com.serenitystay.hotel.model.Room;
import com.serenitystay.hotel.repository.GuestRepository;
import com.serenitystay.hotel.repository.PaymentRepository;
import com.serenitystay.hotel.repository.ReservationRepository;
import com.serenitystay.hotel.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public HomeController(
            RoomRepository roomRepository,
            GuestRepository guestRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository) {

        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // =========================================================
        // BASIC DASHBOARD STATISTICS
        // =========================================================

        long totalRooms =
                roomRepository.count();

        long readyRooms =
                roomRepository.findByStatus(
                        Room.RoomStatus.READY
                ).size();

        long maintenanceRooms =
                roomRepository.findByStatus(
                        Room.RoomStatus.MAINTENANCE
                ).size();

        long totalGuests =
                guestRepository.count();

        long totalReservations =
                reservationRepository.count();

        LocalDate today =
                LocalDate.now();


        // =========================================================
        // OCCUPIED ROOMS TODAY
        // =========================================================

        List<Reservation.ReservationStatus> occupiedStatuses =
                List.of(
                        Reservation.ReservationStatus.CONFIRMED,
                        Reservation.ReservationStatus.CHECKED_IN
                );

        long occupiedToday =
                reservationRepository.countOccupiedRoomsForDate(
                        today,
                        occupiedStatuses
                );


        // =========================================================
        // OCCUPANCY PERCENTAGE
        // =========================================================

        long occupancyPercent =
                totalRooms == 0
                        ? 0
                        : Math.round(
                        (occupiedToday * 100.0)
                                / totalRooms
                );


        // =========================================================
        // ADD BASIC STATISTICS
        // =========================================================

        model.addAttribute(
                "totalRooms",
                totalRooms
        );

        model.addAttribute(
                "readyRooms",
                readyRooms
        );

        model.addAttribute(
                "maintenanceRooms",
                maintenanceRooms
        );

        model.addAttribute(
                "occupiedToday",
                occupiedToday
        );

        model.addAttribute(
                "totalGuests",
                totalGuests
        );

        model.addAttribute(
                "totalReservations",
                totalReservations
        );

        model.addAttribute(
                "occupancyPercent",
                occupancyPercent
        );


        // =========================================================
        // REVENUE CHART - LAST 7 DAYS
        // =========================================================

        LocalDate from =
                today.minusDays(6);

        LocalDateTime startDateTime =
                from.atStartOfDay();

        LocalDateTime endDateTime =
                today.plusDays(1).atStartOfDay();

        List<Payment> payments =
                paymentRepository.findByPaymentDateBetween(
                        startDateTime,
                        endDateTime
                );


        Map<LocalDate, BigDecimal> revenueByDate =
                new LinkedHashMap<>();


        // ---------------------------------------------------------
        // Create all 7 dates with zero revenue
        // ---------------------------------------------------------

        for (int i = 0; i < 7; i++) {

            LocalDate date =
                    from.plusDays(i);

            revenueByDate.put(
                    date,
                    BigDecimal.ZERO
            );
        }


        // ---------------------------------------------------------
        // Add payment amounts
        // ---------------------------------------------------------

        for (Payment payment : payments) {

            if (payment.getPaymentDate() != null) {

                LocalDate paymentDate =
                        payment.getPaymentDate().toLocalDate();

                BigDecimal amount =
                        payment.getAmount();

                if (amount != null) {

                    revenueByDate.put(
                            paymentDate,
                            revenueByDate.getOrDefault(
                                    paymentDate,
                                    BigDecimal.ZERO
                            ).add(amount)
                    );
                }
            }
        }


        // ---------------------------------------------------------
        // Prepare revenue chart data
        // ---------------------------------------------------------

        List<String> dates =
                new ArrayList<>();

        List<Double> revenues =
                new ArrayList<>();


        for (Map.Entry<LocalDate, BigDecimal> entry
                : revenueByDate.entrySet()) {

            dates.add(
                    entry.getKey().toString()
            );

            revenues.add(
                    entry.getValue().doubleValue()
            );
        }


        model.addAttribute(
                "dates",
                dates
        );

        model.addAttribute(
                "revenues",
                revenues
        );


        // =========================================================
        // UPCOMING ROOM BOOKINGS
        // NEXT 7 DAYS
        // =========================================================
        //
        // IMPORTANT:
        // Previously this section grouped bookings by ROOM TYPE:
        //
        // Standard
        // Deluxe
        // Suite
        //
        // Now it groups bookings by INDIVIDUAL ROOM NUMBER:
        //
        // Room 101
        // Room 202
        // Room 301
        //
        // This allows the dashboard graph to show exactly
        // which rooms are booked on each date.
        // =========================================================


        // ---------------------------------------------------------
        // Dates for the next 7 days
        // ---------------------------------------------------------

        List<String> bookingDates =
                new ArrayList<>();


        // ---------------------------------------------------------
        // Discover all room numbers
        // ---------------------------------------------------------

        List<String> roomNumbers =
                new ArrayList<>();

        List<Room> allRooms =
                roomRepository.findAll();


        for (Room room : allRooms) {

            if (room.getRoomNumber() != null
                    && !roomNumbers.contains(
                    room.getRoomNumber()
            )) {

                roomNumbers.add(
                        room.getRoomNumber()
                );
            }
        }


        // ---------------------------------------------------------
        // Keep room numbers in a predictable order
        // ---------------------------------------------------------

        roomNumbers.sort(
                (room1, room2) -> {

                    try {

                        return Integer.compare(
                                Integer.parseInt(room1),
                                Integer.parseInt(room2)
                        );

                    } catch (NumberFormatException e) {

                        return room1.compareToIgnoreCase(
                                room2
                        );
                    }
                }
        );


        // ---------------------------------------------------------
        // Create one booking list for every room
        //
        // Example:
        //
        // Room 101 -> [0, 1, 1, 0, 0, 0, 0]
        // Room 202 -> [0, 1, 0, 0, 1, 1, 0]
        // Room 301 -> [1, 0, 0, 0, 0, 1, 1]
        // ---------------------------------------------------------

        List<List<Long>> roomBookingCounts =
                new ArrayList<>();


        for (int i = 0;
             i < roomNumbers.size();
             i++) {

            roomBookingCounts.add(
                    new ArrayList<>()
            );
        }


        // ---------------------------------------------------------
        // Calculate bookings for each of the next 7 days
        // ---------------------------------------------------------

        for (int day = 0; day < 7; day++) {

            LocalDate date =
                    today.plusDays(day);


            bookingDates.add(
                    date.toString()
            );


            // Get reservations active on this date
            List<Reservation> reservations =
                    reservationRepository
                            .findBookedReservationsForDate(
                                    date
                            );


            // -----------------------------------------------------
            // Check every room
            // -----------------------------------------------------

            for (int roomIndex = 0;
                 roomIndex < roomNumbers.size();
                 roomIndex++) {


                String currentRoomNumber =
                        roomNumbers.get(roomIndex);


                long booked =
                        0;


                // -------------------------------------------------
                // Check whether this specific room is booked
                // -------------------------------------------------

                for (Reservation reservation
                        : reservations) {

                    Room room =
                            reservation.getRoom();


                    if (room != null
                            && room.getRoomNumber() != null
                            && currentRoomNumber.equals(
                            room.getRoomNumber()
                    )) {

                        booked = 1;

                        break;
                    }
                }


                roomBookingCounts
                        .get(roomIndex)
                        .add(booked);
            }
        }


        // =========================================================
        // SEND ROOM BOOKING DATA TO DASHBOARD
        // =========================================================

        model.addAttribute(
                "bookingDates",
                bookingDates
        );


        model.addAttribute(
                "roomNumbers",
                roomNumbers
        );


        model.addAttribute(
                "roomBookingCounts",
                roomBookingCounts
        );


        // =========================================================
        // RETURN DASHBOARD
        // =========================================================

        return "dashboard";
    }
}