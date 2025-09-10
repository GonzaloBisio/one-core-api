package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.EjercicioPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjercicioPlanRepository extends JpaRepository<EjercicioPlan, Long> {
    
    List<EjercicioPlan> findByPlanEntrenamientoIdOrderByOrden(Long planId);
    
    @Query("SELECT e FROM EjercicioPlan e WHERE e.planEntrenamiento.id = :planId AND e.video IS NOT NULL ORDER BY e.orden")
    List<EjercicioPlan> findByPlanEntrenamientoIdWithVideoOrderByOrden(@Param("planId") Long planId);
    
    @Query("SELECT e FROM EjercicioPlan e WHERE e.planEntrenamiento.id = :planId AND e.video IS NULL ORDER BY e.orden")
    List<EjercicioPlan> findByPlanEntrenamientoIdWithoutVideoOrderByOrden(@Param("planId") Long planId);
    
    @Query("SELECT e FROM EjercicioPlan e WHERE e.planEntrenamiento.id = :planId AND LOWER(e.nombreEjercicio) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY e.orden")
    List<EjercicioPlan> findByPlanEntrenamientoIdAndSearchTerm(@Param("planId") Long planId, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT e FROM EjercicioPlan e WHERE e.planEntrenamiento.id = :planId AND e.dificultad = :dificultad ORDER BY e.orden")
    List<EjercicioPlan> findByPlanEntrenamientoIdAndDificultad(@Param("planId") Long planId, @Param("dificultad") String dificultad);
    
    @Query("SELECT e FROM EjercicioPlan e WHERE e.planEntrenamiento.id = :planId AND LOWER(e.musculosTrabajados) LIKE LOWER(CONCAT('%', :musculo, '%')) ORDER BY e.orden")
    List<EjercicioPlan> findByPlanEntrenamientoIdAndMusculo(@Param("planId") Long planId, @Param("musculo") String musculo);
}
