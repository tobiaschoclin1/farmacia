package com.tobias.ui;

import com.tobias.db.Db;
import com.tobias.util.ExcelUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ImportExportView extends BorderPane {

  private final Button btnExportTodo = new Button("Exportar inventario (XLSX)");
  private final Button btnImportar   = new Button("Importar XLSX (Productos + Lotes)");

  public ImportExportView() {
    setPadding(new Insets(10));
    setTop(buildTop());
    setCenter(new Label("Usá los botones de arriba para exportar o importar planillas Excel."));
  }

  private HBox buildTop() {
    var hb = new HBox(8, btnExportTodo, btnImportar);
    hb.setPadding(new Insets(0,0,10,0));
    hb.getStyleClass().add("toolbar");

    btnExportTodo.setOnAction(e -> exportarInventario());
    btnImportar.setOnAction(e -> importarXlsx());
    return hb;
  }

  private void exportarInventario() {
    try {
      var chooser = new FileChooser();
      chooser.setInitialFileName("inventario_completo.xlsx");
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
      File f = chooser.showSaveDialog(getScene().getWindow());
      if (f == null) return;

      // Hoja Productos (totales)
      String[] hTot = {"Código","Producto","Unidad","Unid/Caja","Cant. base total","Cajas total","Tabletas total","Stock mín."};
      var rowsTot = new ArrayList<ExcelUtil.RowWriter>();

      String sqlTot = """
        SELECT p.codigo_barra, p.nombre, p.unidad_base, p.unidades_por_caja, p.stock_minimo,
               COALESCE(SUM(sl.cantidad_base),0) AS cantidad_base_total
        FROM productos p
        LEFT JOIN lotes l ON l.producto_id = p.id
        LEFT JOIN stock_lote sl ON sl.lote_id = l.id
        GROUP BY p.id
        ORDER BY p.nombre
      """;

      try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sqlTot); ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          String codigo = rs.getString("codigo_barra");
          String nombre = rs.getString("nombre");
          String unidad = rs.getString("unidad_base");
          Integer upc   = (Integer) rs.getObject("unidades_por_caja");
          int stockMin  = rs.getInt("stock_minimo");
          int baseTotal = rs.getInt("cantidad_base_total");
          Integer cajas = (upc!=null && upc>0)? baseTotal/upc : null;
          Integer tabs  = "TABLETA".equalsIgnoreCase(unidad)? baseTotal : null;

          rowsTot.add((row, dateStyle, intStyle) -> {
            ExcelUtil.setString(row, 0, codigo==null?"":codigo);
            ExcelUtil.setString(row, 1, nombre);
            ExcelUtil.setString(row, 2, unidad);
            ExcelUtil.setInt(row, 3, upc, intStyle);
            ExcelUtil.setInt(row, 4, baseTotal, intStyle);
            ExcelUtil.setInt(row, 5, cajas, intStyle);
            ExcelUtil.setInt(row, 6, tabs, intStyle);
            ExcelUtil.setInt(row, 7, stockMin, intStyle);
          });
        }
      }

      // Hoja Lotes
      String[] hLot = {"Código","Producto","Fecha lote","Vencimiento","Cant. base","Cajas"};
      var rowsLot = new ArrayList<ExcelUtil.RowWriter>();
      String sqlLot = """
        SELECT p.codigo_barra, p.nombre, p.unidad_base, p.unidades_por_caja,
               l.fecha_lote, l.fecha_vencimiento, COALESCE(sl.cantidad_base,0) AS cantidad_base
        FROM productos p
        JOIN lotes l ON l.producto_id = p.id
        JOIN stock_lote sl ON sl.lote_id = l.id
        ORDER BY p.nombre, l.fecha_lote
      """;
      try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sqlLot); ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          String codigo = rs.getString("codigo_barra");
          String nombre = rs.getString("nombre");
          Integer upc   = (Integer) rs.getObject("unidades_por_caja");
          String lote   = rs.getString("fecha_lote");
          String vto    = rs.getString("fecha_vencimiento");
          int base      = rs.getInt("cantidad_base");
          Integer cajas = (upc!=null && upc>0)? base/upc : null;

          rowsLot.add((row, dateStyle, intStyle)->{
            ExcelUtil.setString(row, 0, codigo==null?"":codigo);
            ExcelUtil.setString(row, 1, nombre);
            ExcelUtil.setDate(row,   2, lote, dateStyle);
            ExcelUtil.setDate(row,   3, vto,  dateStyle);
            ExcelUtil.setInt(row,    4, base, intStyle);
            ExcelUtil.setInt(row,    5, cajas, intStyle);
          });
        }
      }

      // Exportar dos hojas
      org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
      var df = wb.createDataFormat();
      var bold = wb.createFont(); bold.setBold(true);
      var header = wb.createCellStyle();
      header.setFont(bold);
      header.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
      header.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
      header.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
      var dateStyle = wb.createCellStyle(); dateStyle.setDataFormat(df.getFormat("yyyy-mm-dd"));
      var intStyle  = wb.createCellStyle(); intStyle.setDataFormat(df.getFormat("0"));

      var sh1 = wb.createSheet("Productos");
      var rh1 = sh1.createRow(0);
      for (int i=0;i<hTot.length;i++){ var c=rh1.createCell(i); c.setCellValue(hTot[i]); c.setCellStyle(header); }
      for (int i=0;i<rowsTot.size();i++){ var r=sh1.createRow(i+1); rowsTot.get(i).write(r,dateStyle,intStyle);}
      for (int i=0;i<hTot.length;i++) sh1.autoSizeColumn(i);
      sh1.createFreezePane(0,1);

      var sh2 = wb.createSheet("Lotes");
      var rh2 = sh2.createRow(0);
      for (int i=0;i<hLot.length;i++){ var c=rh2.createCell(i); c.setCellValue(hLot[i]); c.setCellStyle(header); }
      for (int i=0;i<rowsLot.size();i++){ var r=sh2.createRow(i+1); rowsLot.get(i).write(r,dateStyle,intStyle);}
      for (int i=0;i<hLot.length;i++) sh2.autoSizeColumn(i);
      sh2.createFreezePane(0,1);

      try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) { wb.write(out); }
      wb.close();

      new Alert(Alert.AlertType.INFORMATION, "Exportado: " + f.getAbsolutePath(), ButtonType.OK).showAndWait();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al exportar:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void importarXlsx() {
    try {
      var chooser = new FileChooser();
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
      File f = chooser.showOpenDialog(getScene().getWindow());
      if (f == null) return;

      int prodUpserts = 0, lotesUpserts = 0;
      try (FileInputStream in = new FileInputStream(f); XSSFWorkbook wb = new XSSFWorkbook(in)) {
        // 1) Productos
        Sheet sp = wb.getSheet("Productos");
        if (sp != null) {
          for (int i=1;i<=sp.getLastRowNum();i++) {
            Row r = sp.getRow(i); if (r==null) continue;
            String codigo = ExcelUtil.getStringCell(r,0);
            String nombre = ExcelUtil.getStringCell(r,1);
            String unidad = ExcelUtil.getStringCell(r,2);
            Integer upc   = ExcelUtil.getIntegerCell(r,3);
            Integer stockMin = ExcelUtil.getIntegerCell(r,7);
            if (nombre==null || unidad==null) continue;

            upsertProducto(codigo, nombre, unidad, upc, stockMin==null?0:stockMin);
            prodUpserts++;
          }
        }
        // 2) Lotes
        Sheet sl = wb.getSheet("Lotes");
        if (sl != null) {
          for (int i=1;i<=sl.getLastRowNum();i++) {
            Row r = sl.getRow(i); if (r==null) continue;
            String codigo = ExcelUtil.getStringCell(r,0);
            String nombre = ExcelUtil.getStringCell(r,1);
            String fechaL = ExcelUtil.getDateAsIso(r,2);
            String fechaV = ExcelUtil.getDateAsIso(r,3);
            Integer base  = ExcelUtil.getIntegerCell(r,4);
            if ((codigo==null && nombre==null) || base==null) continue;
            upsertLoteYStock(codigo, nombre, fechaL, fechaV, base);
            lotesUpserts++;
          }
        }
      }

      new Alert(Alert.AlertType.INFORMATION,
        "Importación finalizada.\nProductos procesados: "+prodUpserts+"\nLotes procesados: "+lotesUpserts,
        ButtonType.OK).showAndWait();

    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al importar:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void upsertProducto(String codigo, String nombre, String unidad, Integer upc, int stockMin) throws Exception {
    String sel = "SELECT id FROM productos WHERE nombre = ? OR (codigo_barra IS NOT NULL AND codigo_barra = ?)";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sel)) {
      ps.setString(1, nombre);
      ps.setString(2, codigo);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          int id = rs.getInt(1);
          String upd = "UPDATE productos SET codigo_barra=?, unidad_base=?, unidades_por_caja=?, stock_minimo=?, activo=1 WHERE id=?";
          try (PreparedStatement up = c.prepareStatement(upd)) {
            up.setString(1, codigo);
            up.setString(2, unidad);
            if (upc==null) up.setNull(3, java.sql.Types.INTEGER); else up.setInt(3, upc);
            up.setInt(4, stockMin);
            up.setInt(5, id);
            up.executeUpdate();
          }
        } else {
          String ins = "INSERT INTO productos(codigo_barra,nombre,unidad_base,unidades_por_caja,stock_minimo,activo) VALUES(?,?,?,?,?,1)";
          try (PreparedStatement in = c.prepareStatement(ins)) {
            in.setString(1, codigo);
            in.setString(2, nombre);
            in.setString(3, unidad);
            if (upc==null) in.setNull(4, java.sql.Types.INTEGER); else in.setInt(4, upc);
            in.setInt(5, stockMin);
            in.executeUpdate();
          }
        }
      }
    }
  }

  private void upsertLoteYStock(String codigo, String nombre, String fechaL, String fechaV, int base) throws Exception {
    // 1) Obtener producto
    Integer productoId = null;
    String q = "SELECT id FROM productos WHERE (codigo_barra IS NOT NULL AND codigo_barra=?) OR nombre=?";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(q)) {
      ps.setString(1, codigo);
      ps.setString(2, nombre);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) productoId = rs.getInt(1);
      }
    }
    if (productoId == null) return; // skip

    // 2) Lote (fecha_lote + producto)
    Integer loteId = null;
    String ql = "SELECT id FROM lotes WHERE producto_id=? AND fecha_lote=? AND IFNULL(fecha_vencimiento,'')=IFNULL(?, '')";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(ql)) {
      ps.setInt(1, productoId);
      ps.setString(2, fechaL);
      ps.setString(3, fechaV);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) loteId = rs.getInt(1);
      }
    }
    if (loteId == null) {
      String ins = "INSERT INTO lotes(producto_id,fecha_lote,fecha_vencimiento) VALUES(?,?,?)";
      try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(ins, PreparedStatement.RETURN_GENERATED_KEYS)) {
        ps.setInt(1, productoId);
        ps.setString(2, fechaL);
        if (fechaV==null) ps.setNull(3, java.sql.Types.VARCHAR); else ps.setString(3, fechaV);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) loteId = rs.getInt(1); }
      }
    }

    // 3) Stock por lote (reemplazo)
    String del = "DELETE FROM stock_lote WHERE lote_id=?";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(del)) { ps.setInt(1, loteId); ps.executeUpdate(); }
    String ins2 = "INSERT INTO stock_lote(lote_id,cantidad_base) VALUES(?,?)";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(ins2)) {
      ps.setInt(1, loteId);
      ps.setInt(2, base);
      ps.executeUpdate();
    }
  }
}
