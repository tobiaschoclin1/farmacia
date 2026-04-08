package com.tobias.service;

import com.tobias.db.Db;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockService {

  // ---------- Tipos auxiliares ----------
  public static class EntradaItem {
    public final int productoId;
    public final LocalDate fechaLote;       // puede ser null
    public final LocalDate fechaVencimiento;// puede ser null
    public final int cantidadBase;
    public EntradaItem(int productoId, LocalDate fechaLote, LocalDate fechaVencimiento, int cantidadBase) {
      this.productoId = productoId;
      this.fechaLote = fechaLote;
      this.fechaVencimiento = fechaVencimiento;
      this.cantidadBase = cantidadBase;
    }
  }

  public static class SalidaAsignacion {
    public int loteId;
    public int cantidadBase;
    public String fechaLote;        // solo informativo para UI/export
    public String fechaVencimiento; // solo informativo para UI/export
    public SalidaAsignacion(int loteId, int cantidadBase, String fechaLote, String fechaVencimiento) {
      this.loteId = loteId;
      this.cantidadBase = cantidadBase;
      this.fechaLote = fechaLote;
      this.fechaVencimiento = fechaVencimiento;
    }
  }

  // ---------- ENTRADAS ----------
  public void registrarEntrada(String usuario, List<com.tobias.model.EntradaItem> items) throws Exception {
    try (Connection c = Db.get()) {
      c.setAutoCommit(false);
      try {
        for (var it : items) {
          int loteId = ensureLote(c, it.getProductoId(), it.getFechaLote(), it.getFechaVencimiento());
          // sumar al stock
          addStock(c, loteId, it.getCantidadBase());
          // movimiento
          insertMovimiento(c, "ENTRADA", usuario, it.getProductoId(), loteId, it.getCantidadBase(), "Entrada");
        }
        c.commit();
      } catch (Exception ex) {
        c.rollback();
        throw ex;
      } finally {
        c.setAutoCommit(true);
      }
    }
  }

  private int ensureLote(Connection c, int productoId, LocalDate fechaLote, LocalDate fechaVenc) throws Exception {
    String sel = "SELECT id FROM lotes WHERE producto_id=? AND COALESCE(fecha_lote,'')=COALESCE(?, '') AND COALESCE(fecha_vencimiento,'')=COALESCE(?, '')";
    try (PreparedStatement ps = c.prepareStatement(sel)) {
      ps.setInt(1, productoId);
      ps.setString(2, fechaLote==null? null : fechaLote.toString());
      ps.setString(3, fechaVenc==null? null : fechaVenc.toString());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
      }
    }
    String ins = "INSERT INTO lotes(producto_id,fecha_lote,fecha_vencimiento) VALUES(?,?,?)";
    try (PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, productoId);
      if (fechaLote==null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, fechaLote.toString());
      if (fechaVenc==null) ps.setNull(3, Types.VARCHAR); else ps.setString(3, fechaVenc.toString());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
    }
  }

  private void addStock(Connection c, int loteId, int cantidadBase) throws Exception {
    String up = """
      INSERT INTO stock_lote(lote_id, cantidad_base) VALUES(?,?)
      ON CONFLICT(lote_id) DO UPDATE SET cantidad_base = cantidad_base + excluded.cantidad_base
      """;
    try (PreparedStatement ps = c.prepareStatement(up)) {
      ps.setInt(1, loteId);
      ps.setInt(2, cantidadBase);
      ps.executeUpdate();
    }
  }

  // ---------- SALIDAS FEFO (existente) ----------
  public List<SalidaAsignacion> previewSalidaFefo(int productoId, int cantidadBase) throws Exception {
    List<SalidaAsignacion> res = new ArrayList<>();
    String sql = """
      SELECT l.id, l.fecha_lote, l.fecha_vencimiento, COALESCE(sl.cantidad_base,0) AS stock
      FROM lotes l
      JOIN stock_lote sl ON sl.lote_id = l.id
      WHERE l.producto_id= ? AND sl.cantidad_base > 0
      ORDER BY l.fecha_vencimiento IS NULL, l.fecha_vencimiento, l.fecha_lote, l.id
      """;
    int pend = cantidadBase;
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, productoId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next() && pend > 0) {
          int loteId = rs.getInt(1);
          String fl  = rs.getString(2);
          String fv  = rs.getString(3);
          int stock  = rs.getInt(4);
          int take = Math.min(pend, stock);
          res.add(new SalidaAsignacion(loteId, take, fl, fv));
          pend -= take;
        }
      }
    }
    if (pend > 0) throw new IllegalStateException("Stock insuficiente para cubrir la cantidad solicitada.");
    return res;
  }

  public void registrarSalidaFefo(String usuario, int productoId, int cantidadBase, String motivo) throws Exception {
    List<SalidaAsignacion> asign = previewSalidaFefo(productoId, cantidadBase);
    registrarSalidaPorLotes(usuario, motivo, asign);
  }

  // ---------- NUEVO: SALIDA MANUAL POR LOTES ----------
  public void registrarSalidaPorLotes(String usuario, String motivo, List<SalidaAsignacion> asignaciones) throws Exception {
    try (Connection c = Db.get()) {
      c.setAutoCommit(false);
      try {
        for (SalidaAsignacion a : asignaciones) {
          // Validar stock suficiente
          int actual = stockActualLote(c, a.loteId);
          if (a.cantidadBase <= 0) throw new IllegalArgumentException("Cantidad inválida.");
          if (a.cantidadBase > actual) throw new IllegalStateException("Stock insuficiente en el lote " + a.loteId);

          // Descontar
          String up = "UPDATE stock_lote SET cantidad_base = cantidad_base - ? WHERE lote_id = ?";
          try (PreparedStatement ps = c.prepareStatement(up)) {
            ps.setInt(1, a.cantidadBase);
            ps.setInt(2, a.loteId);
            ps.executeUpdate();
          }

          // Producto del lote (para registrar movimiento)
          Integer productoId = getProductoIdDeLote(c, a.loteId);

          // Movimiento
          insertMovimiento(c, "SALIDA", usuario, productoId, a.loteId, a.cantidadBase, motivo);
        }
        c.commit();
      } catch (Exception ex) {
        c.rollback();
        throw ex;
      } finally {
        c.setAutoCommit(true);
      }
    }
  }

  // ---------- helpers DB ----------
  private int stockActualLote(Connection c, int loteId) throws Exception {
    String q = "SELECT COALESCE(cantidad_base,0) FROM stock_lote WHERE lote_id=?";
    try (PreparedStatement ps = c.prepareStatement(q)) {
      ps.setInt(1, loteId);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
    }
  }

  private Integer getProductoIdDeLote(Connection c, int loteId) throws Exception {
    String q = "SELECT producto_id FROM lotes WHERE id=?";
    try (PreparedStatement ps = c.prepareStatement(q)) {
      ps.setInt(1, loteId);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
    }
  }

  private void insertMovimiento(Connection c, String tipo, String usuario, Integer productoId, int loteId, int cantidadBase, String motivo) throws Exception {
    // Si tu esquema ya tiene movimientos_stock con estas columnas, perfecto.
    // Si no, comentá esto o ajustá los nombres.
    String ins = """
      INSERT INTO movimientos_stock(tipo, usuario, producto_id, lote_id, cantidad_base, motivo, fecha_hora)
      VALUES (?, ?, ?, ?, ?, ?, DEFAULT)
      """;
    try (PreparedStatement ps = c.prepareStatement(ins)) {
      ps.setString(1, tipo);
      ps.setString(2, usuario);
      if (productoId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, productoId);
      ps.setInt(4, loteId);
      ps.setInt(5, cantidadBase);
      ps.setString(6, motivo);
      ps.executeUpdate();
    } catch (SQLException e) {
      // Si la tabla no existe, ignoramos el movimiento (no rompemos la salida)
      if (!e.getMessage().toLowerCase().contains("no such table")) throw e;
    }
  }
}
