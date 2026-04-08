package com.tobias.web.controller;

import com.tobias.db.Db;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @GetMapping
    public List<Map<String, Object>> getStock(@RequestParam(required = false) String filtro) throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();

        String sql = """
            SELECT
                p.id,
                p.nombre,
                p.codigo_barra,
                p.unidad_base,
                p.stock_minimo,
                COALESCE(SUM(sl.cantidad_base), 0) AS stock_actual,
                CASE
                    WHEN COALESCE(SUM(sl.cantidad_base), 0) = 0 THEN 'SIN_STOCK'
                    WHEN COALESCE(SUM(sl.cantidad_base), 0) < p.stock_minimo THEN 'STOCK_BAJO'
                    ELSE 'EN_STOCK'
                END AS estado
            FROM productos p
            LEFT JOIN lotes l ON l.producto_id = p.id
            LEFT JOIN stock_lote sl ON sl.lote_id = l.id
            WHERE p.activo = true
              AND (? IS NULL OR p.nombre LIKE ? OR p.codigo_barra LIKE ?)
            GROUP BY p.id, p.nombre, p.codigo_barra, p.unidad_base, p.stock_minimo
            ORDER BY p.nombre
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String f = (filtro == null || filtro.isBlank()) ? null : "%" + filtro.trim() + "%";
            ps.setObject(1, f);
            ps.setObject(2, f);
            ps.setObject(3, f);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("nombre", rs.getString("nombre"));
                    item.put("codigoBarra", rs.getString("codigo_barra"));
                    item.put("unidadBase", rs.getString("unidad_base"));
                    item.put("stockMinimo", rs.getInt("stock_minimo"));
                    item.put("stockActual", rs.getInt("stock_actual"));
                    item.put("estado", rs.getString("estado"));
                    items.add(item);
                }
            }
        }

        return items;
    }

    @GetMapping("/lotes")
    public List<Map<String, Object>> getLotesByProducto(@RequestParam int productoId) throws Exception {
        List<Map<String, Object>> lotes = new ArrayList<>();

        String sql = """
            SELECT
                l.id,
                l.fecha_lote,
                l.fecha_vencimiento,
                COALESCE(sl.cantidad_base, 0) AS cantidad
            FROM lotes l
            LEFT JOIN stock_lote sl ON sl.lote_id = l.id
            WHERE l.producto_id = ?
              AND COALESCE(sl.cantidad_base, 0) > 0
            ORDER BY l.fecha_vencimiento IS NULL, l.fecha_vencimiento, l.fecha_lote
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, productoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> lote = new LinkedHashMap<>();
                    lote.put("id", rs.getInt("id"));
                    lote.put("fechaLote", rs.getString("fecha_lote"));
                    lote.put("fechaVencimiento", rs.getString("fecha_vencimiento"));
                    lote.put("cantidad", rs.getInt("cantidad"));
                    lotes.add(lote);
                }
            }
        }

        return lotes;
    }
}
