package com.serenitystay.hotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A room reservation made by a guest.
 * Links a {@link Guest} to a {@link Room} for a date range and tracks its
 * lifecycle via {@link ReservationStatus}.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Guest is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @NotNull(message = "Room is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Check-in date is required")
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    /**
     * Nightly rate LOCKED IN at booking time. Changing a room type's price
     * multiplier later does not change existing reservations.
     */
    @Column(name = "nightly_rate", precision = 10, scale = 2)
    private BigDecimal nightlyRate;

    @Column(name = "special_notes", length = 500)
    private String specialNotes;
    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<StayGuest> stayGuests = new ArrayList<>();
    public List<StayGuest> getStayGuests() {
        return stayGuests;
    }

    public void setStayGuests(List<StayGuest> stayGuests) {
        this.stayGuests = stayGuests;
    }

    public Reservation() {
    }

    public Reservation(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    /** Number of nights covered by this reservation. */
    public long getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Total cost of the stay = locked-in nightly rate x number of nights.
     */
    public java.math.BigDecimal getTotalCost() {
        return getNightlyRate().multiply(java.math.BigDecimal.valueOf(getNumberOfNights()));
    }

    /**
     * The nightly rate for this reservation. Uses the locked-in rate; only
     * falls back to the room's current rate for rows not yet back-filled.
     */
    public BigDecimal getNightlyRate() {
        if (nightlyRate != null) {
            return nightlyRate;
        }
        return room != null ? room.calculateNightlyRate() : BigDecimal.ZERO;
    }

    public void setNightlyRate(BigDecimal nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    /** Locks in the room's current rate if none has been stored yet. */
    @PrePersist
    @PreUpdate
    public void lockInRate() {
        if (nightlyRate == null && room != null) {
            nightlyRate = room.calculateNightlyRate();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    /** Lifecycle states of a reservation. */
    public enum ReservationStatus {
        PENDING,
        CONFIRMED,
        CHECKED_IN,
        CHECKED_OUT,
        CANCELLED
    }
}
