package com.one.core.application.controller.tenant.events;

import com.one.core.application.dto.tenant.events.EventOrderDTO;
import com.one.core.application.dto.tenant.events.EventOrderFilterDTO;
import com.one.core.application.dto.tenant.events.EventOrderRequestDTO;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.service.tenant.events.EventOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.one.core.application.dto.tenant.response.PageableResponse;


@RestController
@RequestMapping("/event-orders")
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SALES_MANAGER', 'SUPER_ADMIN')") // Define los roles adecuados
public class EventOrderController {

    private final EventOrderService eventOrderService;

    @Autowired
    public EventOrderController(EventOrderService eventOrderService) {
        this.eventOrderService = eventOrderService;
    }

    @PostMapping
    public ResponseEntity<EventOrderDTO> createEventOrder(
            @Valid @RequestBody EventOrderRequestDTO requestDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EventOrderDTO createdOrder = eventOrderService.createEventOrder(requestDTO, currentUser);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<EventOrderDTO> confirmEventOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EventOrderDTO confirmedOrder = eventOrderService.confirmEventOrder(id, currentUser);
        return ResponseEntity.ok(confirmedOrder);
    }

    @GetMapping
    public ResponseEntity<PageableResponse<EventOrderDTO>> getAllEventOrders(
            EventOrderFilterDTO filter,
            @PageableDefault(size = 20, sort = "eventDate", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<EventOrderDTO> eventPage = eventOrderService.getAllEventOrders(filter, pageable);
        return ResponseEntity.ok(new PageableResponse<>(eventPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventOrderDTO> getEventOrderById(@PathVariable Long id) {
        EventOrderDTO eventOrder = eventOrderService.getEventOrderById(id);
        return ResponseEntity.ok(eventOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventOrder(@PathVariable Long id) {
        eventOrderService.deleteEventOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ready")
    public ResponseEntity<EventOrderDTO> markOrderAsReady(@PathVariable Long id) {
        EventOrderDTO readyOrder = eventOrderService.markOrderAsReady(id);
        return ResponseEntity.ok(readyOrder);
    }
}