package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.PlanEntrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanEntrenamientoRepository extends JpaRepository<PlanEntrenamiento, Long> {
    
    List<PlanEntrenamiento> findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
    
    List<PlanEntrenamiento> findByInstructorIdAndIsActiveTrueOrderByCreatedAtDesc(Long instructorId);
    
    @Query("SELECT p FROM PlanEntrenamiento p WHERE p.isActive = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<PlanEntrenamiento> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT p FROM PlanEntrenamiento p WHERE p.isActive = true AND p.objetivo = :objetivo")
    List<PlanEntrenamiento> findByObjetivo(@Param("objetivo") String objetivo);
    
    @Query("SELECT p FROM PlanEntrenamiento p WHERE p.isActive = true AND p.nivelDificultad = :nivel")
    List<PlanEntrenamiento> findByNivelDificultad(@Param("nivel") String nivel);
}
