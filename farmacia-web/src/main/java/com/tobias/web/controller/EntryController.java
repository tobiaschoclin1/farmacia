package com.tobias.web.controller;

import com.tobias.model.EntradaItem;
import com.tobias.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entradas")
public class EntryController {

    private final StockService stockService = new StockService();

    @PostMapping
    public ResponseEntity<Map<String, String>> registrarEntrada(@RequestBody EntradaRequest request) {
        try {
            List<EntradaItem> items = new ArrayList<>();

            for (var item : request.items) {
                LocalDate fechaLote = item.fechaLote != null && !item.fechaLote.isBlank()
                        ? LocalDate.parse(item.fechaLote)
                        : LocalDate.now();

                LocalDate fechaVencimiento = item.fechaVencimiento != null && !item.fechaVencimiento.isBlank()
                        ? LocalDate.parse(item.fechaVencimiento)
                        : null;

                items.add(new EntradaItem(
                        item.productoId,
                        "", // nombreProducto no se usa en el service
                        fechaLote,
                        fechaVencimiento,
                        item.cantidad
                ));
            }

            stockService.registrarEntrada(request.usuario, items);

            return ResponseEntity.ok(Map.of("message", "Entrada registrada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // DTOs
    public static class EntradaRequest {
        public String usuario;
        public List<EntradaItemRequest> items;
    }

    public static class EntradaItemRequest {
        public int productoId;
        public String fechaLote;
        public String fechaVencimiento;
        public int cantidad;
    }
}
