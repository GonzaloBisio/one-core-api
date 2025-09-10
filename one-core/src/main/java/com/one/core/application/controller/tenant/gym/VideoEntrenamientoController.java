package com.one.core.application.controller.tenant.gym;

import com.one.core.domain.model.tenant.gym.VideoEntrenamiento;
import com.one.core.domain.service.tenant.gym.PlanEntrenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/gym/videos-entrenamiento")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Videos de Entrenamiento", description = "Gestión de videos de entrenamiento")
public class VideoEntrenamientoController {
    
    private final PlanEntrenamientoService planService;
    
    @GetMapping
    @Operation(summary = "Obtener videos públicos")
    public ResponseEntity<List<VideoEntrenamiento>> obtenerVideosPublicos() {
        log.info("Obteniendo videos públicos");
        List<VideoEntrenamiento> videos = planService.obtenerVideosPublicos();
        return ResponseEntity.ok(videos);
    }
    
    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Obtener videos por instructor")
    public ResponseEntity<List<VideoEntrenamiento>> obtenerVideosPorInstructor(@PathVariable Long instructorId) {
        log.info("Obteniendo videos para instructor: {}", instructorId);
        List<VideoEntrenamiento> videos = planService.obtenerVideosPorInstructor(instructorId);
        return ResponseEntity.ok(videos);
    }
    
    @GetMapping("/buscar")
    @Operation(summary = "Buscar videos por término")
    public ResponseEntity<List<VideoEntrenamiento>> buscarVideos(@RequestParam String termino) {
        log.info("Buscando videos con término: {}", termino);
        List<VideoEntrenamiento> videos = planService.buscarVideos(termino);
        return ResponseEntity.ok(videos);
    }
    
    @GetMapping("/nivel/{nivel}")
    @Operation(summary = "Obtener videos por nivel de dificultad")
    public ResponseEntity<List<VideoEntrenamiento>> obtenerVideosPorNivel(@PathVariable String nivel) {
        log.info("Obteniendo videos para nivel: {}", nivel);
        List<VideoEntrenamiento> videos = planService.obtenerVideosPorNivel(nivel);
        return ResponseEntity.ok(videos);
    }
    
    @GetMapping("/musculo/{musculo}")
    @Operation(summary = "Obtener videos por músculo trabajado")
    public ResponseEntity<List<VideoEntrenamiento>> obtenerVideosPorMusculo(@PathVariable String musculo) {
        log.info("Obteniendo videos para músculo: {}", musculo);
        List<VideoEntrenamiento> videos = planService.obtenerVideosPorMusculo(musculo);
        return ResponseEntity.ok(videos);
    }
    
    @PostMapping
    @Operation(summary = "Crear nuevo video")
    public ResponseEntity<VideoEntrenamiento> crearVideo(@RequestBody VideoEntrenamiento video) {
        log.info("Creando nuevo video: {}", video.getTitulo());
        VideoEntrenamiento videoCreado = planService.crearVideo(video);
        return ResponseEntity.ok(videoCreado);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar video")
    public ResponseEntity<VideoEntrenamiento> actualizarVideo(@PathVariable Long id, @RequestBody VideoEntrenamiento video) {
        log.info("Actualizando video: {}", id);
        video.setId(id);
        VideoEntrenamiento videoActualizado = planService.actualizarVideo(video);
        return ResponseEntity.ok(videoActualizado);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar video")
    public ResponseEntity<Void> eliminarVideo(@PathVariable Long id) {
        log.info("Eliminando video: {}", id);
        planService.eliminarVideo(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener video por ID")
    public ResponseEntity<VideoEntrenamiento> obtenerVideoPorId(@PathVariable Long id) {
        log.info("Obteniendo video: {}", id);
        return planService.obtenerVideoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
