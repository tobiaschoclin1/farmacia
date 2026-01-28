package com.tobias.ui;

import com.tobias.dao.ProductDao;
import com.tobias.model.Product;
import com.tobias.service.StockService;
import com.tobias.service.StockService.SalidaAsignacion;
import com.tobias.util.AppBus;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tobias.db.Db;

public class StockOutView extends BorderPane {

  // ------- UI y estado -------
  private final ProductDao productDao = new ProductDao();
  private final StockService stockService = new StockService();

  private final TextField tfCodigo = new TextField();
  private final Button btnBuscarCod = new Button("Buscar código");

  private final TextField tfBuscarNombre = new TextField();
  private final Button btnBuscarNom = new Button("Buscar nombre");

  private final Label lbNombre = new Label("-");
  private final Label lbUnidad = new Label("-");
  private final Label lbUnidCaja = new Label("-");
  private final TextField tfMotivo = new TextField("Venta / Consumo");

  private Product productoSel = null;

  // Lado izquierdo: lotes disponibles
  public static class LoteRow {
    int loteId;
    String fechaLote;
    String fechaVto;
    int stockBase;      // stock disponible en unidad base
    Integer cajas;      // si aplica
  }
  private final ObservableList<LoteRow> lotes = FXCollections.observableArrayList();
  private TableView<LoteRow> tablaLotes;

  // Agregar cantidad desde lote seleccionado
  private final TextField tfCantidadBase = new TextField();
  private final Button btnAgregar = new Button("Agregar desde lote");

  // Lado derecho: salidas seleccionadas (carrito)
  private final ObservableList<SalidaAsignacion> seleccion = FXCollections.observableArrayList();
  private TableView<SalidaAsignacion> tablaSeleccion;
  private final Button btnQuitar = new Button("Quitar seleccionado");
  private final Button btnConfirmar = new Button("Confirmar salida");

