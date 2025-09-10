package com.one.core.application.controller.tenant.gym;

import com.one.core.domain.model.tenant.gym.PlanEntrenamiento;
import com.one.core.domain.model.tenant.gym.EjercicioPlan;
import com.one.core.domain.service.tenant.gym.PlanEntrenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gym/planes-entrenamiento")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Planes de Entrenamiento", description = "Gestión de planes de entrenamiento y ejercicios")
public class PlanEntrenamientoController {
    
    private final PlanEntrenamientoService planService;
    
    // Endpoints para Planes
    @PostMapping
    @Operation(summary = "Crear nuevo plan de entrenamiento")
    public ResponseEntity<PlanEntrenamiento> crearPlan(@RequestBody PlanEntrenamiento plan) {
        log.info("Creando nuevo plan de entrenamiento: {}", plan.getNombre());
        PlanEntrenamiento planCreado = planService.crearPlan(plan);
        return ResponseEntity.ok(planCreado);
    }
    
    @GetMapping
    @Operation(summary = "Obtener planes públicos")
    public ResponseEntity<List<PlanEntrenamiento>> obtenerPlanesPublicos() {
        log.info("Obteniendo planes públicos");
        List<PlanEntrenamiento> planes = planService.obtenerPlanesPublicos();
        return ResponseEntity.ok(planes);
    }
    
    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Obtener planes por instructor")
    public ResponseEntity<List<PlanEntrenamiento>> obtenerPlanesPorInstructor(@PathVariable Long instructorId) {
        log.info("Obteniendo planes para instructor: {}", instructorId);
        List<PlanEntrenamiento> planes = planService.obtenerPlanesPorInstructor(instructorId);
        return ResponseEntity.ok(planes);
    }
    
    @GetMapping("/buscar")
    @Operation(summary = "Buscar planes por término")
    public ResponseEntity<List<PlanEntrenamiento>> buscarPlanes(@RequestParam String termino) {
        log.info("Buscando planes con término: {}", termino);
        List<PlanEntrenamiento> planes = planService.buscarPlanes(termino);
        return ResponseEntity.ok(planes);
    }
    
    @GetMapping("/objetivo/{objetivo}")
    @Operation(summary = "Obtener planes por objetivo")
    public ResponseEntity<List<PlanEntrenamiento>> obtenerPlanesPorObjetivo(@PathVariable String objetivo) {
        log.info("Obteniendo planes para objetivo: {}", objetivo);
        List<PlanEntrenamiento> planes = planService.obtenerPlanesPorObjetivo(objetivo);
        return ResponseEntity.ok(planes);
    }
    
