package com.one.core.application.controller.tenant.gym.bookings;

import com.one.core.application.dto.tenant.gym.BookingCheckInRequestDTO;
import com.one.core.application.dto.tenant.gym.BookingCreateDTO;
import com.one.core.application.dto.tenant.gym.BookingDTO;
import com.one.core.domain.service.tenant.gym.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@PreAuthorize("hasAnyRole('TENANT_USER','TENANT_ADMIN','SUPER_ADMIN')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingCreateDTO dto){
        return ResponseEntity.ok(bookingService.create(dto));
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id){
        bookingService.cancel(id); return ResponseEntity.ok().build();
    }
    @PostMapping("/check-in")
    public ResponseEntity<Void> checkIn(@Valid @RequestBody BookingCheckInRequestDTO req){
        bookingService.checkIn(req); return ResponseEntity.ok().build();
    }
}
