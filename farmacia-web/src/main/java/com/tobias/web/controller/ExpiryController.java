package com.tobias.web.controller;

import com.tobias.db.Db;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/vencimientos")
public class ExpiryController {

    @GetMapping
    public List<Map<String, Object>> getVencimientos(@RequestParam(defaultValue = "90") int dias) throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();

        String sql = """
            SELECT
                p.id,
                p.nombre,
                p.codigo_barra,
                l.id AS lote_id,
                l.fecha_lote,
                l.fecha_vencimiento,
                COALESCE(sl.cantidad_base, 0) AS cantidad
            FROM lotes l
            INNER JOIN productos p ON p.id = l.producto_id
            LEFT JOIN stock_lote sl ON sl.lote_id = l.id
            WHERE p.activo = true
              AND l.fecha_vencimiento IS NOT NULL
              AND l.fecha_vencimiento <= CURRENT_DATE + (? || ' days')::INTERVAL
              AND COALESCE(sl.cantidad_base, 0) > 0
            ORDER BY l.fecha_vencimiento, p.nombre
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, dias);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productoId", rs.getInt("id"));
                    item.put("nombre", rs.getString("nombre"));
                    item.put("codigoBarra", rs.getString("codigo_barra"));
                    item.put("loteId", rs.getInt("lote_id"));
                    item.put("fechaLote", rs.getString("fecha_lote"));
                    item.put("fechaVencimiento", rs.getString("fecha_vencimiento"));
                    item.put("cantidad", rs.getInt("cantidad"));

                    // Calcular días restantes y estado
                    try {
                        LocalDate fechaVto = LocalDate.parse(rs.getString("fecha_vencimiento"));
                        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaVto);

                        item.put("diasRestantes", diasRestantes);
                        item.put("fechaFormateada", fechaVto.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                        String estado;
                        if (diasRestantes < 0) {
                            estado = "VENCIDO";
                        } else if (diasRestantes <= 7) {
                            estado = "VENCE_MUY_PRONTO";
                        } else if (diasRestantes <= 30) {
                            estado = "VENCE_PRONTO";
                        } else {
                            estado = "VENCE_FUTURO";
                        }
                        item.put("estado", estado);
                    } catch (Exception ignored) {
                        item.put("diasRestantes", 0);
                        item.put("fechaFormateada", rs.getString("fecha_vencimiento"));
                        item.put("estado", "VENCIDO");
                    }

                    items.add(item);
                }
            }
        }

        return items;
    }

    @GetMapping("/stats")
    public Map<String, Integer> getStats() throws Exception {
        Map<String, Integer> stats = new LinkedHashMap<>();

        try (Connection c = Db.get()) {
            // Vencidos
            String sqlVencidos = """
                SELECT COUNT(DISTINCT l.id)
                FROM lotes l
                LEFT JOIN stock_lote sl ON sl.lote_id = l.id
                INNER JOIN productos p ON p.id = l.producto_id
                WHERE p.activo = true
                  AND l.fecha_vencimiento IS NOT NULL
                  AND l.fecha_vencimiento < CURRENT_DATE
                  AND COALESCE(sl.cantidad_base, 0) > 0
                """;

            try (PreparedStatement ps = c.prepareStatement(sqlVencidos);
                 ResultSet rs = ps.executeQuery()) {
                stats.put("vencidos", rs.next() ? rs.getInt(1) : 0);
            }

            // Vencen en 7 días
            String sql7dias = """
                SELECT COUNT(DISTINCT l.id)
                FROM lotes l
                LEFT JOIN stock_lote sl ON sl.lote_id = l.id
                INNER JOIN productos p ON p.id = l.producto_id
                WHERE p.activo = true
                  AND l.fecha_vencimiento IS NOT NULL
                  AND l.fecha_vencimiento >= CURRENT_DATE
                  AND l.fecha_vencimiento <= CURRENT_DATE + INTERVAL '7 days'
                  AND COALESCE(sl.cantidad_base, 0) > 0
                """;

            try (PreparedStatement ps = c.prepareStatement(sql7dias);
                 ResultSet rs = ps.executeQuery()) {
                stats.put("vencen7dias", rs.next() ? rs.getInt(1) : 0);
            }

            // Vencen en 30 días
            String sql30dias = """
                SELECT COUNT(DISTINCT l.id)
                FROM lotes l
                LEFT JOIN stock_lote sl ON sl.lote_id = l.id
                INNER JOIN productos p ON p.id = l.producto_id
                WHERE p.activo = true
                  AND l.fecha_vencimiento IS NOT NULL
                  AND l.fecha_vencimiento >= CURRENT_DATE
                  AND l.fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days'
                  AND COALESCE(sl.cantidad_base, 0) > 0
                """;

            try (PreparedStatement ps = c.prepareStatement(sql30dias);
                 ResultSet rs = ps.executeQuery()) {
                stats.put("vencen30dias", rs.next() ? rs.getInt(1) : 0);
            }
        }

        return stats;
    }
}
