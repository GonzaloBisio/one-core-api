package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.enums.gym.BookingChannel;
import com.one.core.domain.model.enums.gym.BookingStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Entity @Table(name = "bookings",
        uniqueConstraints = @UniqueConstraint(name = "uq_booking_unique", columnNames = {"session_id","customer_id"}))
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "membership_id")
    private Membership membership; // null = drop-in

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(name = "booked_at", nullable = false)
    private OffsetDateTime bookedAt = OffsetDateTime.now();

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BookingChannel channel;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }
}