    @GetMapping("/nivel/{nivel}")
    @Operation(summary = "Obtener planes por nivel de dificultad")
    public ResponseEntity<List<PlanEntrenamiento>> obtenerPlanesPorNivel(@PathVariable String nivel) {
        log.info("Obteniendo planes para nivel: {}", nivel);
        List<PlanEntrenamiento> planes = planService.obtenerPlanesPorNivel(nivel);
        return ResponseEntity.ok(planes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener plan por ID")
    public ResponseEntity<PlanEntrenamiento> obtenerPlanPorId(@PathVariable Long id) {
        log.info("Obteniendo plan: {}", id);
        return planService.obtenerPlanPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar plan")
    public ResponseEntity<PlanEntrenamiento> actualizarPlan(@PathVariable Long id, @RequestBody PlanEntrenamiento plan) {
        log.info("Actualizando plan: {}", id);
        plan.setId(id);
        PlanEntrenamiento planActualizado = planService.actualizarPlan(plan);
        return ResponseEntity.ok(planActualizado);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar plan")
    public ResponseEntity<Void> eliminarPlan(@PathVariable Long id) {
        log.info("Eliminando plan: {}", id);
        planService.eliminarPlan(id);
        return ResponseEntity.ok().build();
    }
    
    // Endpoints para Ejercicios
    @GetMapping("/{planId}/ejercicios")
    @Operation(summary = "Obtener ejercicios de un plan")
    public ResponseEntity<List<EjercicioPlan>> obtenerEjerciciosPorPlan(@PathVariable Long planId) {
        log.info("Obteniendo ejercicios para plan: {}", planId);
        List<EjercicioPlan> ejercicios = planService.obtenerEjerciciosPorPlan(planId);
        return ResponseEntity.ok(ejercicios);
    }
    
    @GetMapping("/{planId}/ejercicios/con-video")
    @Operation(summary = "Obtener ejercicios con video de un plan")
    public ResponseEntity<List<EjercicioPlan>> obtenerEjerciciosConVideoPorPlan(@PathVariable Long planId) {
        log.info("Obteniendo ejercicios con video para plan: {}", planId);
        List<EjercicioPlan> ejercicios = planService.obtenerEjerciciosConVideoPorPlan(planId);
        return ResponseEntity.ok(ejercicios);
    }
    
    @GetMapping("/{planId}/ejercicios/sin-video")
    @Operation(summary = "Obtener ejercicios sin video de un plan")
    public ResponseEntity<List<EjercicioPlan>> obtenerEjerciciosSinVideoPorPlan(@PathVariable Long planId) {
        log.info("Obteniendo ejercicios sin video para plan: {}", planId);
        List<EjercicioPlan> ejercicios = planService.obtenerEjerciciosSinVideoPorPlan(planId);
        return ResponseEntity.ok(ejercicios);
    }
    
    @PostMapping("/{planId}/ejercicios")
    @Operation(summary = "Agregar ejercicio a un plan")
    public ResponseEntity<EjercicioPlan> agregarEjercicio(@PathVariable Long planId, @RequestBody EjercicioPlan ejercicio) {
        log.info("Agregando ejercicio al plan: {}", planId);
        // Aquí necesitarías establecer la relación con el plan
        // ejercicio.setPlanEntrenamiento(plan);
        EjercicioPlan ejercicioCreado = planService.agregarEjercicio(ejercicio);
        return ResponseEntity.ok(ejercicioCreado);
    }
    
    @GetMapping("/{planId}/ejercicios/buscar")
    @Operation(summary = "Buscar ejercicios en un plan")
    public ResponseEntity<List<EjercicioPlan>> buscarEjerciciosEnPlan(
            @PathVariable Long planId, 
            @RequestParam String termino) {
        log.info("Buscando ejercicios en plan {} con término: {}", planId, termino);
        List<EjercicioPlan> ejercicios = planService.buscarEjerciciosEnPlan(planId, termino);
        return ResponseEntity.ok(ejercicios);
    }
    
    @GetMapping("/{planId}/ejercicios/dificultad/{dificultad}")
    @Operation(summary = "Obtener ejercicios por dificultad en un plan")
    public ResponseEntity<List<EjercicioPlan>> obtenerEjerciciosPorDificultad(
            @PathVariable Long planId, 
            @PathVariable String dificultad) {
        log.info("Obteniendo ejercicios de dificultad {} en plan: {}", dificultad, planId);
        List<EjercicioPlan> ejercicios = planService.obtenerEjerciciosPorDificultad(planId, dificultad);
        return ResponseEntity.ok(ejercicios);
    }
    
    @GetMapping("/{planId}/ejercicios/musculo/{musculo}")
    @Operation(summary = "Obtener ejercicios por músculo en un plan")
    public ResponseEntity<List<EjercicioPlan>> obtenerEjerciciosPorMusculo(
            @PathVariable Long planId, 
            @PathVariable String musculo) {
        log.info("Obteniendo ejercicios para músculo {} en plan: {}", musculo, planId);
        List<EjercicioPlan> ejercicios = planService.obtenerEjerciciosPorMusculo(planId, musculo);
        return ResponseEntity.ok(ejercicios);
    }
    
    @PutMapping("/ejercicios/{ejercicioId}")
    @Operation(summary = "Actualizar ejercicio")
    public ResponseEntity<EjercicioPlan> actualizarEjercicio(@PathVariable Long ejercicioId, @RequestBody EjercicioPlan ejercicio) {
        log.info("Actualizando ejercicio: {}", ejercicioId);
        ejercicio.setId(ejercicioId);
        EjercicioPlan ejercicioActualizado = planService.actualizarEjercicio(ejercicio);
        return ResponseEntity.ok(ejercicioActualizado);
    }
    
    @DeleteMapping("/ejercicios/{ejercicioId}")
    @Operation(summary = "Eliminar ejercicio")
    public ResponseEntity<Void> eliminarEjercicio(@PathVariable Long ejercicioId) {
        log.info("Eliminando ejercicio: {}", ejercicioId);
        planService.eliminarEjercicio(ejercicioId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/ejercicios/{ejercicioId}")
    @Operation(summary = "Obtener ejercicio por ID")
    public ResponseEntity<EjercicioPlan> obtenerEjercicioPorId(@PathVariable Long ejercicioId) {
        log.info("Obteniendo ejercicio: {}", ejercicioId);
        return planService.obtenerEjercicioPorId(ejercicioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
