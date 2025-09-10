package com.one.core.domain.service.tenant.gym;

import com.one.core.domain.model.tenant.gym.Membership;
import com.one.core.domain.model.tenant.gym.NotificacionMembresia;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.repository.tenant.gym.MembershipRepository;
import com.one.core.domain.repository.tenant.gym.NotificacionMembresiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificacionMembresiaService {
    
    private final NotificacionMembresiaRepository notificacionRepository;
    private final MembershipRepository membershipRepository;
    
    /**
     * Tarea programada que se ejecuta diariamente a las 9:00 AM
     * para verificar membresías próximas a vencer
     */
    @Scheduled(cron = "0 0 9 * * *") // Todos los días a las 9:00 AM
    public void verificarMembresiasProximasAVencer() {
        log.info("Iniciando verificación de membresías próximas a vencer");
        
        LocalDate hoy = LocalDate.now();
        List<Membership> membresiasActivas = membershipRepository.findByStatusAndEndDateIsNotNull(
            com.one.core.domain.model.enums.gym.MembershipStatus.ACTIVE);
        
        for (Membership membresia : membresiasActivas) {
            if (membresia.getEndDate() != null) {
                long diasRestantes = ChronoUnit.DAYS.between(hoy, membresia.getEndDate());
                
                // Notificación a 10 días
                if (diasRestantes == 10) {
                    crearNotificacion(membresia, "VENCIMIENTO_10_DIAS", 
                        "Tu membresía vence en 10 días", 
                        "Tu membresía " + membresia.getPlan().getName() + " vence el " + 
                        membresia.getEndDate() + ". ¡Renueva ahora para no perder acceso!");
                }
                
                // Notificación a 5 días
                if (diasRestantes == 5) {
                    crearNotificacion(membresia, "VENCIMIENTO_5_DIAS", 
                        "Tu membresía vence en 5 días", 
                        "Tu membresía " + membresia.getPlan().getName() + " vence el " + 
                        membresia.getEndDate() + ". ¡Última oportunidad para renovar!");
                }
                
                // Notificación de vencida
                if (diasRestantes < 0) {
                    crearNotificacion(membresia, "VENCIDA", 
                        "Tu membresía ha vencido", 
                        "Tu membresía " + membresia.getPlan().getName() + " ha vencido. " +
                        "Renueva ahora para recuperar el acceso a todas las instalaciones.");
                }
            }
        }
        
        log.info("Verificación de membresías completada");
    }
    
    private void crearNotificacion(Membership membresia, String tipo, String titulo, String mensaje) {
        // Verificar si ya existe una notificación de este tipo para esta membresía
        List<NotificacionMembresia> notificacionesExistentes = notificacionRepository
            .findByTipoNotificacionPendiente(tipo);
        
        boolean yaExiste = notificacionesExistentes.stream()
            .anyMatch(n -> n.getMembership().getId().equals(membresia.getId()));
        
        if (!yaExiste) {
            NotificacionMembresia notificacion = new NotificacionMembresia();
            notificacion.setCustomer(membresia.getCustomer());
            notificacion.setMembership(membresia);
            notificacion.setTipoNotificacion(tipo);
            notificacion.setTitulo(titulo);
            notificacion.setMensaje(mensaje);
            notificacion.setFechaVencimiento(membresia.getEndDate().atStartOfDay());
            notificacion.setDiasRestantes((int) ChronoUnit.DAYS.between(LocalDate.now(), membresia.getEndDate()));
            notificacion.setCanalEnvio("EMAIL"); // Por defecto email
            
            notificacionRepository.save(notificacion);
            log.info("Notificación creada para cliente {}: {}", 
                membresia.getCustomer().getId(), titulo);
        }
    }
    
    @Transactional(readOnly = true)
    public List<NotificacionMembresia> obtenerNotificacionesPorCliente(Customer customer) {
        return notificacionRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }
    
    @Transactional(readOnly = true)
    public List<NotificacionMembresia> obtenerNotificacionesNoLeidas(Customer customer) {
        return notificacionRepository.findByCustomerAndIsLeidaFalseOrderByCreatedAtDesc(customer);
    }
    
    @Transactional(readOnly = true)
    public long contarNotificacionesNoLeidas(Customer customer) {
        return notificacionRepository.countNotificacionesNoLeidas(customer);
    }
    
    public void marcarComoLeida(Long notificacionId) {
        notificacionRepository.findById(notificacionId).ifPresent(notificacion -> {
            notificacion.setLeida(true);
            notificacion.setFechaLectura(LocalDateTime.now());
            notificacionRepository.save(notificacion);
        });
    }
    
    public void marcarComoEnviada(Long notificacionId, String canalEnvio) {
        notificacionRepository.findById(notificacionId).ifPresent(notificacion -> {
            notificacion.setEnviada(true);
            notificacion.setFechaEnvio(LocalDateTime.now());
            notificacion.setCanalEnvio(canalEnvio);
            notificacionRepository.save(notificacion);
        });
    }
    
    @Transactional(readOnly = true)
    public List<NotificacionMembresia> obtenerNotificacionesPendientesDeEnvio() {
        return notificacionRepository.findPendientesDeEnvio(LocalDateTime.now());
    }
}
