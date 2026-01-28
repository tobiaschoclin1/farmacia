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

public class ExpiryView extends BorderPane {

  public static class Row {
    public int productoId;
    public int loteId;
    public String codigo;
    public String nombre;
    public String unidadBase;
    public Integer unidCaja;
    public String fechaLote;
    public String fechaVto;
    public int diasRestantes; // <0 vencido
    public int cantidadBase;
    public Integer cajas;
  }

  private final ObservableList<Row> data = FXCollections.observableArrayList();

  private final TextField tfBuscar = new TextField();
  private final CheckBox chkActivos = new CheckBox("Solo activos");
  private final CheckBox chkConStock = new CheckBox("Solo con stock");
  private final ComboBox<String> cbRango = new ComboBox<>();
  private final DatePicker dpHasta = new DatePicker();
  private final Button btnExport = new Button("Exportar XLSX");

  private TableView<Row> table;

  public ExpiryView() {
    setPadding(new Insets(10));
    setTop(buildTop());
    table = buildTable();
    setCenter(table);

    chkActivos.setSelected(true);
    chkConStock.setSelected(true);
    cbRango.getItems().addAll("30 días", "60 días", "90 días", "Personalizado…");
    cbRango.getSelectionModel().select("60 días");

    tfBuscar.textProperty().addListener((o,a,b)->recargar());
    chkActivos.selectedProperty().addListener((o,a,b)->recargar());
    chkConStock.selectedProperty().addListener((o,a,b)->recargar());
    cbRango.valueProperty().addListener((o,a,b)->{
      boolean custom = "Personalizado…".equals(b);
      dpHasta.setDisable(!custom);
      if (!custom) dpHasta.setValue(null);
      recargar();
    });
    dpHasta.valueProperty().addListener((o,a,b)-> recargar());
    btnExport.setOnAction(e -> exportar());

    // refrescar ante entradas/salidas
    AppBus.onStockChanged(this::recargar);

    recargar();
  }

  private HBox buildTop() {
    dpHasta.setPromptText("Elegir fecha");
    dpHasta.setDisable(true);

    var hb = new HBox(8,
      new Label("Buscar:"), tfBuscar,
      chkActivos, chkConStock,
      new Label("Rango:"), cbRango,
      new Label("Hasta:"), dpHasta,
      btnExport
    );
    hb.setPadding(new Insets(0,0,10,0));
    hb.getStyleClass().add("toolbar");
    return hb;
  }

