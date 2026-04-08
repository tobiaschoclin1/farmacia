package com.tobias.ui;

import com.tobias.dao.ProductDao;
import com.tobias.model.Product;
import com.tobias.service.StockService;
import com.tobias.service.StockService.SalidaAsignacion;
import com.tobias.util.AppBus;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tobias.db.Db;

public class StockOutView extends BorderPane {

  private final ProductDao productDao = new ProductDao();
  private final StockService stockService = new StockService();

  private final TextField tfCodigo = new TextField();
  private final Button btnBuscarCod = new Button("Buscar");

  private final TextField tfBuscarNombre = new TextField();
  private final Button btnBuscarNom = new Button("Buscar");

  private final Label lbNombre = new Label("-");
  private final Label lbUnidad = new Label("-");
  private final Label lbUnidCaja = new Label("-");
  private final TextField tfMotivo = new TextField("Venta / Consumo");

  private Product productoSel = null;

  public static class LoteRow {
    int loteId;
    String fechaLote;
    String fechaVto;
    int stockBase;
    Integer cajas;
  }

  private final ObservableList<LoteRow> lotes = FXCollections.observableArrayList();
  private TableView<LoteRow> tablaLotes;

  private final TextField tfCantidadBase = new TextField();
  private final Button btnAgregar = new Button("Agregar desde lote");

  private final ObservableList<SalidaAsignacion> seleccion = FXCollections.observableArrayList();
  private TableView<SalidaAsignacion> tablaSeleccion;
  private final Button btnQuitar = new Button("Quitar");
  private final Button btnConfirmar = new Button("Confirmar salida");

  public StockOutView() {
    setPadding(new Insets(0));

    var content = new VBox(16);
    content.setPadding(new Insets(0));

    content.getChildren().addAll(
      buildHeader(),
      buildProductSelector(),
      buildTablesCard(),
      buildActions()
    );

    var scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");

    setCenter(scroll);

    // Configuracion
    tfCodigo.setPromptText("Codigo de barras");
    tfBuscarNombre.setPromptText("Buscar por nombre...");
    tfCantidadBase.setPromptText("Cantidad");
    tfMotivo.setPromptText("Motivo de la salida");

    btnBuscarCod.getStyleClass().add("secondary");
    btnBuscarNom.getStyleClass().add("secondary");
    btnAgregar.getStyleClass().add("success");
    btnQuitar.getStyleClass().add("secondary");
    btnConfirmar.getStyleClass().add("primary");

    // Configurar iconos en botones
    btnBuscarCod.setGraphic(Icons.magnifyingGlass(16));
    btnBuscarNom.setGraphic(Icons.magnifyingGlass(16));
    btnAgregar.setGraphic(Icons.plus(16));
    btnQuitar.setGraphic(Icons.xMark(16));
    btnConfirmar.setGraphic(Icons.check(16));

    onlyInts(tfCantidadBase);

    btnBuscarCod.setOnAction(e -> buscarProductoPorCodigo());
    btnBuscarNom.setOnAction(e -> buscarProductoPorNombre());
    btnAgregar.setOnAction(e -> agregarDesdeLote());
    btnQuitar.setOnAction(e -> {
      var s = tablaSeleccion.getSelectionModel().getSelectedItem();
      if (s != null) seleccion.remove(s);
    });
    btnConfirmar.setOnAction(e -> confirmarSalida());
  }

  private Node buildHeader() {
    var title = new Label("Registro de Salidas");
    title.getStyleClass().add("page-title");

    var subtitle = new Label("Registra salidas de stock del inventario (ventas, consumos, perdidas)");
    subtitle.getStyleClass().add("page-subtitle");

    var headerText = new VBox(4, title, subtitle);
    headerText.getStyleClass().add("page-header");

    return headerText;
  }

