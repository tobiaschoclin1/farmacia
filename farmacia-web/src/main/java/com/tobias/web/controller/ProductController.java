package com.tobias.web.controller;

import com.tobias.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public List<Product> findAll(@RequestParam(required = false) String filtro) throws Exception {
        String sql = """
            SELECT id, codigo_barra, nombre, unidad_base, unidades_por_caja, stock_minimo, activo
            FROM productos
            WHERE (? IS NULL OR nombre LIKE ? OR codigo_barra LIKE ?)
            ORDER BY nombre
            """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String f = (filtro == null || filtro.isBlank()) ? null : "%" + filtro.trim() + "%";
            ps.setString(1, f);
            ps.setString(2, f);
            ps.setString(3, f);

            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("codigo_barra"),
                        rs.getString("nombre"),
                        rs.getString("unidad_base"),
                        (Integer) rs.getObject("unidades_por_caja"),
                        rs.getInt("stock_minimo"),
                        rs.getBoolean("activo")
                    ));
                }
                return products;
            }
        }
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Product> findByCodigoBarra(@PathVariable String codigo) throws Exception {
        String sql = "SELECT id, codigo_barra, nombre, unidad_base, unidades_por_caja, stock_minimo, activo FROM productos WHERE codigo_barra = ?";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("codigo_barra"),
                        rs.getString("nombre"),
                        rs.getString("unidad_base"),
                        (Integer) rs.getObject("unidades_por_caja"),
                        rs.getInt("stock_minimo"),
                        rs.getBoolean("activo")
                    );
                    return ResponseEntity.ok(product);
                }
                return ResponseEntity.notFound().build();
            }
        }
    }

    @PostMapping
    public Product create(@RequestBody Product product) throws Exception {
        String sql = """
            INSERT INTO productos(codigo_barra, nombre, unidad_base, unidades_por_caja, stock_minimo, activo)
            VALUES(?,?,?,?,?,?)
            """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, emptyToNull(product.getCodigoBarra()));
            ps.setString(2, product.getNombre());
            ps.setString(3, product.getUnidadBase());
            if (product.getUnidadesPorCaja() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, product.getUnidadesPorCaja());
            }
            ps.setInt(5, product.getStockMinimo() == null ? 0 : product.getStockMinimo());
            ps.setBoolean(6, product.isActivo());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
            return product;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Product product) throws Exception {
        String sql = """
            UPDATE productos SET codigo_barra=?, nombre=?, unidad_base=?, unidades_por_caja=?, stock_minimo=?, activo=?
            WHERE id=?
            """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, emptyToNull(product.getCodigoBarra()));
            ps.setString(2, product.getNombre());
            ps.setString(3, product.getUnidadBase());
            if (product.getUnidadesPorCaja() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, product.getUnidadesPorCaja());
            }
            ps.setInt(5, product.getStockMinimo() == null ? 0 : product.getStockMinimo());
            ps.setBoolean(6, product.isActivo());
            ps.setInt(7, id);

            ps.executeUpdate();
            return ResponseEntity.ok().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) throws Exception {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM productos WHERE id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return ResponseEntity.ok().build();
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
