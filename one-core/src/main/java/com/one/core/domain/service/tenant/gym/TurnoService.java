package com.one.core.domain.service.tenant.gym;

import com.one.core.domain.model.tenant.gym.Turno;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.repository.tenant.gym.TurnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TurnoService {
    
    private final TurnoRepository turnoRepository;
    
    public Turno crearTurno(Turno turno) {
        log.info("Creando nuevo turno para cliente: {}", turno.getCustomer().getId());
        
        // Validar disponibilidad del instructor
        if (turno.getInstructor() != null) {
            long turnosExistentes = turnoRepository.countByInstructorAndFechaHora(
                turno.getInstructor().getId(), turno.getFechaHora());
            if (turnosExistentes > 0) {
                throw new RuntimeException("El instructor ya tiene un turno en esa fecha y hora");
            }
        }
        
        // Validar disponibilidad de la sala
        if (turno.getRoom() != null) {
            long turnosExistentes = turnoRepository.countByRoomAndFechaHora(
                turno.getRoom().getId(), turno.getFechaHora());
            if (turnosExistentes > 0) {
                throw new RuntimeException("La sala ya está ocupada en esa fecha y hora");
            }
        }
        
        return turnoRepository.save(turno);
    }
    
    @Transactional(readOnly = true)
    public List<Turno> obtenerTurnosPorCliente(Customer customer) {
        return turnoRepository.findByCustomerOrderByFechaHoraDesc(customer);
    }
    
    @Transactional(readOnly = true)
    public List<Turno> obtenerTurnosPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return turnoRepository.findByFechaHoraBetweenOrderByFechaHora(fechaInicio, fechaFin);
    }
    
    @Transactional(readOnly = true)
    public List<Turno> obtenerTurnosPorInstructor(Long instructorId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return turnoRepository.findByInstructorAndFechaHoraBetween(instructorId, fechaInicio, fechaFin);
    }
    
    @Transactional(readOnly = true)
    public List<Turno> obtenerTurnosPorSala(Long roomId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return turnoRepository.findByRoomAndFechaHoraBetween(roomId, fechaInicio, fechaFin);
    }
    
    public Turno actualizarTurno(Turno turno) {
        log.info("Actualizando turno: {}", turno.getId());
        return turnoRepository.save(turno);
    }
    
    public void cancelarTurno(Long turnoId) {
        log.info("Cancelando turno: {}", turnoId);
        Optional<Turno> turnoOpt = turnoRepository.findById(turnoId);
        if (turnoOpt.isPresent()) {
            Turno turno = turnoOpt.get();
            turno.setStatus(com.one.core.domain.model.enums.gym.BookingStatus.CANCELLED);
            turnoRepository.save(turno);
        }
    }
    
    public void eliminarTurno(Long turnoId) {
        log.info("Eliminando turno: {}", turnoId);
        turnoRepository.deleteById(turnoId);
    }
    
    @Transactional(readOnly = true)
    public Optional<Turno> obtenerTurnoPorId(Long id) {
        return turnoRepository.findById(id);
    }
}