  private Node buildProductSelector() {
    var card = new VBox(16);
    card.getStyleClass().add("card");

    var cardTitle = new Label("1. Seleccionar Producto");
    cardTitle.getStyleClass().add("card-title");
    cardTitle.setStyle("-fx-font-size: 18px;");

    var grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(12);

    var col1 = new ColumnConstraints();
    col1.setMinWidth(180);
    var col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().addAll(col1, col2);

    int r = 0;

    var lblCodigo = new Label("Codigo de barras");
    lblCodigo.getStyleClass().add("form-label");
    var hbCod = new HBox(8, tfCodigo, btnBuscarCod);
    HBox.setHgrow(tfCodigo, Priority.ALWAYS);
    grid.add(lblCodigo, 0, r);
    grid.add(hbCod, 1, r++);

    var lblNombre = new Label("Buscar por nombre");
    lblNombre.getStyleClass().add("form-label");
    var hbNom = new HBox(8, tfBuscarNombre, btnBuscarNom);
    HBox.setHgrow(tfBuscarNombre, Priority.ALWAYS);
    grid.add(lblNombre, 0, r);
    grid.add(hbNom, 1, r++);

    var sep = new Separator();
    grid.add(sep, 0, r++, 2, 1);

    var lblProdSel = new Label("Producto seleccionado");
    lblProdSel.getStyleClass().add("form-label");
    lbNombre.setStyle("-fx-font-weight: 600; -fx-text-fill: #2563EB;");
    grid.add(lblProdSel, 0, r);
    grid.add(lbNombre, 1, r++);

    var lblUnidBase = new Label("Unidad base");
    lblUnidBase.getStyleClass().add("form-label");
    grid.add(lblUnidBase, 0, r);
    grid.add(lbUnidad, 1, r++);

    var lblUnidCaja = new Label("Unidades por caja");
    lblUnidCaja.getStyleClass().add("form-label");
    grid.add(lblUnidCaja, 0, r);
    grid.add(lbUnidCaja, 1, r++);

    var lblMotivo = new Label("Motivo de la salida");
    lblMotivo.getStyleClass().add("form-label");
    grid.add(lblMotivo, 0, r);
    grid.add(tfMotivo, 1, r++);

    card.getChildren().addAll(cardTitle, grid);
    return card;
  }

  private Node buildTablesCard() {
    var card = new VBox(12);
    card.getStyleClass().add("card");
    VBox.setVgrow(card, Priority.ALWAYS);

    var cardTitle = new Label("2. Seleccionar Lotes");
    cardTitle.getStyleClass().add("card-title");
    cardTitle.setStyle("-fx-font-size: 18px;");

    tablaLotes = buildTablaLotes();
    tablaSeleccion = buildTablaSeleccion();

    var leftPanel = new VBox(8);
    var leftTitle = new Label("Lotes Disponibles (FEFO - Primero en vencer, primero en salir)");
    leftTitle.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");

    var addBar = new HBox(12, new Label("Cantidad:"), tfCantidadBase, btnAgregar);
    addBar.setAlignment(Pos.CENTER_LEFT);
    addBar.setPadding(new Insets(8, 0, 0, 0));

    leftPanel.getChildren().addAll(leftTitle, tablaLotes, addBar);
    VBox.setVgrow(tablaLotes, Priority.ALWAYS);

    var rightPanel = new VBox(8);
    var rightTitle = new Label("Items Seleccionados para Salida");
    rightTitle.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");

    var removeBar = new HBox(12, btnQuitar);
    removeBar.setAlignment(Pos.CENTER_LEFT);
    removeBar.setPadding(new Insets(8, 0, 0, 0));

    rightPanel.getChildren().addAll(rightTitle, tablaSeleccion, removeBar);
    VBox.setVgrow(tablaSeleccion, Priority.ALWAYS);

    var split = new SplitPane(leftPanel, rightPanel);
    split.setDividerPositions(0.55);
    VBox.setVgrow(split, Priority.ALWAYS);

    card.getChildren().addAll(cardTitle, split);
    return card;
  }

