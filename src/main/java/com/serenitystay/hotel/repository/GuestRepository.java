package com.serenitystay.hotel.repository;

import com.serenitystay.hotel.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNic(String nic);
}