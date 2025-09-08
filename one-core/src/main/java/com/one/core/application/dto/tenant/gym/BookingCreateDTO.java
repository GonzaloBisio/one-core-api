package com.one.core.application.dto.tenant.gym;

import com.one.core.domain.model.enums.gym.BookingChannel;
import lombok.Data;

@Data
public class BookingCreateDTO {
    public Long sessionId;
    public Long customerId;
    public Long membershipId;            // optional: null = drop-in
    public BookingChannel channel;       // FRONTDESK / APP...
    public String notes;
}