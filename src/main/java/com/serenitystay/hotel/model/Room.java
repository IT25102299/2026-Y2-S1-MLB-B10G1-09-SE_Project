package com.serenitystay.hotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Abstract base for every room type in the hotel.
 *
 * Physical status is:
 *   READY       - available for guests
 *   OCCUPIED    - a guest is currently staying
 *   CLEANING    - housekeeping is preparing the room
 *   MAINTENANCE - temporarily out of service
 *
 * NOTE: Room availability for a given DATE RANGE is determined by
 * overlapping reservations, NOT by room status.
 *
 * PRICING: the nightly rate is no longer hardcoded in the subclasses.
 * It is calculated from the room's {@link RoomTypeLookup}:
 *      rate = basePrice x priceMultiplier + serviceFee
 * so editing the lookup data immediately changes the rate.
 */
@Entity
@Table(name = "rooms")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "room_type")
public abstract class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", nullable = false, unique = true, length = 10)
    private String roomNumber;

    @DecimalMin(value = "0.0", inclusive = true, message = "Base price cannot be negative")
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.READY;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private int maxOccupancy = 2;

    /** Housekeeping staff member currently responsible for this room (optional). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_staff_id")
    private Staff assignedStaff;

    /** Pricing data (multiplier + service fee) for this room's type. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_lookup_id")
    private RoomTypeLookup roomTypeLookup;

    protected Room() {
        // required by JPA
    }

    protected Room(String roomNumber, BigDecimal basePrice, String description) {
        this.roomNumber = roomNumber;
        this.basePrice = basePrice;
        this.description = description;
    }

    /**
     * The nightly rate charged for this room, driven by lookup data.
     * Falls back to the plain base price when no lookup is linked yet
     * (e.g. old rows that have not been back-filled).
     */
    public BigDecimal calculateNightlyRate() {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        if (roomTypeLookup == null) {
            return basePrice.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal multiplier = roomTypeLookup.getPriceMultiplier() == null
                ? BigDecimal.ONE : roomTypeLookup.getPriceMultiplier();
        BigDecimal fee = roomTypeLookup.getServiceFee() == null
                ? BigDecimal.ZERO : roomTypeLookup.getServiceFee();
        return basePrice.multiply(multiplier)
                .add(fee)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Polymorphic: a short label identifying the concrete room type. */
    public abstract String getRoomTypeName();

    // ----- Encapsulated accessors -----

    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Staff getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(Staff assignedStaff) { this.assignedStaff = assignedStaff; }
    public RoomTypeLookup getRoomTypeLookup() { return roomTypeLookup; }
    public void setRoomTypeLookup(RoomTypeLookup roomTypeLookup) { this.roomTypeLookup = roomTypeLookup; }

    /**
     * Physical status of a room.
     * NOTE: "BOOKED" is intentionally absent - bookings are represented
     * by Reservation records, not by room status.
     */
    public enum RoomStatus {
        READY,
        OCCUPIED,
        CLEANING,
        MAINTENANCE;

        /**
         * Which MANUAL status changes housekeeping may make:
         *   READY       -> CLEANING, MAINTENANCE
         *   CLEANING    -> READY, MAINTENANCE
         *   MAINTENANCE -> READY, CLEANING
         *   OCCUPIED    -> (none) the room becomes CLEANING automatically at
         *                  check-out; use "Report Issue" for a fault in an
         *                  occupied room.
         * Nobody can set OCCUPIED by hand; only check-in does that.
         */
        public boolean canChangeTo(RoomStatus next) {
            if (next == null || next == this || next == OCCUPIED) {
                return false;
            }
            return switch (this) {
                case READY, CLEANING, MAINTENANCE -> true;
                case OCCUPIED -> false;
            };
        }
    }
}
