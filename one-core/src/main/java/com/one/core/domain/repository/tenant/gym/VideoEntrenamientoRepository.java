package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.VideoEntrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoEntrenamientoRepository extends JpaRepository<VideoEntrenamiento, Long> {
    
    // Los videos ahora se obtienen a través de los ejercicios del plan
    
    List<VideoEntrenamiento> findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
    
    List<VideoEntrenamiento> findByInstructorIdAndIsActiveTrueOrderByCreatedAtDesc(Long instructorId);
    
    @Query("SELECT v FROM VideoEntrenamiento v WHERE v.isActive = true AND " +
           "(LOWER(v.titulo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<VideoEntrenamiento> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT v FROM VideoEntrenamiento v WHERE v.isActive = true AND v.nivelDificultad = :nivel")
    List<VideoEntrenamiento> findByNivelDificultad(@Param("nivel") String nivel);
    
    @Query("SELECT v FROM VideoEntrenamiento v WHERE v.isActive = true AND " +
           "LOWER(v.musculosTrabajados) LIKE LOWER(CONCAT('%', :musculo, '%'))")
    List<VideoEntrenamiento> findByMusculoTrabajado(@Param("musculo") String musculo);
    
    List<VideoEntrenamiento> findByTituloContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String titulo);
    
    List<VideoEntrenamiento> findByNivelDificultadAndIsActiveTrueOrderByCreatedAtDesc(String nivel);
    
    List<VideoEntrenamiento> findByMusculosTrabajadosContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String musculo);
}
