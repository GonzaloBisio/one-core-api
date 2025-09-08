package com.one.core.application.mapper.gym.booking;

import com.one.core.application.dto.tenant.gym.BookingCreateDTO;
import com.one.core.application.dto.tenant.gym.BookingDTO;
import com.one.core.domain.model.enums.gym.BookingChannel;
import com.one.core.domain.model.enums.gym.BookingStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.gym.Booking;
import com.one.core.domain.model.tenant.gym.ClassSession;
import com.one.core.domain.model.tenant.gym.Membership;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class BookingMapper {

    public Booking toNewEntity(BookingCreateDTO dto) {
        Booking b = new Booking();
        b.setStatus(BookingStatus.BOOKED);
        b.setBookedAt(OffsetDateTime.now());
        b.setNotes(dto.getNotes());
        if (dto.getChannel() != null) b.setChannel(dto.getChannel());
        return b;
    }

    public void applyAssociations(Booking target, ClassSession session, Customer customer, Membership membership) {
        target.setSession(session);
        target.setCustomer(customer);
        target.setMembership(membership);
    }

    public BookingDTO toDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        dto.setStatus(b.getStatus());
        dto.setBookedAt(b.getBookedAt());
        dto.setChannel(b.getChannel());
        dto.setWaitlistPosition(b.getWaitlistPosition());
        dto.setNotes(b.getNotes());

        if (b.getCustomer() != null) {
            dto.setCustomerId(b.getCustomer().getId());
            dto.setCustomerName(b.getCustomer().getName());
        }
        if (b.getMembership() != null) {
            dto.setMembershipId(b.getMembership().getId());
            dto.setMembershipStatus(b.getMembership().getStatus());
        }
        if (b.getSession() != null) {
            dto.setSessionId(b.getSession().getId());
        }
        return dto;
    }
}
