package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.Turno;
import com.one.core.domain.model.tenant.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    List<Turno> findByCustomerOrderByFechaHoraDesc(Customer customer);
    
    List<Turno> findByFechaHoraBetweenOrderByFechaHora(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT t FROM Turno t WHERE t.instructor.id = :instructorId AND t.fechaHora BETWEEN :start AND :end ORDER BY t.fechaHora")
    List<Turno> findByInstructorAndFechaHoraBetween(@Param("instructorId") Long instructorId, 
                                                   @Param("start") LocalDateTime start, 
                                                   @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Turno t WHERE t.room.id = :roomId AND t.fechaHora BETWEEN :start AND :end ORDER BY t.fechaHora")
    List<Turno> findByRoomAndFechaHoraBetween(@Param("roomId") Long roomId, 
                                             @Param("start") LocalDateTime start, 
                                             @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.instructor.id = :instructorId AND t.fechaHora = :fechaHora")
    long countByInstructorAndFechaHora(@Param("instructorId") Long instructorId, 
                                      @Param("fechaHora") LocalDateTime fechaHora);
    
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.room.id = :roomId AND t.fechaHora = :fechaHora")
    long countByRoomAndFechaHora(@Param("roomId") Long roomId, 
                                @Param("fechaHora") LocalDateTime fechaHora);
}
