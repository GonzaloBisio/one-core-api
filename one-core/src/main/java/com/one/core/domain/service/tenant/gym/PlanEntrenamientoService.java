package com.one.core.domain.service.tenant.gym;

import com.one.core.domain.model.tenant.gym.PlanEntrenamiento;
import com.one.core.domain.model.tenant.gym.EjercicioPlan;
import com.one.core.domain.model.tenant.gym.VideoEntrenamiento;
import com.one.core.domain.repository.tenant.gym.PlanEntrenamientoRepository;
import com.one.core.domain.repository.tenant.gym.EjercicioPlanRepository;
import com.one.core.domain.repository.tenant.gym.VideoEntrenamientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlanEntrenamientoService {
    
    private final PlanEntrenamientoRepository planRepository;
    private final EjercicioPlanRepository ejercicioRepository;
    private final VideoEntrenamientoRepository videoRepository;
    
    public PlanEntrenamiento crearPlan(PlanEntrenamiento plan) {
        log.info("Creando nuevo plan de entrenamiento: {}", plan.getNombre());
        return planRepository.save(plan);
    }
    
    @Transactional(readOnly = true)
    public List<PlanEntrenamiento> obtenerPlanesPublicos() {
        return planRepository.findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<PlanEntrenamiento> obtenerPlanesPorInstructor(Long instructorId) {
        return planRepository.findByInstructorIdAndIsActiveTrueOrderByCreatedAtDesc(instructorId);
    }
    
    @Transactional(readOnly = true)
    public List<PlanEntrenamiento> buscarPlanes(String terminoBusqueda) {
        return planRepository.findBySearchTerm(terminoBusqueda);
    }
    
    @Transactional(readOnly = true)
    public List<PlanEntrenamiento> obtenerPlanesPorObjetivo(String objetivo) {
        return planRepository.findByObjetivo(objetivo);
    }
    
    @Transactional(readOnly = true)
    public List<PlanEntrenamiento> obtenerPlanesPorNivel(String nivel) {
        return planRepository.findByNivelDificultad(nivel);
    }
    
    public PlanEntrenamiento actualizarPlan(PlanEntrenamiento plan) {
        log.info("Actualizando plan de entrenamiento: {}", plan.getId());
        return planRepository.save(plan);
    }
    
    public void eliminarPlan(Long planId) {
        log.info("Eliminando plan de entrenamiento: {}", planId);
        Optional<PlanEntrenamiento> planOpt = planRepository.findById(planId);
        if (planOpt.isPresent()) {
            PlanEntrenamiento plan = planOpt.get();
            plan.setActive(false);
            planRepository.save(plan);
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<PlanEntrenamiento> obtenerPlanPorId(Long id) {
        return planRepository.findById(id);
    }
    
    // Métodos para ejercicios
    public EjercicioPlan agregarEjercicio(EjercicioPlan ejercicio) {
        log.info("Agregando ejercicio al plan: {}", ejercicio.getPlanEntrenamiento().getId());
        return ejercicioRepository.save(ejercicio);
    }
    
    @Transactional(readOnly = true)
    public List<EjercicioPlan> obtenerEjerciciosPorPlan(Long planId) {
        return ejercicioRepository.findByPlanEntrenamientoIdOrderByOrden(planId);
    }
    
    @Transactional(readOnly = true)
    public List<EjercicioPlan> obtenerEjerciciosConVideoPorPlan(Long planId) {
        return ejercicioRepository.findByPlanEntrenamientoIdWithVideoOrderByOrden(planId);
    }
    
    @Transactional(readOnly = true)
    public List<EjercicioPlan> obtenerEjerciciosSinVideoPorPlan(Long planId) {
        return ejercicioRepository.findByPlanEntrenamientoIdWithoutVideoOrderByOrden(planId);
    }
    
    @Transactional(readOnly = true)
    public List<VideoEntrenamiento> obtenerVideosPublicos() {
        return videoRepository.findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<VideoEntrenamiento> buscarVideos(String terminoBusqueda) {
        return videoRepository.findBySearchTerm(terminoBusqueda);
    }
    
    @Transactional(readOnly = true)
    public List<VideoEntrenamiento> obtenerVideosPorMusculo(String musculo) {
        return videoRepository.findByMusculoTrabajado(musculo);
    }
    
    public VideoEntrenamiento actualizarVideo(VideoEntrenamiento video) {
        log.info("Actualizando video: {}", video.getId());
        return videoRepository.save(video);
    }
    
    public void eliminarVideo(Long videoId) {
        log.info("Eliminando video: {}", videoId);
        Optional<VideoEntrenamiento> videoOpt = videoRepository.findById(videoId);
        if (videoOpt.isPresent()) {
            VideoEntrenamiento video = videoOpt.get();
            video.setActive(false);
            videoRepository.save(video);
        }
    }
    
    // Métodos adicionales para ejercicios
    public EjercicioPlan actualizarEjercicio(EjercicioPlan ejercicio) {
        log.info("Actualizando ejercicio: {}", ejercicio.getId());
        return ejercicioRepository.save(ejercicio);
    }
    
    public void eliminarEjercicio(Long ejercicioId) {
        log.info("Eliminando ejercicio: {}", ejercicioId);
        ejercicioRepository.deleteById(ejercicioId);
    }
    
    @Transactional(readOnly = true)
    public Optional<EjercicioPlan> obtenerEjercicioPorId(Long id) {
        return ejercicioRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<EjercicioPlan> buscarEjerciciosEnPlan(Long planId, String terminoBusqueda) {
        return ejercicioRepository.findByPlanEntrenamientoIdAndSearchTerm(planId, terminoBusqueda);
    }
    
    @Transactional(readOnly = true)
    public List<EjercicioPlan> obtenerEjerciciosPorDificultad(Long planId, String dificultad) {
        return ejercicioRepository.findByPlanEntrenamientoIdAndDificultad(planId, dificultad);
    }
    
    @Transactional(readOnly = true)
    public List<EjercicioPlan> obtenerEjerciciosPorMusculo(Long planId, String musculo) {
        return ejercicioRepository.findByPlanEntrenamientoIdAndMusculo(planId, musculo);
    }
    
    // Métodos adicionales para videos
    public VideoEntrenamiento crearVideo(VideoEntrenamiento video) {
        log.info("Creando nuevo video: {}", video.getTitulo());
        return videoRepository.save(video);
    }
    
    @Transactional(readOnly = true)
    public Optional<VideoEntrenamiento> obtenerVideoPorId(Long id) {
        return videoRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<VideoEntrenamiento> obtenerVideosPorInstructor(Long instructorId) {
        return videoRepository.findByInstructorIdAndIsActiveTrueOrderByCreatedAtDesc(instructorId);
    }
    
    @Transactional(readOnly = true)
    public List<VideoEntrenamiento> obtenerVideosPorNivel(String nivel) {
        return videoRepository.findByNivelDificultad(nivel);
    }
}
