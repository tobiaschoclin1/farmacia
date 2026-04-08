package com.tobias.web.controller;

import com.tobias.db.Db;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/kpis")
    public Map<String, Object> getKPIs() throws Exception {
        Map<String, Object> kpis = new LinkedHashMap<>();

        try (Connection c = Db.get()) {
            // Total productos activos
            String sqlTotal = "SELECT COUNT(*) FROM productos WHERE activo = 1";
            try (PreparedStatement ps = c.prepareStatement(sqlTotal);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kpis.put("totalProductos", rs.getInt(1));
                }
            }

            // Productos por estado de stock
            String sqlStock = """
                SELECT
                  SUM(CASE WHEN total_base >= p.stock_minimo AND total_base > 0 THEN 1 ELSE 0 END) AS en_stock,
                  SUM(CASE WHEN total_base > 0 AND total_base < p.stock_minimo THEN 1 ELSE 0 END) AS bajo_stock,
                  SUM(CASE WHEN total_base = 0 THEN 1 ELSE 0 END) AS sin_stock
                FROM productos p
                LEFT JOIN (
                  SELECT l.producto_id, COALESCE(SUM(sl.cantidad_base), 0) AS total_base
                  FROM lotes l
                  LEFT JOIN stock_lote sl ON sl.lote_id = l.id
                  GROUP BY l.producto_id
                ) stock ON stock.producto_id = p.id
                WHERE p.activo = 1
                """;

            try (PreparedStatement ps = c.prepareStatement(sqlStock);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kpis.put("enStock", rs.getInt("en_stock"));
                    kpis.put("stockBajo", rs.getInt("bajo_stock"));
                    kpis.put("sinStock", rs.getInt("sin_stock"));
                }
            }

            // Productos próximos a vencer (30 días)
            String sqlVencer = """
                SELECT COUNT(DISTINCT l.producto_id)
                FROM lotes l
                INNER JOIN productos p ON p.id = l.producto_id
                WHERE p.activo = 1
                  AND l.fecha_vencimiento IS NOT NULL
                  AND l.fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days'
                  AND l.fecha_vencimiento >= CURRENT_DATE
                """;

            try (PreparedStatement ps = c.prepareStatement(sqlVencer);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kpis.put("proxVencer", rs.getInt(1));
                }
            }
        }

        return kpis;
    }

    @GetMapping("/proximos-vencer")
    public List<Map<String, Object>> getProximosVencer() throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();

        String sql = """
            SELECT p.nombre, l.fecha_vencimiento, COALESCE(sl.cantidad_base, 0) AS cantidad
            FROM lotes l
            INNER JOIN productos p ON p.id = l.producto_id
            LEFT JOIN stock_lote sl ON sl.lote_id = l.id
            WHERE p.activo = 1
              AND l.fecha_vencimiento IS NOT NULL
              AND l.fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days'
              AND l.fecha_vencimiento >= CURRENT_DATE
            ORDER BY l.fecha_vencimiento
            LIMIT 10
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                String nombre = rs.getString("nombre");
                String fechaVto = rs.getString("fecha_vencimiento");
                int cantidad = rs.getInt("cantidad");

                item.put("nombre", nombre);
                item.put("fechaVencimiento", fechaVto);
                item.put("cantidad", cantidad);

                // Calcular días restantes
                try {
                    LocalDate fecha = LocalDate.parse(fechaVto);
                    long dias = ChronoUnit.DAYS.between(LocalDate.now(), fecha);
                    item.put("diasRestantes", dias);
                    item.put("fechaFormateada", fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } catch (Exception ignored) {
                    item.put("diasRestantes", 0);
                    item.put("fechaFormateada", fechaVto);
                }

                items.add(item);
            }
        }

        return items;
    }

    @GetMapping("/stock-bajo")
    public List<Map<String, Object>> getStockBajo() throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();

        String sql = """
            SELECT p.nombre, p.stock_minimo, COALESCE(SUM(sl.cantidad_base), 0) AS cantidad_actual
            FROM productos p
            LEFT JOIN lotes l ON l.producto_id = p.id
            LEFT JOIN stock_lote sl ON sl.lote_id = l.id
            WHERE p.activo = 1
            GROUP BY p.id, p.nombre, p.stock_minimo
            HAVING cantidad_actual > 0 AND cantidad_actual < p.stock_minimo
            ORDER BY (cantidad_actual * 1.0 / p.stock_minimo)
            LIMIT 10
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("nombre", rs.getString("nombre"));
                item.put("stockMinimo", rs.getInt("stock_minimo"));
                item.put("cantidadActual", rs.getInt("cantidad_actual"));

                double porcentaje = (rs.getInt("cantidad_actual") * 100.0) / rs.getInt("stock_minimo");
                item.put("porcentaje", Math.round(porcentaje));

                items.add(item);
            }
        }

        return items;
    }
}