  private TableView<Row> buildTable() {
    var t = new TableView<Row>(data);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cCod = new TableColumn<Row, String>("Código");
    cCod.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().codigo==null?"":p.getValue().codigo));

    var cNom = new TableColumn<Row, String>("Producto");
    cNom.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().nombre));

    var cUni = new TableColumn<Row, String>("Unidad");
    cUni.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidadBase));

    var cUPC = new TableColumn<Row, String>("Unid/Caja");
    cUPC.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidCaja==null?"-":String.valueOf(p.getValue().unidCaja)));

    var cLote = new TableColumn<Row, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));

    var cVto = new TableColumn<Row, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVto==null?"":p.getValue().fechaVto));

    var cDias = new TableColumn<Row, Number>("Días restantes");
    cDias.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().diasRestantes));

    var cBase = new TableColumn<Row, Number>("Cant. base");
    cBase.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().cantidadBase));

    var cCaja = new TableColumn<Row, String>("Cajas");
    cCaja.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().cajas==null?"-":String.valueOf(p.getValue().cajas)));

    // Alineación de columnas
    cUPC.setStyle("-fx-alignment: CENTER;");
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");
    cDias.setStyle("-fx-alignment: CENTER-RIGHT;");
    cBase.setStyle("-fx-alignment: CENTER-RIGHT;");
    cCaja.setStyle("-fx-alignment: CENTER;");

    // Porcentajes de ancho (suman 1.0)
    bindPercentWidth(cCod,  t, 0.12);
    bindPercentWidth(cNom,  t, 0.26);
    bindPercentWidth(cUni,  t, 0.10);
    bindPercentWidth(cUPC,  t, 0.08);
    bindPercentWidth(cLote, t, 0.12);
    bindPercentWidth(cVto,  t, 0.12);
    bindPercentWidth(cDias, t, 0.08);
    bindPercentWidth(cBase, t, 0.06);
    bindPercentWidth(cCaja, t, 0.06);

    // Colorear por estado de vencimiento
    t.setRowFactory(tv -> new TableRow<>() {
      @Override protected void updateItem(Row item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setStyle(""); return; }
        if (item.diasRestantes < 0)       setStyle("-fx-background-color: #ffd6d6;");
        else if (item.diasRestantes <=15) setStyle("-fx-background-color: #ffe2bf;");
        else if (item.diasRestantes <=30) setStyle("-fx-background-color: #fff7bf;");
        else                               setStyle("");
      }
    });

    t.getColumns().addAll(cCod, cNom, cUni, cUPC, cLote, cVto, cDias, cBase, cCaja);
    return t;
  }

  /** Enlaza el prefWidth de la columna a un porcentaje del ancho útil de la tabla. */
  private static void bindPercentWidth(TableColumn<?, ?> col, TableView<?> table, double pct) {
    // restamos ~18px por scrollbar/bordes para evitar salto
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }

  private void recargar() {
    try {
      String filtro = tfBuscar.getText();
      boolean soloActivos = chkActivos.isSelected();
      boolean soloConStock = chkConStock.isSelected();

      LocalDate hoy = LocalDate.now();
      LocalDate hasta;
      switch (cbRango.getValue()) {
        case "30 días" -> hasta = hoy.plusDays(30);
        case "60 días" -> hasta = hoy.plusDays(60);
        case "90 días" -> hasta = hoy.plusDays(90);
        case "Personalizado…" -> hasta = dpHasta.getValue() != null ? dpHasta.getValue() : hoy.plusDays(60);
        default -> hasta = hoy.plusDays(60);
      }

      data.clear();

      String base = """
        SELECT p.id AS producto_id, p.codigo_barra, p.nombre, p.unidad_base, p.unidades_por_caja, p.activo,
               l.id AS lote_id, l.fecha_lote, l.fecha_vencimiento,
               COALESCE(sl.cantidad_base,0) AS cantidad_base,
               CAST(julianday(l.fecha_vencimiento) - julianday(date('now')) AS INT) AS dias_restantes
        FROM productos p
        JOIN lotes l ON l.producto_id = p.id
        JOIN stock_lote sl ON sl.lote_id = l.id
        WHERE l.fecha_vencimiento IS NOT NULL
          AND date(l.fecha_vencimiento) <= date(?)
        """;

      StringBuilder where = new StringBuilder();
      if (filtro != null && !filtro.isBlank()) where.append(" AND (p.nombre LIKE ? OR p.codigo_barra LIKE ?)");
      if (soloActivos) where.append(" AND p.activo = 1");
      if (soloConStock) where.append(" AND COALESCE(sl.cantidad_base,0) > 0");

      String sql = base + where + " ORDER BY l.fecha_vencimiento, p.nombre, l.fecha_lote";

      try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
        int idx = 1;
        ps.setString(idx++, hasta.toString());
        if (filtro != null && !filtro.isBlank()) {
          String f = "%" + filtro.trim() + "%";
          ps.setString(idx++, f);
          ps.setString(idx++, f);
        }
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            var r = new Row();
            r.productoId = rs.getInt("producto_id");
            r.loteId = rs.getInt("lote_id");
            r.codigo = rs.getString("codigo_barra");
            r.nombre = rs.getString("nombre");
            r.unidadBase = rs.getString("unidad_base");
            r.unidCaja = (Integer) rs.getObject("unidades_por_caja");
            r.fechaLote = rs.getString("fecha_lote");
            r.fechaVto = rs.getString("fecha_vencimiento");
            r.cantidadBase = rs.getInt("cantidad_base");
            r.diasRestantes = rs.getInt("dias_restantes");
            if (r.unidCaja != null && r.unidCaja > 0) r.cajas = r.cantidadBase / r.unidCaja;
            data.add(r);
          }
        }
      }
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al cargar vencimientos:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void exportar() {
    try {
      if (data.isEmpty()) {
        new Alert(Alert.AlertType.INFORMATION, "No hay filas para exportar con el filtro actual.", ButtonType.OK).showAndWait();
        return;
      }
      var chooser = new FileChooser();
      chooser.setInitialFileName("proximos_a_vencer.xlsx");
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
      File f = chooser.showSaveDialog(getScene().getWindow());
      if (f == null) return;

      String[] headers = {"Código","Producto","Unidad","Unid/Caja","Fecha lote","Vencimiento","Días restantes","Cant. base","Cajas"};
      var rows = new ArrayList<ExcelUtil.RowWriter>();
      for (Row r : data) {
        rows.add((row, dateStyle, intStyle) -> {
          ExcelUtil.setString(row, 0, r.codigo==null?"":r.codigo);
          ExcelUtil.setString(row, 1, r.nombre);
          ExcelUtil.setString(row, 2, r.unidadBase);
          ExcelUtil.setInt(row, 3, r.unidCaja, intStyle);
          ExcelUtil.setDate(row, 4, r.fechaLote, dateStyle);
          ExcelUtil.setDate(row, 5, r.fechaVto,  dateStyle);
          ExcelUtil.setInt(row, 6, r.diasRestantes, intStyle);
          ExcelUtil.setInt(row, 7, r.cantidadBase, intStyle);
          ExcelUtil.setInt(row, 8, r.cajas, intStyle);
        });
      }
      ExcelUtil.exportSheet(f, "Próx. a vencer", headers, rows);
      new Alert(Alert.AlertType.INFORMATION, "Exportado: " + f.getAbsolutePath(), ButtonType.OK).showAndWait();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al exportar:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }
}
