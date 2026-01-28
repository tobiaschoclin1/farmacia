package com.tobias.ui;

import com.tobias.db.Db;
import com.tobias.util.AppBus;
import com.tobias.util.ExcelUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockView extends BorderPane {
  public static class TotRow {
    public int productoId;
    public String codigo;
    public String nombre;
    public String unidadBase;
    public Integer unidCaja;
    public int cantidadBaseTotal;
    public Integer cajasTotal;
    public Integer tabletasTotales;
    public boolean activo;
    public int stockMinimo;
  }
  public static class LoteRow {
    public int loteId;
    public String fechaLote;
    public String fechaVto;
    public int cantidadBase;
    public Integer cajas;
    public Integer tabletasTotales;
  }

  private final ObservableList<TotRow> totales = FXCollections.observableArrayList();
  private final ObservableList<LoteRow> lotes = FXCollections.observableArrayList();

  private final TextField tfBuscar = new TextField();
  private final CheckBox chkActivos = new CheckBox("Solo activos");
  private final CheckBox chkConStock = new CheckBox("Solo con stock");
  private final ComboBox<String> cbVenc = new ComboBox<>();
  private final Button btnExport = new Button("Exportar selección XLSX");

  private TableView<TotRow> tablaTotales;
  private TableView<LoteRow> tablaLotes;

  public StockView() {
    setPadding(new Insets(10));
    setTop(buildTop());
    tablaTotales = buildTablaTotales();
    tablaLotes   = buildTablaLotes();
    var split = new SplitPane(tablaTotales, tablaLotes);
    split.setDividerPositions(0.55);
    setCenter(split);

    chkActivos.setSelected(true);
    chkConStock.setSelected(true);
    cbVenc.getItems().addAll("Todos","30 días","60 días","90 días");
    cbVenc.getSelectionModel().select("Todos");

    tfBuscar.textProperty().addListener((o,a,b)->recargarTotales());
    chkActivos.selectedProperty().addListener((o,a,b)->recargarTotales());
    chkConStock.selectedProperty().addListener((o,a,b)->recargarTotales());
    cbVenc.valueProperty().addListener((o,a,b)->recargarTotales());
    btnExport.setOnAction(e -> exportarSeleccion());

    AppBus.onStockChanged(() -> {
      TotRow sel = tablaTotales.getSelectionModel().getSelectedItem();
      recargarTotales();
      if (sel != null) {
        for (TotRow r : totales) {
          if (r.productoId == sel.productoId) {
            tablaTotales.getSelectionModel().select(r);
            recargarLotes(r.productoId, r.unidadBase, r.unidCaja);
            break;
          }
        }
      }
    });

    recargarTotales();
  }

  private HBox buildTop() {
    var hb = new HBox(8,
      new Label("Buscar:"), tfBuscar,
      chkActivos, chkConStock,
      new Label("Próx. a vencer:"), cbVenc,
      btnExport
    );
    hb.setPadding(new Insets(0,0,10,0));
    hb.getStyleClass().add("toolbar");
    return hb;
  }

  private TableView<TotRow> buildTablaTotales() {
    var t = new TableView<TotRow>(totales);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cCod = new TableColumn<TotRow, String>("Código");
    cCod.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().codigo==null?"":p.getValue().codigo));

    var cNom = new TableColumn<TotRow, String>("Producto");
    cNom.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().nombre));

    var cUni = new TableColumn<TotRow, String>("Unidad");
    cUni.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidadBase));

    var cUpc = new TableColumn<TotRow, String>("Unid/Caja");
    cUpc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidCaja==null?"-":String.valueOf(p.getValue().unidCaja)));
    cUpc.setStyle("-fx-alignment: CENTER;");

    var cBase = new TableColumn<TotRow, Number>("Cant. base total");
    cBase.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().cantidadBaseTotal));
    cBase.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cCaja = new TableColumn<TotRow, String>("Cajas total");
    cCaja.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().cajasTotal==null?"-":String.valueOf(p.getValue().cajasTotal)));
    cCaja.setStyle("-fx-alignment: CENTER;");

    var cTabs = new TableColumn<TotRow, String>("Tabletas total");
    cTabs.setCellValueFactory(p -> new SimpleStringProperty(
      p.getValue().tabletasTotales==null?"-":String.valueOf(p.getValue().tabletasTotales)
    ));
    cTabs.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cMin = new TableColumn<TotRow, String>("Stock mín.");
    cMin.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().stockMinimo)));
    cMin.setStyle("-fx-alignment: CENTER-RIGHT;");

    bindPercentWidth(cCod,  t, 0.12);
    bindPercentWidth(cNom,  t, 0.28);
    bindPercentWidth(cUni,  t, 0.10);
    bindPercentWidth(cUpc,  t, 0.10);
    bindPercentWidth(cBase, t, 0.14);
    bindPercentWidth(cCaja, t, 0.10);
    bindPercentWidth(cTabs, t, 0.08);
    bindPercentWidth(cMin,  t, 0.08);

    t.setRowFactory(tv -> new TableRow<>() {
      @Override protected void updateItem(TotRow item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) setStyle("");
        else if (item.cantidadBaseTotal < item.stockMinimo) setStyle("-fx-background-color: #ffe6e6;");
        else setStyle("");
      }
    });

    t.getColumns().setAll(cCod,cNom,cUni,cUpc,cBase,cCaja,cTabs,cMin);

    t.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{
      if (b != null) recargarLotes(b.productoId, b.unidadBase, b.unidCaja);
      else lotes.clear();
    });

    return t;
  }

  private TableView<LoteRow> buildTablaLotes() {
    var t = new TableView<LoteRow>(lotes);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    t.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    var cLote = new TableColumn<LoteRow, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto = new TableColumn<LoteRow, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVto==null?"":p.getValue().fechaVto));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cBase = new TableColumn<LoteRow, Number>("Cant. base");
    cBase.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().cantidadBase));
    cBase.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cCaja = new TableColumn<LoteRow, String>("Cajas");
    cCaja.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().cajas==null?"-":String.valueOf(p.getValue().cajas)));
    cCaja.setStyle("-fx-alignment: CENTER;");

    var cTabs = new TableColumn<LoteRow, String>("Tabletas");
    cTabs.setCellValueFactory(p -> new SimpleStringProperty(
      p.getValue().tabletasTotales==null?"-":String.valueOf(p.getValue().tabletasTotales)
    ));
    cTabs.setStyle("-fx-alignment: CENTER-RIGHT;");

    bindPercentWidth(cLote, t, 0.24);
    bindPercentWidth(cVto,  t, 0.24);
    bindPercentWidth(cBase, t, 0.22);
    bindPercentWidth(cCaja, t, 0.15);
    bindPercentWidth(cTabs, t, 0.15);

    t.getColumns().setAll(cLote,cVto,cBase,cCaja,cTabs);
    return t;
  }

  private void recargarTotales() {
    String filtro = tfBuscar.getText();
    boolean soloActivos = chkActivos.isSelected();
    boolean soloConStock = chkConStock.isSelected();
    Integer dias = switch (cbVenc.getValue()) {
      case "30 días" -> 30;
      case "60 días" -> 60;
      case "90 días" -> 90;
      default -> null;
    };
    String fechaHasta = (dias == null) ? null : LocalDate.now().plusDays(dias).toString();

    totales.clear();

    String sql = """
      SELECT p.id AS producto_id, p.codigo_barra, p.nombre, p.unidad_base, p.unidades_por_caja, p.activo, p.stock_minimo,
             COALESCE(SUM(sl.cantidad_base),0) AS cantidad_base_total
      FROM productos p
      LEFT JOIN lotes l ON l.producto_id = p.id
      LEFT JOIN stock_lote sl ON sl.lote_id = l.id
      WHERE 1=1
      """;

    StringBuilder where = new StringBuilder();
    if (filtro != null && !filtro.isBlank()) where.append(" AND (p.nombre LIKE ? OR p.codigo_barra LIKE ?)");
    if (soloActivos) where.append(" AND p.activo = 1");
    if (soloConStock) where.append(" AND COALESCE(sl.cantidad_base,0) > 0");
    if (fechaHasta != null) where.append(" AND (l.fecha_vencimiento IS NOT NULL AND l.fecha_vencimiento <= ?)");

    String end = " GROUP BY p.id, p.codigo_barra, p.nombre, p.unidad_base, p.unidades_por_caja, p.activo, p.stock_minimo ORDER BY p.nombre";

    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql + where + end)) {
      int idx = 1;
      if (filtro != null && !filtro.isBlank()) {
        String f = "%" + filtro.trim() + "%";
        ps.setString(idx++, f);
        ps.setString(idx++, f);
      }
      if (fechaHasta != null) ps.setString(idx++, fechaHasta);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          var r = new TotRow();
          r.productoId = rs.getInt("producto_id");
          r.codigo = rs.getString("codigo_barra");
          r.nombre = rs.getString("nombre");
          r.unidadBase = rs.getString("unidad_base");
          r.unidCaja = (Integer) rs.getObject("unidades_por_caja");
          r.cantidadBaseTotal = rs.getInt("cantidad_base_total");
          r.stockMinimo = rs.getInt("stock_minimo");
          if (r.unidCaja != null && r.unidCaja > 0) r.cajasTotal = r.cantidadBaseTotal / r.unidCaja;
          if ("TABLETA".equalsIgnoreCase(r.unidadBase)) r.tabletasTotales = r.cantidadBaseTotal;
          r.activo = rs.getInt("activo") == 1;
          totales.add(r);
        }
      }
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al cargar totales:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void recargarLotes(int productoId, String unidadBase, Integer upc) {
    lotes.clear();
    String sql = """
      SELECT l.id AS lote_id, l.fecha_lote, l.fecha_vencimiento,
             COALESCE(sl.cantidad_base,0) AS cantidad_base
      FROM lotes l
      LEFT JOIN stock_lote sl ON sl.lote_id = l.id
      WHERE l.producto_id = ?
      ORDER BY l.fecha_lote, l.fecha_vencimiento
    """;
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, productoId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          var r = new LoteRow();
          r.loteId = rs.getInt("lote_id");
          r.fechaLote = rs.getString("fecha_lote");
          r.fechaVto = rs.getString("fecha_vencimiento");
          r.cantidadBase = rs.getInt("cantidad_base");
          if (upc != null && upc > 0) r.cajas = r.cantidadBase / upc;
          if ("TABLETA".equalsIgnoreCase(unidadBase)) r.tabletasTotales = r.cantidadBase;
          lotes.add(r);
        }
      }
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al cargar lotes:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void exportarSeleccion() {
    try {
      List<LoteRow> seleccionLotes = new ArrayList<>(tablaLotes.getSelectionModel().getSelectedItems());
      TotRow prodSel = tablaTotales.getSelectionModel().getSelectedItem();

      if (!seleccionLotes.isEmpty() && prodSel != null) {
        var chooser = new FileChooser();
        chooser.setInitialFileName(safeFile(prodSel.nombre) + "_lotes_seleccionados.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File f = chooser.showSaveDialog(getScene().getWindow());
        if (f == null) return;

        var headersTot = new String[]{"Código","Producto","Unidad","Unid/Caja","Cant. base total","Cajas total","Tabletas total","Stock mín."};
        var rowsTot = new ArrayList<ExcelUtil.RowWriter>();
        rowsTot.add((row, dateStyle, intStyle)->{
          ExcelUtil.setString(row, 0, prodSel.codigo==null?"":prodSel.codigo);
          ExcelUtil.setString(row, 1, prodSel.nombre);
          ExcelUtil.setString(row, 2, prodSel.unidadBase);
          ExcelUtil.setInt(row, 3, prodSel.unidCaja, intStyle);
          ExcelUtil.setInt(row, 4, prodSel.cantidadBaseTotal, intStyle);
          ExcelUtil.setInt(row, 5, prodSel.cajasTotal, intStyle);
          ExcelUtil.setInt(row, 6, prodSel.tabletasTotales, intStyle);
          ExcelUtil.setInt(row, 7, prodSel.stockMinimo, intStyle);
        });

        var headersLote = new String[]{"Fecha lote","Vencimiento","Cant. base","Cajas","Tabletas"};
        var rowsLote = new ArrayList<ExcelUtil.RowWriter>();
        for (LoteRow r : seleccionLotes) {
          rowsLote.add((row, dateStyle, intStyle)->{
            ExcelUtil.setDate(row, 0, r.fechaLote, dateStyle);
            ExcelUtil.setDate(row, 1, r.fechaVto,  dateStyle);
            ExcelUtil.setInt(row, 2, r.cantidadBase, intStyle);
            ExcelUtil.setInt(row, 3, r.cajas, intStyle);
            ExcelUtil.setInt(row, 4, r.tabletasTotales, intStyle);
          });
        }

        exportTwoSheets(f, "Producto", headersTot, rowsTot, "Lotes seleccionados", headersLote, rowsLote);
        new Alert(Alert.AlertType.INFORMATION, "Exportado: " + f.getAbsolutePath(), ButtonType.OK).showAndWait();
        return;
      }

      if (prodSel != null) {
        var chooser = new FileChooser();
        chooser.setInitialFileName(safeFile(prodSel.nombre) + "_producto.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File f = chooser.showSaveDialog(getScene().getWindow());
        if (f == null) return;

        var headersTot = new String[]{"Código","Producto","Unidad","Unid/Caja","Cant. base total","Cajas total","Tabletas total","Stock mín."};
        var rowsTot = new ArrayList<ExcelUtil.RowWriter>();
        rowsTot.add((row, dateStyle, intStyle)->{
          ExcelUtil.setString(row, 0, prodSel.codigo==null?"":prodSel.codigo);
          ExcelUtil.setString(row, 1, prodSel.nombre);
          ExcelUtil.setString(row, 2, prodSel.unidadBase);
          ExcelUtil.setInt(row, 3, prodSel.unidCaja, intStyle);
          ExcelUtil.setInt(row, 4, prodSel.cantidadBaseTotal, intStyle);
          ExcelUtil.setInt(row, 5, prodSel.cajasTotal, intStyle);
          ExcelUtil.setInt(row, 6, prodSel.tabletasTotales, intStyle);
          ExcelUtil.setInt(row, 7, prodSel.stockMinimo, intStyle);
        });

        var headersLote = new String[]{"Fecha lote","Vencimiento","Cant. base","Cajas","Tabletas"};
        var rowsLote = new ArrayList<ExcelUtil.RowWriter>();
        for (LoteRow r : lotes) {
          rowsLote.add((row, dateStyle, intStyle)->{
            ExcelUtil.setDate(row, 0, r.fechaLote, dateStyle);
            ExcelUtil.setDate(row, 1, r.fechaVto,  dateStyle);
            ExcelUtil.setInt(row, 2, r.cantidadBase, intStyle);
            ExcelUtil.setInt(row, 3, r.cajas, intStyle);
            ExcelUtil.setInt(row, 4, r.tabletasTotales, intStyle);
          });
        }

        exportTwoSheets(f, "Producto", headersTot, rowsTot, "Lotes", headersLote, rowsLote);
        new Alert(Alert.AlertType.INFORMATION, "Exportado: " + f.getAbsolutePath(), ButtonType.OK).showAndWait();
        return;
      }

      new Alert(Alert.AlertType.INFORMATION, "Seleccioná un producto o algunos lotes para exportar.", ButtonType.OK).showAndWait();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al exportar:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void exportTwoSheets(File file,
                               String sheet1, String[] headers1, List<ExcelUtil.RowWriter> rows1,
                               String sheet2, String[] headers2, List<ExcelUtil.RowWriter> rows2) throws Exception {
    org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
    var df = wb.createDataFormat();
    var bold = wb.createFont(); bold.setBold(true);

    var header = wb.createCellStyle();
    header.setFont(bold);
    header.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
    header.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
    header.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);

    var zebra = wb.createCellStyle();
    zebra.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LEMON_CHIFFON.getIndex());
    zebra.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

    var dateStyle = wb.createCellStyle(); dateStyle.setDataFormat(df.getFormat("yyyy-mm-dd"));
    var intStyle  = wb.createCellStyle(); intStyle.setDataFormat(df.getFormat("0"));

    createSheet(wb, sheet1, headers1, rows1, header, zebra, dateStyle, intStyle);
    createSheet(wb, sheet2, headers2, rows2, header, zebra, dateStyle, intStyle);

    try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) { wb.write(out); }
    wb.close();
  }

  private void createSheet(org.apache.poi.ss.usermodel.Workbook wb,
                           String name, String[] headers, List<ExcelUtil.RowWriter> rows,
                           org.apache.poi.ss.usermodel.CellStyle header,
                           org.apache.poi.ss.usermodel.CellStyle zebra,
                           org.apache.poi.ss.usermodel.CellStyle dateStyle,
                           org.apache.poi.ss.usermodel.CellStyle intStyle) {
    var sh = wb.createSheet(name);
    var rh = sh.createRow(0);
    for (int c=0;c<headers.length;c++) {
      var cell = rh.createCell(c);
      cell.setCellValue(headers[c]);
      cell.setCellStyle(header);
    }
    for (int i=0;i<rows.size();i++) {
      var r = sh.createRow(i+1);
      rows.get(i).write(r, dateStyle, intStyle);
      if (i % 2 == 1) {
        for (int c=0;c<headers.length;c++) {
          var cell = r.getCell(c);
          if (cell == null) cell = r.createCell(c);
          var s = wb.createCellStyle(); s.cloneStyleFrom(cell.getCellStyle());
          s.setFillForegroundColor(zebra.getFillForegroundColor());
          s.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
          cell.setCellStyle(s);
        }
      }
    }
    for (int c=0;c<headers.length;c++) sh.autoSizeColumn(c);
    sh.createFreezePane(0,1);
    sh.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, rows.size(), 0, Math.max(0, headers.length-1)));
  }

  private static String safeFile(String s){ return s==null? "producto" : s.replaceAll("[\\\\/:*?\"<>|]", "_"); }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }
}
