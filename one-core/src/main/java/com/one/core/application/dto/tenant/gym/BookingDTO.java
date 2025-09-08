package com.one.core.application.dto.tenant.gym;

import com.one.core.domain.model.enums.gym.BookingChannel;
import com.one.core.domain.model.enums.gym.BookingStatus;
import com.one.core.domain.model.enums.gym.MembershipStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BookingDTO {
    public Long id;
    public Long sessionId;
    public Long customerId;
    public Long membershipId;
    public BookingStatus status;
    public Integer waitlistPosition;
    public OffsetDateTime bookedAt;
    public BookingChannel channel;
    public String notes;
    public String customerName; // Info derivada (opcional, para UI)
    public MembershipStatus membershipStatus;



}