  private TableView<LoteRow> buildTablaLotes() {
    var t = new TableView<LoteRow>(lotes);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    t.setPlaceholder(new Label("Selecciona un producto para ver sus lotes disponibles"));

    var cLote = new TableColumn<LoteRow, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto  = new TableColumn<LoteRow, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVto==null?"Sin vencimiento":p.getValue().fechaVto));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cStock = new TableColumn<LoteRow, Number>("Stock");
    cStock.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().stockBase));
    cStock.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cCajas = new TableColumn<LoteRow, String>("Cajas");
    cCajas.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().cajas==null?"-":String.valueOf(p.getValue().cajas)));
    cCajas.setStyle("-fx-alignment: CENTER;");

    bindPercentWidth(cLote, t, 0.28);
    bindPercentWidth(cVto,  t, 0.32);
    bindPercentWidth(cStock,t, 0.22);
    bindPercentWidth(cCajas,t, 0.18);

    t.getColumns().setAll(List.of(cLote, cVto, cStock, cCajas));

    t.setRowFactory(tv -> new TableRow<>() {
      @Override protected void updateItem(LoteRow item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("expired", "expiring-very-soon");
        if (empty || item == null) {
          setStyle("");
        } else if (item.fechaVto != null && !item.fechaVto.isBlank()) {
          if (item.fechaVto.compareTo(java.time.LocalDate.now().toString()) < 0) {
            getStyleClass().add("expired");
          } else if (item.fechaVto.compareTo(java.time.LocalDate.now().plusDays(30).toString()) < 0) {
            getStyleClass().add("expiring-very-soon");
          } else {
            setStyle("");
          }
        }
      }
    });

    return t;
  }

  private TableView<SalidaAsignacion> buildTablaSeleccion() {
    var t = new TableView<>(seleccion);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    t.setPlaceholder(new Label("Agrega lotes para registrar la salida"));

    var cLote = new TableColumn<SalidaAsignacion, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto = new TableColumn<SalidaAsignacion, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVencimiento==null?"Sin vencimiento":p.getValue().fechaVencimiento));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cCant = new TableColumn<SalidaAsignacion, Number>("Cantidad");
    cCant.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().cantidadBase));
    cCant.setStyle("-fx-alignment: CENTER-RIGHT;");

    bindPercentWidth(cLote, t, 0.35);
    bindPercentWidth(cVto,  t, 0.35);
    bindPercentWidth(cCant, t, 0.30);

    t.getColumns().setAll(List.of(cLote, cVto, cCant));
    return t;
  }

  private Node buildActions() {
    var card = new HBox(12);
    card.getStyleClass().add("card");
    card.setAlignment(Pos.CENTER_RIGHT);

    var lblTotal = new Label("Total de items:");
    lblTotal.setStyle("-fx-font-weight: 600;");

    var lblCount = new Label("0");
    lblCount.textProperty().bind(javafx.beans.binding.Bindings.size(seleccion).asString());
    lblCount.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #2563EB;");

    var spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    card.getChildren().addAll(lblTotal, lblCount, spacer, btnConfirmar);
    return card;
  }

  private void buscarProductoPorCodigo() {
    try {
      String codigo = tfCodigo.getText();
      if (codigo==null || codigo.isBlank()) { alert("Ingresa un codigo de barras."); return; }
      var p = productDao.findByCodigoBarra(codigo.trim());
      if (p==null) { alert("No existe un producto con ese codigo."); limpiarProductoSel(); return; }
      setProductoSel(p);
    } catch (Exception ex) { alert("Error al buscar: " + ex.getMessage()); }
  }

  private void buscarProductoPorNombre() {
    try {
      String filtro = tfBuscarNombre.getText();
      if (filtro==null || filtro.isBlank()) { alert("Ingresa parte del nombre."); return; }
      List<Product> lista = productDao.findAll(filtro.trim());
      if (lista.isEmpty()) { alert("No hay productos que coincidan."); return; }
      if (lista.size()==1) { setProductoSel(lista.get(0)); return; }
      ChoiceDialog<String> dlg = new ChoiceDialog<>();
      dlg.setTitle("Seleccionar producto");
      dlg.setHeaderText("Coincidencias para: " + filtro);
      dlg.getItems().addAll(lista.stream().map(Product::getNombre).collect(Collectors.toList()));
      dlg.setSelectedItem(dlg.getItems().get(0));
      Optional<String> res = dlg.showAndWait();
      res.ifPresent(sel -> lista.stream().filter(p->p.getNombre().equals(sel)).findFirst().ifPresent(this::setProductoSel));
    } catch (Exception ex) { alert("Error al buscar: " + ex.getMessage()); }
  }

  private void setProductoSel(Product p) {
    productoSel = p;
    lbNombre.setText(p.getNombre());
    lbUnidad.setText(p.getUnidadBase());
    lbUnidCaja.setText(p.getUnidadesPorCaja()==null? "-" : String.valueOf(p.getUnidadesPorCaja()));
    seleccion.clear();
    cargarLotes(p);
  }

  private void limpiarProductoSel() {
    productoSel = null;
    lbNombre.setText("-"); lbUnidad.setText("-"); lbUnidCaja.setText("-");
    lotes.clear(); seleccion.clear();
  }

  private void cargarLotes(Product p) {
    lotes.clear();
    String sql = """
      SELECT l.id AS lote_id, l.fecha_lote, l.fecha_vencimiento,
             COALESCE(sl.cantidad_base,0) AS stock_base
      FROM lotes l
      LEFT JOIN stock_lote sl ON sl.lote_id = l.id
      WHERE l.producto_id = ?
      ORDER BY l.fecha_vencimiento IS NULL, l.fecha_vencimiento, l.fecha_lote, l.id
    """;
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, p.getId());
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          var r = new LoteRow();
          r.loteId    = rs.getInt("lote_id");
          r.fechaLote = rs.getString("fecha_lote");
          r.fechaVto  = rs.getString("fecha_vencimiento");
          r.stockBase = rs.getInt("stock_base");
          if (p.getUnidadesPorCaja()!=null && p.getUnidadesPorCaja()>0)
            r.cajas = r.stockBase / p.getUnidadesPorCaja();
          lotes.add(r);
        }
      }
    } catch (Exception ex) {
      alert("Error al cargar lotes:\n" + ex.getMessage());
    }
  }

  private void agregarDesdeLote() {
    var lote = tablaLotes.getSelectionModel().getSelectedItem();
    if (productoSel == null) { alert("Selecciona un producto."); return; }
    if (lote == null) { alert("Elige un lote de la tabla izquierda."); return; }
    int cant = parseInt(tfCantidadBase.getText());
    if (cant <= 0) { alert("Ingresa una cantidad (>0)."); return; }
    if (cant > lote.stockBase) { alert("Cantidad supera el stock del lote."); return; }

    for (var a : seleccion) {
      if (a.loteId == lote.loteId) {
        if (a.cantidadBase + cant > lote.stockBase) {
          alert("La suma excede el stock del lote.");
          return;
        }
        a.cantidadBase += cant;
        tablaSeleccion.refresh();
        tfCantidadBase.clear();
        return;
      }
    }

    seleccion.add(new SalidaAsignacion(lote.loteId, cant, lote.fechaLote, lote.fechaVto));
    tfCantidadBase.clear();
  }

  private void confirmarSalida() {
    if (productoSel == null) { alert("Selecciona un producto."); return; }
    if (seleccion.isEmpty()) { alert("No hay lotes seleccionados."); return; }

    for (var a : seleccion) {
      int stockActual = obtenerStockLote(a.loteId);
      if (a.cantidadBase > stockActual) {
        alert("El lote con fecha " + (a.fechaLote==null? "—" : a.fechaLote) +
              " no tiene suficiente stock (actual: " + stockActual + ").");
        return;
      }
    }

    String detalle = seleccion.stream()
      .map(x -> String.format("- Lote %s (Vto %s): %d",
        x.fechaLote==null?"—":x.fechaLote, x.fechaVencimiento==null?"—":x.fechaVencimiento, x.cantidadBase))
      .collect(Collectors.joining("\n"));

    if (!confirm("Confirmar salida manual por lotes?\n\n" + detalle)) return;

    try {
      String usuario = System.getProperty("user.name", "sistema");
      stockService.registrarSalidaPorLotes(
        usuario,
        tfMotivo.getText()==null? "Salida" : tfMotivo.getText().trim(),
        FXCollections.observableArrayList(seleccion)
      );
      seleccion.clear();
      cargarLotes(productoSel);
      info("Salida registrada.");
      AppBus.fireStockChanged();
    } catch (Exception ex) {
      alert("No se pudo registrar:\n" + ex.getMessage());
    }
  }

  private int obtenerStockLote(int loteId) {
    String q = "SELECT COALESCE(sl.cantidad_base,0) FROM stock_lote sl WHERE sl.lote_id=?";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(q)) {
      ps.setInt(1, loteId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
      }
    } catch (Exception ignored) {}
    return 0;
  }

  private static void onlyInts(TextField tf){ tf.textProperty().addListener((o,a,b)->{ if(b!=null && !b.matches("\\d*")) tf.setText(b.replaceAll("[^\\d]",""));});}
  private static int parseInt(String s){ try{ return (s==null||s.isBlank())? 0 : Integer.parseInt(s.trim()); }catch(Exception e){ return 0; } }
  private static void alert(String m){ new Alert(Alert.AlertType.ERROR,m,ButtonType.OK).showAndWait(); }
  private static void info(String m){ new Alert(Alert.AlertType.INFORMATION,m,ButtonType.OK).showAndWait(); }
  private static boolean confirm(String m){ var r=new Alert(Alert.AlertType.CONFIRMATION,m,ButtonType.OK,ButtonType.CANCEL).showAndWait(); return r.isPresent() && r.get()==ButtonType.OK; }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }
}
