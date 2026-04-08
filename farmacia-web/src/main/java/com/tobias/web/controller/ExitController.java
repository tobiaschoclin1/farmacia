package com.tobias.web.controller;

import com.tobias.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salidas")
public class ExitController {

    private final StockService stockService = new StockService();

    @PostMapping("/fefo")
    public ResponseEntity<Map<String, Object>> registrarSalidaFefo(@RequestBody SalidaFefoRequest request) {
        try {
            stockService.registrarSalidaFefo(
                    request.usuario,
                    request.productoId,
                    request.cantidad,
                    request.motivo
            );

            return ResponseEntity.ok(Map.of("message", "Salida registrada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/preview-fefo")
    public ResponseEntity<Object> previewSalidaFefo(
            @RequestParam int productoId,
            @RequestParam int cantidad) {
        try {
            List<StockService.SalidaAsignacion> preview =
                    stockService.previewSalidaFefo(productoId, cantidad);

            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/manual")
    public ResponseEntity<Map<String, String>> registrarSalidaManual(@RequestBody SalidaManualRequest request) {
        try {
            List<StockService.SalidaAsignacion> asignaciones = request.lotes.stream()
                    .map(l -> new StockService.SalidaAsignacion(l.loteId, l.cantidad, null, null))
                    .toList();

            stockService.registrarSalidaPorLotes(request.usuario, request.motivo, asignaciones);

            return ResponseEntity.ok(Map.of("message", "Salida manual registrada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // DTOs
    public static class SalidaFefoRequest {
        public String usuario;
        public int productoId;
        public int cantidad;
        public String motivo;
    }

    public static class SalidaManualRequest {
        public String usuario;
        public String motivo;
        public List<LoteAsignacion> lotes;
    }

    public static class LoteAsignacion {
        public int loteId;
        public int cantidad;
    }
}
