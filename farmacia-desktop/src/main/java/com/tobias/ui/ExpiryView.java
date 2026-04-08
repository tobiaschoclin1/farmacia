package com.tobias.ui;

import com.tobias.db.Db;
import com.tobias.util.AppBus;
import com.tobias.util.ExcelUtil;
import com.tobias.util.IconHelper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    public int diasRestantes;
    public int cantidadBase;
    public Integer cajas;
  }

  private final ObservableList<Row> data = FXCollections.observableArrayList();

  private final TextField tfBuscar = new TextField();
  private final CheckBox chkActivos = new CheckBox("Solo activos");
  private final CheckBox chkConStock = new CheckBox("Solo con stock");
  private final ComboBox<String> cbRango = new ComboBox<>();
  private final DatePicker dpHasta = new DatePicker();
  private final Button btnExport = new Button(IconHelper.EXCEL + "  Exportar XLSX");

  private TableView<Row> table;

  // KPIs
  private Label lblVencidos = new Label("0");
  private Label lblPorVencer = new Label("0");
  private Label lblProximos = new Label("0");

  public ExpiryView() {
    setPadding(new Insets(0));

    var content = new VBox(16);
    content.setPadding(new Insets(0));

    content.getChildren().addAll(
      buildHeader(),
      buildKPIs(),
      buildFiltersCard(),
      buildTableCard()
    );

    var scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");

    setCenter(scroll);

    // Configuracion
    tfBuscar.setPromptText(IconHelper.SEARCH + " Buscar producto...");
    tfBuscar.getStyleClass().add("search-field");
    dpHasta.setPromptText("Elegir fecha");
    dpHasta.setDisable(true);

    chkActivos.setSelected(true);
    chkConStock.setSelected(true);
    cbRango.getItems().addAll("30 dias", "60 dias", "90 dias", "Personalizado");
    cbRango.getSelectionModel().select("60 dias");

    btnExport.getStyleClass().add("secondary");

    tfBuscar.textProperty().addListener((o,a,b)->recargar());
    chkActivos.selectedProperty().addListener((o,a,b)->recargar());
    chkConStock.selectedProperty().addListener((o,a,b)->recargar());
    cbRango.valueProperty().addListener((o,a,b)->{
      boolean custom = "Personalizado".equals(b);
      dpHasta.setDisable(!custom);
      if (!custom) dpHasta.setValue(null);
      recargar();
    });
    dpHasta.valueProperty().addListener((o,a,b)-> recargar());
    btnExport.setOnAction(e -> exportar());

    AppBus.onStockChanged(this::recargar);

    recargar();
  }

  private Node buildHeader() {
    var icon = IconHelper.emoji(IconHelper.ALERT, 32);
    var title = new Label("Productos Proximos a Vencer");
    title.getStyleClass().add("page-title");

    var subtitle = new Label("Monitorea los productos que estan por vencer o ya vencieron");
    subtitle.getStyleClass().add("page-subtitle");

    var headerText = new VBox(4, title, subtitle);

    var header = new HBox(12, icon, headerText);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getStyleClass().add("page-header");
    return header;
  }

  private Node buildKPIs() {
    var grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(16);

    var kpi1 = buildKPICard("Vencidos", lblVencidos, "danger");
    var kpi2 = buildKPICard("Por Vencer (15 dias)", lblPorVencer, "warning");
    var kpi3 = buildKPICard("Proximos (30 dias)", lblProximos, "info");

    grid.add(kpi1, 0, 0);
    grid.add(kpi2, 1, 0);
    grid.add(kpi3, 2, 0);

    for (int i = 0; i < 3; i++) {
      var col = new ColumnConstraints();
      col.setHgrow(Priority.ALWAYS);
      col.setPercentWidth(33.33);
      grid.getColumnConstraints().add(col);
    }

    return grid;
  }

  private Node buildKPICard(String label, Label valueLabel, String type) {
    var card = new VBox(8);
    card.getStyleClass().add("metric-card");
    card.setAlignment(Pos.CENTER_LEFT);

    var lblTitle = new Label(label);
    lblTitle.getStyleClass().add("metric-label");

    valueLabel.getStyleClass().add("metric-value");

    switch(type) {
      case "success" -> valueLabel.setStyle("-fx-text-fill: #10B981;");
      case "warning" -> valueLabel.setStyle("-fx-text-fill: #F59E0B;");
      case "danger" -> valueLabel.setStyle("-fx-text-fill: #EF4444;");
      case "info" -> valueLabel.setStyle("-fx-text-fill: #2563EB;");
    }

    card.getChildren().addAll(lblTitle, valueLabel);
    return card;
  }

  private Node buildFiltersCard() {
    var card = new HBox(12);
    card.getStyleClass().add("card");
    card.setAlignment(Pos.CENTER_LEFT);

    var spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    card.getChildren().addAll(
      tfBuscar,
      chkActivos,
      chkConStock,
      new Label("Rango:"),
      cbRango,
      new Label("Hasta:"),
      dpHasta,
      spacer,
      btnExport
    );

    return card;
  }

  private Node buildTableCard() {
    var card = new VBox(12);
    card.getStyleClass().add("card");
    VBox.setVgrow(card, Priority.ALWAYS);

    table = buildTable();
    VBox.setVgrow(table, Priority.ALWAYS);

    card.getChildren().add(table);
    return card;
  }

  private TableView<Row> buildTable() {
    var t = new TableView<Row>(data);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cCod = new TableColumn<Row, String>("Codigo");
    cCod.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().codigo==null?"":p.getValue().codigo));

    var cNom = new TableColumn<Row, String>("Producto");
    cNom.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().nombre));

    var cUni = new TableColumn<Row, String>("Unidad");
    cUni.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidadBase));

    var cUPC = new TableColumn<Row, String>("Unid/Caja");
    cUPC.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidCaja==null?"-":String.valueOf(p.getValue().unidCaja)));
    cUPC.setStyle("-fx-alignment: CENTER;");

    var cLote = new TableColumn<Row, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto = new TableColumn<Row, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVto==null?"":p.getValue().fechaVto));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cDias = new TableColumn<Row, Number>("Dias restantes");
    cDias.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().diasRestantes));
    cDias.setStyle("-fx-alignment: CENTER-RIGHT;");

    // Colorear la celda de días
    cDias.setCellFactory(col -> new TableCell<Row, Number>() {
      @Override
      protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setStyle("");
        } else {
          int dias = item.intValue();
          setText(String.valueOf(dias));

          if (dias < 0) {
            setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700;");
            setText(dias + " (VENCIDO)");
          } else if (dias <= 15) {
            setStyle("-fx-text-fill: #D97706; -fx-font-weight: 700;");
          } else if (dias <= 30) {
            setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: 600;");
          } else {
            setStyle("-fx-text-fill: #6B7280;");
          }
        }
      }
    });

    var cBase = new TableColumn<Row, Number>("Cant. base");
    cBase.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().cantidadBase));
    cBase.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cCaja = new TableColumn<Row, String>("Cajas");
    cCaja.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().cajas==null?"-":String.valueOf(p.getValue().cajas)));
    cCaja.setStyle("-fx-alignment: CENTER;");

    bindPercentWidth(cCod,  t, 0.10);
    bindPercentWidth(cNom,  t, 0.24);
    bindPercentWidth(cUni,  t, 0.08);
    bindPercentWidth(cUPC,  t, 0.08);
    bindPercentWidth(cLote, t, 0.12);
    bindPercentWidth(cVto,  t, 0.12);
    bindPercentWidth(cDias, t, 0.12);
    bindPercentWidth(cBase, t, 0.08);
    bindPercentWidth(cCaja, t, 0.06);

    t.setRowFactory(tv -> new TableRow<>() {
      @Override protected void updateItem(Row item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("expired", "expiring-very-soon", "expiring-soon");

        if (empty || item == null) {
          setStyle("");
        } else {
          if (item.diasRestantes < 0) {
            getStyleClass().add("expired");
          } else if (item.diasRestantes <=15) {
            getStyleClass().add("expiring-very-soon");
          } else if (item.diasRestantes <=30) {
            getStyleClass().add("expiring-soon");
          } else {
            setStyle("");
          }
        }
      }
    });

    t.getColumns().addAll(java.util.List.of(cCod, cNom, cUni, cUPC, cLote, cVto, cDias, cBase, cCaja));
    return t;
  }

  private void recargar() {
    try {
      String filtro = tfBuscar.getText();
      boolean soloActivos = chkActivos.isSelected();
      boolean soloConStock = chkConStock.isSelected();

      LocalDate hoy = LocalDate.now();
      LocalDate hasta;
      switch (cbRango.getValue()) {
        case "30 dias" -> hasta = hoy.plusDays(30);
        case "60 dias" -> hasta = hoy.plusDays(60);
        case "90 dias" -> hasta = hoy.plusDays(90);
        case "Personalizado" -> hasta = dpHasta.getValue() != null ? dpHasta.getValue() : hoy.plusDays(60);
        default -> hasta = hoy.plusDays(60);
      }

      data.clear();

      int vencidos = 0;
      int porVencer15 = 0;
      int proximos30 = 0;

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

            // Calcular KPIs
            if (r.diasRestantes < 0) {
              vencidos++;
            } else if (r.diasRestantes <= 15) {
              porVencer15++;
            } else if (r.diasRestantes <= 30) {
              proximos30++;
            }

            data.add(r);
          }
        }
      }

      // Actualizar KPIs
      lblVencidos.setText(String.valueOf(vencidos));
      lblPorVencer.setText(String.valueOf(porVencer15));
      lblProximos.setText(String.valueOf(proximos30));

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

      String[] headers = {"Codigo","Producto","Unidad","Unid/Caja","Fecha lote","Vencimiento","Dias restantes","Cant. base","Cajas"};
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
      ExcelUtil.exportSheet(f, "Prox. a vencer", headers, rows);
      new Alert(Alert.AlertType.INFORMATION, "Exportado: " + f.getAbsolutePath(), ButtonType.OK).showAndWait();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al exportar:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }
}
