package com.tobias.dao;

import com.tobias.db.Db;
import com.tobias.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

  public List<Product> findAll(String filtro) throws Exception {
    String sql = """
      SELECT id, codigo_barra, nombre, unidad_base, unidades_por_caja, stock_minimo, activo
      FROM productos
      WHERE (? IS NULL OR nombre LIKE ? OR codigo_barra LIKE ?)
      ORDER BY nombre
    """;
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      String f = (filtro == null || filtro.isBlank()) ? null : "%" + filtro.trim() + "%";
      ps.setObject(1, f); ps.setObject(2, f); ps.setObject(3, f);
      try (ResultSet rs = ps.executeQuery()) {
        List<Product> out = new ArrayList<>();
        while (rs.next()) {
          out.add(new Product(
            rs.getInt("id"),
            rs.getString("codigo_barra"),
            rs.getString("nombre"),
            rs.getString("unidad_base"),
            (Integer) rs.getObject("unidades_por_caja"),
            rs.getInt("stock_minimo"),
            rs.getInt("activo") == 1
          ));
        }
        return out;
      }
    }
  }

  public Product findByCodigoBarra(String codigo) throws Exception {
    String sql = "SELECT id, codigo_barra, nombre, unidad_base, unidades_por_caja, stock_minimo, activo FROM productos WHERE codigo_barra = ?";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, codigo);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Product(
            rs.getInt("id"),
            rs.getString("codigo_barra"),
            rs.getString("nombre"),
            rs.getString("unidad_base"),
            (Integer) rs.getObject("unidades_por_caja"),
            rs.getInt("stock_minimo"),
            rs.getInt("activo") == 1
          );
        }
        return null;
      }
    }
  }

  public Product insert(Product p) throws Exception {
    String sql = """
      INSERT INTO productos(codigo_barra, nombre, unidad_base, unidades_por_caja, stock_minimo, activo)
      VALUES(?,?,?,?,?,?)
    """;
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, emptyToNull(p.getCodigoBarra()));
      ps.setString(2, p.getNombre());
      ps.setString(3, p.getUnidadBase());
      if (p.getUnidadesPorCaja() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, p.getUnidadesPorCaja());
      ps.setInt(5, p.getStockMinimo() == null ? 0 : p.getStockMinimo());
      ps.setInt(6, p.isActivo() ? 1 : 0);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) p.setId(keys.getInt(1)); }
      return p;
    }
  }

  public void update(Product p) throws Exception {
    String sql = """
      UPDATE productos SET codigo_barra=?, nombre=?, unidad_base=?, unidades_por_caja=?, stock_minimo=?, activo=?
      WHERE id=?
    """;
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, emptyToNull(p.getCodigoBarra()));
      ps.setString(2, p.getNombre());
      ps.setString(3, p.getUnidadBase());
      if (p.getUnidadesPorCaja() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, p.getUnidadesPorCaja());
      ps.setInt(5, p.getStockMinimo() == null ? 0 : p.getStockMinimo());
      ps.setInt(6, p.isActivo() ? 1 : 0);
      ps.setInt(7, p.getId());
      ps.executeUpdate();
    }
  }

  public void delete(int id) throws Exception {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("DELETE FROM productos WHERE id=?")) {
      ps.setInt(1, id);
      ps.executeUpdate();
    }
  }

  private String emptyToNull(String s) {
    return (s == null || s.isBlank()) ? null : s.trim();
  }
}
