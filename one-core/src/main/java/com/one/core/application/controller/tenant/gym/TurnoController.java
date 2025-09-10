package com.one.core.application.controller.tenant.gym;

import com.one.core.domain.model.tenant.gym.Turno;
import com.one.core.domain.service.tenant.gym.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/gym/turnos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Turnos", description = "Gestión de turnos y citas")
public class TurnoController {
    
    private final TurnoService turnoService;
    
    @PostMapping
    @Operation(summary = "Crear nuevo turno")
    public ResponseEntity<Turno> crearTurno(@RequestBody Turno turno) {
        log.info("Creando nuevo turno");
        Turno turnoCreado = turnoService.crearTurno(turno);
        return ResponseEntity.ok(turnoCreado);
    }
    
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener turnos por cliente")
    public ResponseEntity<List<Turno>> obtenerTurnosPorCliente(@PathVariable Long clienteId) {
        log.info("Obteniendo turnos para cliente: {}", clienteId);
        // Aquí necesitarías obtener el Customer por ID
        // List<Turno> turnos = turnoService.obtenerTurnosPorCliente(customer);
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/fecha")
    @Operation(summary = "Obtener turnos por rango de fechas")
    public ResponseEntity<List<Turno>> obtenerTurnosPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        log.info("Obteniendo turnos entre {} y {}", fechaInicio, fechaFin);
        List<Turno> turnos = turnoService.obtenerTurnosPorFecha(fechaInicio, fechaFin);
        return ResponseEntity.ok(turnos);
    }
    
    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Obtener turnos por instructor")
    public ResponseEntity<List<Turno>> obtenerTurnosPorInstructor(
            @PathVariable Long instructorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        log.info("Obteniendo turnos para instructor: {}", instructorId);
        List<Turno> turnos = turnoService.obtenerTurnosPorInstructor(instructorId, fechaInicio, fechaFin);
        return ResponseEntity.ok(turnos);
    }
    
    @GetMapping("/sala/{roomId}")
    @Operation(summary = "Obtener turnos por sala")
    public ResponseEntity<List<Turno>> obtenerTurnosPorSala(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        log.info("Obteniendo turnos para sala: {}", roomId);
        List<Turno> turnos = turnoService.obtenerTurnosPorSala(roomId, fechaInicio, fechaFin);
        return ResponseEntity.ok(turnos);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar turno")
    public ResponseEntity<Turno> actualizarTurno(@PathVariable Long id, @RequestBody Turno turno) {
        log.info("Actualizando turno: {}", id);
        turno.setId(id);
        Turno turnoActualizado = turnoService.actualizarTurno(turno);
        return ResponseEntity.ok(turnoActualizado);
    }
    
    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar turno")
    public ResponseEntity<Void> cancelarTurno(@PathVariable Long id) {
        log.info("Cancelando turno: {}", id);
        turnoService.cancelarTurno(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar turno")
    public ResponseEntity<Void> eliminarTurno(@PathVariable Long id) {
        log.info("Eliminando turno: {}", id);
        turnoService.eliminarTurno(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener turno por ID")
    public ResponseEntity<Turno> obtenerTurnoPorId(@PathVariable Long id) {
        log.info("Obteniendo turno: {}", id);
        return turnoService.obtenerTurnoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
