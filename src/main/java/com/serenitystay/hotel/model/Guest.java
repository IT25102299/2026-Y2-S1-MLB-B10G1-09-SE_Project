package com.serenitystay.hotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "guests")
@SecondaryTable(
        name = "guest_credentials",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "guest_id"))
public class Guest extends Person {

    @Column(length = 255)
    private String address;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;   // kept for staff, but hidden from guest self-service

    @NotBlank(message = "Password is required")
    // Stored in a SEPARATE table (guest_credentials) as a BCrypt hash.
    @Column(table = "guest_credentials", name = "password_hash", nullable = false, length = 128)
    private String password;

    // NEW: NIC field (unique, optional for guests)
    @Column(name = "nic", unique = true, length = 20)
    private String nic;

    public Guest() {
        super();
    }

    public Guest(String fullName, String email, String phone, String address, String specialRequests, String password, String nic) {
        super(fullName, email, phone);
        this.address = address;
        this.specialRequests = specialRequests;
        this.password = password;
        this.nic = nic;
    }

    // Simplified constructor (for DataSeeder "“ you can pass null for nic)
    public Guest(String fullName, String email, String phone, String address, String specialRequests, String password) {
        super(fullName, email, phone);
        this.address = address;
        this.specialRequests = specialRequests;
        this.password = password;
    }

    @Override
    public String getRole() {
        return "Guest";
    }

    @Override
    public String describe() {
        return super.describe() + (address != null ? " | " + address : "");
    }

    // Getters and setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
}
