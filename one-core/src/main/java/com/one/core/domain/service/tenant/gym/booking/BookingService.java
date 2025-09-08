package com.one.core.domain.service.tenant.gym.booking;

import com.one.core.application.dto.tenant.gym.BookingCheckInRequestDTO;
import com.one.core.application.dto.tenant.gym.BookingCreateDTO;
import com.one.core.application.dto.tenant.gym.BookingDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.gym.booking.BookingMapper;
import com.one.core.domain.model.enums.gym.BookingStatus;
import com.one.core.domain.model.enums.gym.SessionStatus;
import com.one.core.domain.model.enums.gym.UsageReason;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.gym.*;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.gym.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final ClassSessionRepository sessionRepo;
    private final CustomerRepository customerRepo;
    private final MembershipRepository membershipRepo;
    private final MembershipUsageEventRepository usageEventRepo;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepo,
                          ClassSessionRepository sessionRepo,
                          CustomerRepository customerRepo,
                          MembershipRepository membershipRepo,
                          MembershipUsageEventRepository usageEventRepo,
                          BookingMapper bookingMapper) {
        this.bookingRepo = bookingRepo;
        this.sessionRepo = sessionRepo;
        this.customerRepo = customerRepo;
        this.membershipRepo = membershipRepo;
        this.usageEventRepo = usageEventRepo;
        this.bookingMapper = bookingMapper;
    }

    @Transactional
    public BookingDTO create(BookingCreateDTO dto) {
        // 1) Asociaciones
        ClassSession session = sessionRepo.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassSession", "id", dto.getSessionId()));
        Customer customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));
        Membership membership = null;

        if (dto.getMembershipId() != null) {
            membership = membershipRepo.findById(dto.getMembershipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", dto.getMembershipId()));
            if (!membership.getCustomer().getId().equals(customer.getId())) {
                throw new ValidationException("La membresía no pertenece al cliente.");
            }
            if (membership.getStatus() == null || !membership.getStatus().name().equals("ACTIVE")) {
                throw new ValidationException("La membresía no está activa.");
            }
            LocalDate sessionDate = session.getStartAt().toLocalDate();
            if (membership.getStartDate() != null && sessionDate.isBefore(membership.getStartDate())) {
                throw new ValidationException("La membresía aún no está vigente para la fecha de la clase.");
            }
            if (membership.getEndDate() != null && sessionDate.isAfter(membership.getEndDate())) {
                throw new ValidationException("La membresía está vencida para la fecha de la clase.");
            }
            // (opcional) validar tags/plan si corresponde
        }

        // 2) Reglas de sesión
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new ValidationException("No se pueden tomar reservas sobre sesiones no programadas.");
        }
        if (session.getCapacity() != null && session.getBookedCount() >= session.getCapacity()) {
            // Si querés manejar WAITLIST, acá setearías waitlistPosition y status WAITLISTED.
            throw new ValidationException("La clase ya está completa.");
        }

        // 3) Unicidad por cliente y sesión
        if (bookingRepo.existsBySessionIdAndCustomerId(session.getId(), customer.getId())) {
            throw new ValidationException("El cliente ya tiene una reserva en esta clase.");
        }

        // 4) Armar y persistir
        Booking b = bookingMapper.toNewEntity(dto);
        bookingMapper.applyAssociations(b, session, customer, membership);
        Booking saved = bookingRepo.save(b);

        // 5) Actualizar contador de la sesión
        session.setBookedCount(session.getBookedCount() + 1);
        sessionRepo.save(session);

        return bookingMapper.toDTO(saved);
    }

    @Transactional
    public void cancel(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (b.getStatus() == BookingStatus.CANCELLED) return;
        if (b.getStatus() == BookingStatus.CHECKED_IN) {
            throw new ValidationException("No se puede cancelar una reserva con CHECK-IN realizado.");
        }

        b.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(b);

        // devolver cupo si estaba reservando lugar
        ClassSession s = b.getSession();
        if (s != null && s.getBookedCount() != null && s.getBookedCount() > 0) {
            s.setBookedCount(s.getBookedCount() - 1);
            sessionRepo.save(s);
        }
    }

    @Transactional
    public void checkIn(BookingCheckInRequestDTO req) {
        Booking b = bookingRepo.findById(req.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", req.getBookingId()));

        if (b.getStatus() != BookingStatus.BOOKED) {
            throw new ValidationException("Solo se puede hacer check-in sobre reservas en estado BOOKED.");
        }

        b.setStatus(BookingStatus.CHECKED_IN);
        bookingRepo.save(b);

        // Registrar uso de membresía si aplica
        if (b.getMembership() != null) {
            MembershipUsageEvent ev = new MembershipUsageEvent();
            ev.setMembership(b.getMembership());
            ev.setSession(b.getSession());
            ev.setEventDate(b.getSession() != null ? b.getSession().getStartAt().toLocalDate() : LocalDate.now());
            ev.setUnits(1);
            ev.setReason(UsageReason.CHECK_IN);
            usageEventRepo.save(ev);
        }
    }
}
