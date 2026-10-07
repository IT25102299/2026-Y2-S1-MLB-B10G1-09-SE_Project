package com.serenitystay.hotel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "room_type_lookup")
public class RoomTypeLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @NotNull(message = "Price multiplier is required")
    @DecimalMin(value = "0.01", message = "Price multiplier must be at least 0.01")
    @Column(name = "price_multiplier", nullable = false)
    private BigDecimal priceMultiplier = BigDecimal.ONE;

    @DecimalMin(value = "0.00", message = "Service fee cannot be negative")
    @Column(name = "service_fee")
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(name = "image_url", length = 200)
    private String imageUrl;

    @Column(name = "is_active")
    private boolean active = true;

    public RoomTypeLookup() {}

    public RoomTypeLookup(String name, String description, BigDecimal priceMultiplier, BigDecimal serviceFee) {
        this.name = name;
        this.description = description;
        this.priceMultiplier = priceMultiplier;
        this.serviceFee = serviceFee;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPriceMultiplier() { return priceMultiplier; }
    public void setPriceMultiplier(BigDecimal priceMultiplier) { this.priceMultiplier = priceMultiplier; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
