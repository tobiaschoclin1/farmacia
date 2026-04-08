package com.tobias.ui;

import com.tobias.db.Db;
import com.tobias.util.AppBus;
import com.tobias.util.ExcelUtil;
import com.tobias.util.Icons;
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
  private final Button btnExport = new Button("Exportar XLSX");

  private TableView<TotRow> tablaTotales;
  private TableView<LoteRow> tablaLotes;

  // KPIs
  private Label lblTotalProductos = new Label("0");
  private Label lblEnStock = new Label("0");
  private Label lblBajoStock = new Label("0");
  private Label lblSinStock = new Label("0");

  public StockView() {
    setPadding(new Insets(0));

    var content = new VBox(16);
    content.setPadding(new Insets(0));

    content.getChildren().addAll(
      buildHeader(),
      buildKPIs(),
      buildFiltersCard(),
      buildTablesCard()
    );

    var scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");

    setCenter(scroll);

    // Configuracion
    tfBuscar.setPromptText("Buscar producto por nombre o codigo...");
    tfBuscar.getStyleClass().add("search-field");

    chkActivos.setSelected(true);
    chkConStock.setSelected(true);
    cbVenc.getItems().addAll("Todos","30 dias","60 dias","90 dias");
    cbVenc.getSelectionModel().select("Todos");

    // Configurar icono en boton
    btnExport.setGraphic(Icons.arrowDownOnSquare(16));

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

  private Node buildHeader() {
    var title = new Label("Inventario de Stock");
    title.getStyleClass().add("page-title");

    var subtitle = new Label("Visualiza el stock actual de todos los productos");
    subtitle.getStyleClass().add("page-subtitle");

    var headerText = new VBox(4, title, subtitle);
    headerText.getStyleClass().add("page-header");

    return headerText;
  }

  private Node buildKPIs() {
    var grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(16);

    // KPI 1: Total Productos
    var kpi1 = buildKPICard("Total Productos", lblTotalProductos, "info");

    // KPI 2: En Stock
    var kpi2 = buildKPICard("En Stock", lblEnStock, "success");

    // KPI 3: Stock Bajo
    var kpi3 = buildKPICard("Stock Bajo", lblBajoStock, "warning");

    // KPI 4: Sin Stock
    var kpi4 = buildKPICard("Sin Stock", lblSinStock, "danger");

    grid.add(kpi1, 0, 0);
    grid.add(kpi2, 1, 0);
    grid.add(kpi3, 2, 0);
    grid.add(kpi4, 3, 0);

    // Configurar columnas para que se expandan igualmente
    for (int i = 0; i < 4; i++) {
      var col = new ColumnConstraints();
      col.setHgrow(Priority.ALWAYS);
      col.setPercentWidth(25);
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

    // Colorear segun tipo
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

    btnExport.getStyleClass().add("secondary");

    card.getChildren().addAll(
      tfBuscar,
      chkActivos,
      chkConStock,
      new Label("Prox. a vencer:"),
      cbVenc,
      spacer,
      btnExport
    );

    return card;
  }

  private Node buildTablesCard() {
    var card = new VBox(12);
    card.getStyleClass().add("card");
    VBox.setVgrow(card, Priority.ALWAYS);

    var titleLeft = new Label("Productos - Stock Total");
    titleLeft.getStyleClass().add("card-title");

    var titleRight = new Label("Lotes del Producto Seleccionado");
    titleRight.getStyleClass().add("card-title");

    tablaTotales = buildTablaTotales();
    tablaLotes = buildTablaLotes();

    var leftPanel = new VBox(8, titleLeft, tablaTotales);
    VBox.setVgrow(tablaTotales, Priority.ALWAYS);

    var rightPanel = new VBox(8, titleRight, tablaLotes);
    VBox.setVgrow(tablaLotes, Priority.ALWAYS);

    var split = new SplitPane(leftPanel, rightPanel);
    split.setDividerPositions(0.55);
    VBox.setVgrow(split, Priority.ALWAYS);

    card.getChildren().add(split);
    return card;
  }

  private TableView<TotRow> buildTablaTotales() {
    var t = new TableView<TotRow>(totales);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cCod = new TableColumn<TotRow, String>("Codigo");
    cCod.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().codigo==null?"":p.getValue().codigo));

    var cNom = new TableColumn<TotRow, String>("Producto");
    cNom.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().nombre));

    var cUni = new TableColumn<TotRow, String>("Unidad");
    cUni.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidadBase));

    var cUpc = new TableColumn<TotRow, String>("Unid/Caja");
    cUpc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().unidCaja==null?"-":String.valueOf(p.getValue().unidCaja)));
    cUpc.setStyle("-fx-alignment: CENTER;");

    var cBase = new TableColumn<TotRow, Number>("Cant. Total");
    cBase.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().cantidadBaseTotal));
    cBase.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cCaja = new TableColumn<TotRow, String>("Cajas");
    cCaja.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().cajasTotal==null?"-":String.valueOf(p.getValue().cajasTotal)));
    cCaja.setStyle("-fx-alignment: CENTER;");

    var cTabs = new TableColumn<TotRow, String>("Tabletas");
    cTabs.setCellValueFactory(p -> new SimpleStringProperty(
      p.getValue().tabletasTotales==null?"-":String.valueOf(p.getValue().tabletasTotales)
    ));
    cTabs.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cMin = new TableColumn<TotRow, String>("Stock Min");
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
        if (empty || item == null) {
          setStyle("");
          getStyleClass().removeAll("low-stock", "medium-stock");
        } else {
          getStyleClass().removeAll("low-stock", "medium-stock");
          if (item.cantidadBaseTotal == 0) {
            getStyleClass().add("low-stock");
          } else if (item.cantidadBaseTotal < item.stockMinimo) {
            getStyleClass().add("medium-stock");
          } else {
            setStyle("");
          }
        }
      }
    });

    t.getColumns().setAll(List.of(cCod,cNom,cUni,cUpc,cBase,cCaja,cTabs,cMin));

    t.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{
      if (b != null) recargarLotes(b.productoId, b.unidadBase, b.unidCaja);
      else lotes.clear();
    });

    return t;
  }

  private TableView<LoteRow> buildTablaLotes() {
    var t = new TableView<LoteRow>(lotes);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    t.setPlaceholder(new Label("Selecciona un producto para ver sus lotes"));

    var cLote = new TableColumn<LoteRow, String>("Fecha Lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto = new TableColumn<LoteRow, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVto==null?"":p.getValue().fechaVto));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cBase = new TableColumn<LoteRow, Number>("Cantidad");
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

    t.getColumns().setAll(List.of(cLote,cVto,cBase,cCaja,cTabs));
    return t;
  }

  private void recargarTotales() {
    String filtro = tfBuscar.getText();
    boolean soloActivos = chkActivos.isSelected();
    boolean soloConStock = chkConStock.isSelected();
    Integer dias = switch (cbVenc.getValue()) {
      case "30 dias" -> 30;
      case "60 dias" -> 60;
      case "90 dias" -> 90;
      default -> null;
    };

    totales.clear();
    lotes.clear();

    int totalCount = 0;
    int enStockCount = 0;
    int bajoStockCount = 0;
    int sinStockCount = 0;

    String base = """
      SELECT p.id, p.codigo_barra, p.nombre, p.unidad_base, p.unidades_por_caja, p.stock_minimo,
             COALESCE(SUM(sl.cantidad_base), 0) AS total_base
      FROM productos p
      LEFT JOIN lotes l ON l.producto_id = p.id
      LEFT JOIN stock_lote sl ON sl.lote_id = l.id
      """;

    StringBuilder where = new StringBuilder(" WHERE 1=1 ");
    if (filtro != null && !filtro.isBlank())
      where.append(" AND (p.nombre LIKE ? OR p.codigo_barra LIKE ?)");
    if (soloActivos) where.append(" AND p.activo = 1");

    if (dias != null) {
      where.append(" AND l.fecha_vencimiento IS NOT NULL AND date(l.fecha_vencimiento) <= date('now', '+").append(dias).append(" days')");
    }

    String sql = base + where + " GROUP BY p.id ORDER BY p.nombre";

    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      int idx = 1;
      if (filtro != null && !filtro.isBlank()) {
        String f = "%" + filtro.trim() + "%";
        ps.setString(idx++, f);
        ps.setString(idx++, f);
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          var r = new TotRow();
          r.productoId = rs.getInt("id");
          r.codigo = rs.getString("codigo_barra");
          r.nombre = rs.getString("nombre");
          r.unidadBase = rs.getString("unidad_base");
          r.unidCaja = (Integer) rs.getObject("unidades_por_caja");
          r.cantidadBaseTotal = rs.getInt("total_base");
          r.stockMinimo = rs.getInt("stock_minimo");

          if (r.unidCaja != null && r.unidCaja > 0) {
            r.cajasTotal = r.cantidadBaseTotal / r.unidCaja;
            r.tabletasTotales = r.cantidadBaseTotal % r.unidCaja;
          }

          if (soloConStock && r.cantidadBaseTotal == 0) continue;

          // Calcular KPIs
          totalCount++;
          if (r.cantidadBaseTotal == 0) {
            sinStockCount++;
          } else if (r.cantidadBaseTotal < r.stockMinimo) {
            bajoStockCount++;
          } else {
            enStockCount++;
          }

          totales.add(r);
        }
      }
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al cargar stock:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }

    // Actualizar KPIs
    lblTotalProductos.setText(String.valueOf(totalCount));
    lblEnStock.setText(String.valueOf(enStockCount));
    lblBajoStock.setText(String.valueOf(bajoStockCount));
    lblSinStock.setText(String.valueOf(sinStockCount));
  }

  private void recargarLotes(int productoId, String unidadBase, Integer unidCaja) {
    lotes.clear();
    String sql = """
      SELECT l.id, l.fecha_lote, l.fecha_vencimiento, COALESCE(sl.cantidad_base,0) AS cant
      FROM lotes l
      LEFT JOIN stock_lote sl ON sl.lote_id = l.id
      WHERE l.producto_id = ?
      ORDER BY l.fecha_vencimiento NULLS LAST, l.fecha_lote
      """;

    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, productoId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          var r = new LoteRow();
          r.loteId = rs.getInt("id");
          r.fechaLote = rs.getString("fecha_lote");
          r.fechaVto = rs.getString("fecha_vencimiento");
          r.cantidadBase = rs.getInt("cant");
          if (unidCaja != null && unidCaja > 0) {
            r.cajas = r.cantidadBase / unidCaja;
            r.tabletasTotales = r.cantidadBase % unidCaja;
          }
          lotes.add(r);
        }
      }
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al cargar lotes:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void exportarSeleccion() {
    try {
      var sel = tablaTotales.getSelectionModel().getSelectedItem();
      if (sel == null) {
        new Alert(Alert.AlertType.INFORMATION, "Selecciona un producto para exportar sus lotes.", ButtonType.OK).showAndWait();
        return;
      }
      if (lotes.isEmpty()) {
        new Alert(Alert.AlertType.INFORMATION, "No hay lotes para exportar.", ButtonType.OK).showAndWait();
        return;
      }

      var chooser = new FileChooser();
      chooser.setInitialFileName("stock_" + sel.nombre.replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx");
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
      File f = chooser.showSaveDialog(getScene().getWindow());
      if (f == null) return;

      String[] headers = {"Fecha Lote","Vencimiento","Cantidad Base","Cajas","Tabletas"};
      var rows = new ArrayList<ExcelUtil.RowWriter>();
      for (LoteRow r : lotes) {
        rows.add((row, dateStyle, intStyle) -> {
          ExcelUtil.setDate(row, 0, r.fechaLote, dateStyle);
          ExcelUtil.setDate(row, 1, r.fechaVto, dateStyle);
          ExcelUtil.setInt(row, 2, r.cantidadBase, intStyle);
          ExcelUtil.setInt(row, 3, r.cajas, intStyle);
          ExcelUtil.setInt(row, 4, r.tabletasTotales, intStyle);
        });
      }
      ExcelUtil.exportSheet(f, sel.nombre, headers, rows);
      new Alert(Alert.AlertType.INFORMATION, "Exportado: " + f.getAbsolutePath(), ButtonType.OK).showAndWait();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al exportar:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }
}
