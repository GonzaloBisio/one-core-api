package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.NotificacionMembresia;
import com.one.core.domain.model.tenant.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacionMembresiaRepository extends JpaRepository<NotificacionMembresia, Long> {
    
    List<NotificacionMembresia> findByCustomerOrderByCreatedAtDesc(Customer customer);
    
    List<NotificacionMembresia> findByCustomerAndIsLeidaFalseOrderByCreatedAtDesc(Customer customer);
    
    @Query("SELECT n FROM NotificacionMembresia n WHERE n.isEnviada = false AND n.fechaVencimiento <= :fecha")
    List<NotificacionMembresia> findPendientesDeEnvio(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT n FROM NotificacionMembresia n WHERE n.tipoNotificacion = :tipo AND n.isEnviada = false")
    List<NotificacionMembresia> findByTipoNotificacionPendiente(@Param("tipo") String tipo);
    
    @Query("SELECT COUNT(n) FROM NotificacionMembresia n WHERE n.customer = :customer AND n.isLeida = false")
    long countNotificacionesNoLeidas(@Param("customer") Customer customer);
}