  public StockOutView() {
    setPadding(new Insets(10));
    setTop(buildFinder());
    var split = new SplitPane(buildLeftPane(), buildRightPane());
    split.setDividerPositions(0.55);
    setCenter(split);

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

  // ------- Top: selector de producto -------
  private Node buildFinder() {
    var g = new GridPane();
    g.setHgap(8); g.setVgap(8); g.setPadding(new Insets(0,0,10,0));
    g.getStyleClass().add("toolbar");
    int r=0;

    g.add(new Label("Código de barras"), 0,r);
    var hbCod = new HBox(8, tfCodigo, btnBuscarCod);
    HBox.setHgrow(tfCodigo, Priority.ALWAYS);
    g.add(hbCod, 1,r++);

    g.add(new Label("Buscar por nombre"), 0,r);
    var hbNom = new HBox(8, tfBuscarNombre, btnBuscarNom);
    HBox.setHgrow(tfBuscarNombre, Priority.ALWAYS);
    g.add(hbNom, 1,r++);

    g.add(new Label("Producto"), 0,r); g.add(lbNombre, 1,r++);
    g.add(new Label("Unidad base"), 0,r); g.add(lbUnidad, 1,r++);
    g.add(new Label("Unid/Caja"), 0,r); g.add(lbUnidCaja, 1,r++);

    g.add(new Label("Motivo"), 0,r); g.add(tfMotivo, 1,r++);

    return g;
  }

  // ------- Izquierda: lotes + agregar -------
  private Node buildLeftPane() {
    var root = new VBox(8);
    tablaLotes = buildTablaLotes();
    var addBar = new HBox(8, new Label("Cantidad (base):"), tfCantidadBase, btnAgregar);
    root.getChildren().addAll(new Label("Lotes disponibles"), tablaLotes, addBar);
    VBox.setVgrow(tablaLotes, Priority.ALWAYS);
    return root;
  }

  private TableView<LoteRow> buildTablaLotes() {
    var t = new TableView<LoteRow>(lotes);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cLote = new TableColumn<LoteRow, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto  = new TableColumn<LoteRow, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVto==null?"":p.getValue().fechaVto));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cStock = new TableColumn<LoteRow, Number>("Stock (base)");
    cStock.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().stockBase));
    cStock.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cCajas = new TableColumn<LoteRow, String>("Cajas");
    cCajas.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().cajas==null?"-":String.valueOf(p.getValue().cajas)));
    cCajas.setStyle("-fx-alignment: CENTER;");

    bindPercentWidth(cLote, t, 0.28);
    bindPercentWidth(cVto,  t, 0.28);
    bindPercentWidth(cStock,t, 0.26);
    bindPercentWidth(cCajas,t, 0.18);

    t.getColumns().setAll(cLote, cVto, cStock, cCajas);

    // Colores por vencimiento
    t.setRowFactory(tv -> new TableRow<>() {
      @Override protected void updateItem(LoteRow item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setStyle(""); return; }
        if (item.fechaVto != null && !item.fechaVto.isBlank()) {
          // cálculo rápido de días restantes en SQL no lo trajimos; solo marcamos si fecha pasada
          if (item.fechaVto.compareTo(java.time.LocalDate.now().toString()) < 0) {
            setStyle("-fx-background-color:#ffd6d6;");
            return;
          }
        }
        setStyle("");
      }
    });

    return t;
  }

  // ------- Derecha: carrito de salidas -------
  private Node buildRightPane() {
    var root = new VBox(8);
    tablaSeleccion = buildTablaSeleccion();
    var actions = new HBox(8, btnQuitar, btnConfirmar);
    root.getChildren().addAll(new Label("Salidas seleccionadas"), tablaSeleccion, actions);
    VBox.setVgrow(tablaSeleccion, Priority.ALWAYS);
    return root;
  }

  private TableView<SalidaAsignacion> buildTablaSeleccion() {
    var t = new TableView<>(seleccion);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cLote = new TableColumn<SalidaAsignacion, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaLote==null?"":p.getValue().fechaLote));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto = new TableColumn<SalidaAsignacion, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().fechaVencimiento==null?"":p.getValue().fechaVencimiento));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cCant = new TableColumn<SalidaAsignacion, Number>("Cantidad (base)");
    cCant.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().cantidadBase));
    cCant.setStyle("-fx-alignment: CENTER-RIGHT;");

    bindPercentWidth(cLote, t, 0.35);
    bindPercentWidth(cVto,  t, 0.35);
    bindPercentWidth(cCant, t, 0.30);

    t.getColumns().setAll(cLote, cVto, cCant);
    return t;
  }

  // ------- Acciones -------
  private void buscarProductoPorCodigo() {
    try {
      String codigo = tfCodigo.getText();
      if (codigo==null || codigo.isBlank()) { alert("Ingresá un código de barras."); return; }
      var p = productDao.findByCodigoBarra(codigo.trim());
      if (p==null) { alert("No existe un producto con ese código."); limpiarProductoSel(); return; }
      setProductoSel(p);
    } catch (Exception ex) { alert("Error al buscar: " + ex.getMessage()); }
  }

  private void buscarProductoPorNombre() {
    try {
      String filtro = tfBuscarNombre.getText();
      if (filtro==null || filtro.isBlank()) { alert("Ingresá parte del nombre."); return; }
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
    if (productoSel == null) { alert("Seleccioná un producto."); return; }
    if (lote == null) { alert("Elegí un lote de la tabla izquierda."); return; }
    int cant = parseInt(tfCantidadBase.getText());
    if (cant <= 0) { alert("Ingresá una cantidad (>0)."); return; }
    if (cant > lote.stockBase) { alert("Cantidad supera el stock del lote."); return; }

    // Si ya existe una asignación para ese lote, sumamos
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
    if (productoSel == null) { alert("Seleccioná un producto."); return; }
    if (seleccion.isEmpty()) { alert("No hay lotes seleccionados."); return; }

    // Validación contra stock actual (por si cambió)
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

    if (!confirm("¿Confirmar salida manual por lotes?\n\n" + detalle)) return;

    try {
      stockService.registrarSalidaPorLotes(
        "admin",
        tfMotivo.getText()==null? "Salida" : tfMotivo.getText().trim(),
        FXCollections.observableArrayList(seleccion)
      );
      seleccion.clear();
      cargarLotes(productoSel); // refresca stock restante
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

  // ------- utils -------
  private static void onlyInts(TextField tf){ tf.textProperty().addListener((o,a,b)->{ if(b!=null && !b.matches("\\d*")) tf.setText(b.replaceAll("[^\\d]",""));});}
  private static int parseInt(String s){ try{ return (s==null||s.isBlank())? 0 : Integer.parseInt(s.trim()); }catch(Exception e){ return 0; } }
  private static void alert(String m){ new Alert(Alert.AlertType.ERROR,m,ButtonType.OK).showAndWait(); }
  private static void info(String m){ new Alert(Alert.AlertType.INFORMATION,m,ButtonType.OK).showAndWait(); }
  private static boolean confirm(String m){ var r=new Alert(Alert.AlertType.CONFIRMATION,m,ButtonType.OK,ButtonType.CANCEL).showAndWait(); return r.isPresent() && r.get()==ButtonType.OK; }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }

  // Para obtener la ventana (si la necesitás en algún diálogo futuro)
  @SuppressWarnings("unused")
  private Window window() { return getScene()==null? null : getScene().getWindow(); }
}
