package com.serenitystay.hotel.controller;

import com.serenitystay.hotel.model.Guest;
import com.serenitystay.hotel.model.RefundRequest;
import com.serenitystay.hotel.model.Reservation;
import com.serenitystay.hotel.service.GuestService;
import com.serenitystay.hotel.service.RefundService;
import com.serenitystay.hotel.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/guests")
public class GuestController {

    private final GuestService guestService;
    private final ReservationService reservationService;
    private final RefundService refundService;
    private final PasswordEncoder passwordEncoder;

    public GuestController(GuestService guestService,
                           ReservationService reservationService,
                           RefundService refundService,
                           PasswordEncoder passwordEncoder) {
        this.guestService = guestService;
        this.reservationService = reservationService;
        this.refundService = refundService;
        this.passwordEncoder = passwordEncoder;
    }

    // -------- Staff CRUD --------

    @GetMapping
    public String list(Model model) {
        model.addAttribute("guests", guestService.findAll());
        return "guests/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("guest", new Guest());
        return "guests/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("guest") Guest guest,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "guests/form";
        }
        if (guestService.findByEmail(guest.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.guest", "Email already registered.");
            return "guests/form";
        }
        // Default password for staff-registered guests
        if (guest.getPassword() == null || guest.getPassword().isBlank()) {
            guest.setPassword(passwordEncoder.encode("guest123"));
        } else {
            guest.setPassword(passwordEncoder.encode(guest.getPassword()));
        }
        guestService.save(guest);
        redirectAttributes.addFlashAttribute("message", "Guest registered successfully.");
        return "redirect:/guests";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("guest", guestService.findById(id));
        return "guests/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute Guest guest,
                         RedirectAttributes redirectAttributes) {
        Guest existing = guestService.findById(id);
        existing.setFullName(guest.getFullName());
        existing.setEmail(guest.getEmail());
        existing.setPhone(guest.getPhone());
        existing.setAddress(guest.getAddress());
        existing.setSpecialRequests(guest.getSpecialRequests());
        guestService.save(existing);
        redirectAttributes.addFlashAttribute("message", "Guest updated successfully.");
        return "redirect:/guests";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        guestService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Guest deleted successfully.");
        return "redirect:/guests";
    }

    // -------- Guest self-service dashboard --------

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Guest guest = guestService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Guest not found"));

        List<Reservation> reservations = reservationService.findByGuestId(guest.getId());

        Map<Long, RefundRequest> refundRequestMap = new HashMap<>();
        Map<Long, BigDecimal> suggestedRefundMap = new HashMap<>();
        Map<Long, String> policyMap = new HashMap<>();

        for (Reservation r : reservations) {
            List<RefundRequest> existing = refundService.findByReservation(r);
            if (!existing.isEmpty()) {
                refundRequestMap.put(r.getId(), existing.get(existing.size() - 1));
            }
            if (r.getStatus() == Reservation.ReservationStatus.PENDING
                    || r.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
                suggestedRefundMap.put(r.getId(), refundService.calculateRefundAmount(r));
                policyMap.put(r.getId(), refundService.describeRefundPolicy(r));
            }
        }

        model.addAttribute("guest", guest);
        model.addAttribute("reservations", reservations);
        model.addAttribute("refundRequestMap", refundRequestMap);
        model.addAttribute("suggestedRefundMap", suggestedRefundMap);
        model.addAttribute("policyMap", policyMap);
        return "guest/dashboard";
    }
}