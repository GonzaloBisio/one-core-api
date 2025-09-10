package com.one.core.application.controller.tenant.gym;

import com.one.core.domain.model.tenant.gym.NotificacionMembresia;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.service.tenant.gym.NotificacionMembresiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gym/notificaciones-membresia")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notificaciones de Membresía", description = "Gestión de notificaciones de vencimiento de membresías")
public class NotificacionMembresiaController {
    
    private final NotificacionMembresiaService notificacionService;
    
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener notificaciones por cliente")
    public ResponseEntity<List<NotificacionMembresia>> obtenerNotificacionesPorCliente(@PathVariable Long clienteId) {
        log.info("Obteniendo notificaciones para cliente: {}", clienteId);
        // Aquí necesitarías obtener el Customer por ID
        // List<NotificacionMembresia> notificaciones = notificacionService.obtenerNotificacionesPorCliente(customer);
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/cliente/{clienteId}/no-leidas")
    @Operation(summary = "Obtener notificaciones no leídas por cliente")
    public ResponseEntity<List<NotificacionMembresia>> obtenerNotificacionesNoLeidas(@PathVariable Long clienteId) {
        log.info("Obteniendo notificaciones no leídas para cliente: {}", clienteId);
        // Aquí necesitarías obtener el Customer por ID
        // List<NotificacionMembresia> notificaciones = notificacionService.obtenerNotificacionesNoLeidas(customer);
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/cliente/{clienteId}/contador")
    @Operation(summary = "Contar notificaciones no leídas por cliente")
    public ResponseEntity<Long> contarNotificacionesNoLeidas(@PathVariable Long clienteId) {
        log.info("Contando notificaciones no leídas para cliente: {}", clienteId);
        // Aquí necesitarías obtener el Customer por ID
        // long count = notificacionService.contarNotificacionesNoLeidas(customer);
        return ResponseEntity.ok(0L);
    }
    
    @PutMapping("/{notificacionId}/marcar-leida")
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long notificacionId) {
        log.info("Marcando notificación como leída: {}", notificacionId);
        notificacionService.marcarComoLeida(notificacionId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{notificacionId}/marcar-enviada")
    @Operation(summary = "Marcar notificación como enviada")
    public ResponseEntity<Void> marcarComoEnviada(@PathVariable Long notificacionId, @RequestParam String canalEnvio) {
        log.info("Marcando notificación como enviada: {} por canal: {}", notificacionId, canalEnvio);
        notificacionService.marcarComoEnviada(notificacionId, canalEnvio);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/pendientes-envio")
    @Operation(summary = "Obtener notificaciones pendientes de envío")
    public ResponseEntity<List<NotificacionMembresia>> obtenerNotificacionesPendientesDeEnvio() {
        log.info("Obteniendo notificaciones pendientes de envío");
        List<NotificacionMembresia> notificaciones = notificacionService.obtenerNotificacionesPendientesDeEnvio();
        return ResponseEntity.ok(notificaciones);
    }
    
    @PostMapping("/verificar-vencimientos")
    @Operation(summary = "Ejecutar verificación manual de vencimientos")
    public ResponseEntity<Void> verificarVencimientos() {
        log.info("Ejecutando verificación manual de vencimientos");
        notificacionService.verificarMembresiasProximasAVencer();
        return ResponseEntity.ok().build();
    }
}
