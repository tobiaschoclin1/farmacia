package com.tobias.ui;

import com.tobias.dao.ProductDao;
import com.tobias.model.EntradaItem;
import com.tobias.model.Product;
import com.tobias.service.StockService;
import com.tobias.util.AppBus;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StockEntryView extends BorderPane {
  private final ProductDao productDao = new ProductDao();
  private final StockService stockService = new StockService();
  private final ObservableList<EntradaItem> items = FXCollections.observableArrayList();

  private final TextField tfCodigo = new TextField();
  private final Button btnBuscarCod = new Button("Buscar código");

  private final TextField tfBuscarNombre = new TextField();
  private final Button btnBuscarNom = new Button("Buscar nombre");

  private final Label lbNombre = new Label("-");
  private final Label lbUnidad = new Label("-");
  private final Label lbUnidCaja = new Label("-");
  private Product productoSel = null;

  private final DatePicker dpLote = new DatePicker();
  private final DatePicker dpVenc = new DatePicker();
  private final TextField tfCajas = new TextField();
  private final TextField tfTabletas = new TextField();
  private final TextField tfCantidadBase = new TextField();

  private final Button btnAgregar = new Button("Agregar a la lista");
  private final Button btnConfirmar = new Button("Confirmar entrada");

  public StockEntryView() {
    setPadding(new Insets(10));
    setTop(buildFinder());
    setCenter(buildTable());
    setBottom(buildActions());

    onlyInts(tfCajas); onlyInts(tfTabletas); onlyInts(tfCantidadBase);

    btnBuscarCod.setOnAction(e -> buscarProductoPorCodigo());
    btnBuscarNom.setOnAction(e -> buscarProductoPorNombre());
    btnAgregar.setOnAction(e -> agregarItem());
    btnConfirmar.setOnAction(e -> confirmarEntrada());
  }

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

    g.add(new Label("Fecha de lote (ingreso)"), 0,r); g.add(dpLote, 1,r++);
    g.add(new Label("Vencimiento"), 0,r); g.add(dpVenc, 1,r++);

    var filaCaja = new HBox(8, new Label("Cajas"), tfCajas, new Label("Tabletas"), tfTabletas);
    var filaBase = new HBox(8, new Label("Cantidad (unidad base)"), tfCantidadBase);
    var wrap = new VBox(6, filaCaja, filaBase);
    g.add(wrap, 1,r++);

    return g;
  }

  private TableView<EntradaItem> buildTable() {
    var table = new TableView<>(items);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cProd = new TableColumn<EntradaItem, String>("Producto");
    cProd.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNombreProducto()));

    var cLote = new TableColumn<EntradaItem, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFechaLote()==null? "" : p.getValue().getFechaLote().toString()));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto = new TableColumn<EntradaItem, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFechaVencimiento()==null? "" : p.getValue().getFechaVencimiento().toString()));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cCant = new TableColumn<EntradaItem, Number>("Cantidad (base)");
    cCant.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getCantidadBase()));
    cCant.setStyle("-fx-alignment: CENTER-RIGHT;");

    bindPercentWidth(cProd, table, 0.40);
    bindPercentWidth(cLote, table, 0.20);
    bindPercentWidth(cVto,  table, 0.20);
    bindPercentWidth(cCant, table, 0.20);

    table.getColumns().setAll(cProd, cLote, cVto, cCant);
    return table;
  }

  private Node buildActions() {
    var hb = new HBox(10, btnAgregar, btnConfirmar);
    hb.setPadding(new Insets(10,0,0,0));
    return hb;
  }

  private void buscarProductoPorCodigo() {
    try {
      String codigo = tfCodigo.getText();
      if (codigo==null||codigo.isBlank()) { alert("Ingresá un código de barras."); return; }
      var p = productDao.findByCodigoBarra(codigo.trim());
      if (p==null) { alert("No existe un producto con ese código."); limpiarProductoSel(); return; }
      setProductoSel(p);
    } catch (Exception ex) { alert("Error al buscar: "+ex.getMessage()); }
  }

  private void buscarProductoPorNombre() {
    try {
      String filtro = tfBuscarNombre.getText();
      if (filtro==null||filtro.isBlank()) { alert("Ingresá parte del nombre."); return; }
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
    } catch (Exception ex) { alert("Error al buscar: "+ex.getMessage()); }
  }

  private void setProductoSel(Product p) {
    productoSel = p;
    lbNombre.setText(p.getNombre());
    lbUnidad.setText(p.getUnidadBase());
    lbUnidCaja.setText(p.getUnidadesPorCaja()==null? "-" : String.valueOf(p.getUnidadesPorCaja()));
  }
  private void limpiarProductoSel() { productoSel=null; lbNombre.setText("-"); lbUnidad.setText("-"); lbUnidCaja.setText("-"); }

  private void agregarItem() {
    if (productoSel==null) { alert("Primero seleccioná un producto (código o nombre)."); return; }
    LocalDate fechaLote = dpLote.getValue();
    LocalDate vto = dpVenc.getValue();

    int cantidadBase;
    Integer upc = productoSel.getUnidadesPorCaja();
    if (upc!=null && upc>0) {
      int cajas = parseInt(tfCajas.getText());
      int tabs  = parseInt(tfTabletas.getText());
      cantidadBase = cajas*upc + tabs;
    } else {
      cantidadBase = parseInt(tfCantidadBase.getText());
    }
    if (cantidadBase<=0) { alert("Cantidad debe ser mayor a 0."); return; }

    items.add(new EntradaItem(productoSel.getId(), productoSel.getNombre(), fechaLote, vto, cantidadBase));
    tfCajas.clear(); tfTabletas.clear(); tfCantidadBase.clear(); dpLote.setValue(null); dpVenc.setValue(null);
  }

  private void confirmarEntrada() {
    if (items.isEmpty()) { alert("No hay ítems para ingresar."); return; }
    if (!confirm("¿Registrar la entrada de stock?")) return;
    try {
      stockService.registrarEntrada("admin", FXCollections.observableArrayList(items));
      items.clear();
      info("Entrada registrada.");
      AppBus.fireStockChanged();
    } catch (Exception ex) { alert("Error al registrar:\n"+ex.getMessage()); }
  }

  // utils
  private static void onlyInts(TextField tf){ tf.textProperty().addListener((o,a,b)->{ if(b!=null && !b.matches("\\d*")) tf.setText(b.replaceAll("[^\\d]",""));});}
  private static int parseInt(String s){ try{ return (s==null||s.isBlank())? 0 : Integer.parseInt(s.trim()); }catch(Exception e){ return 0; } }
  private static void alert(String m){ new Alert(Alert.AlertType.ERROR,m,ButtonType.OK).showAndWait(); }
  private static void info(String m){ new Alert(Alert.AlertType.INFORMATION,m,ButtonType.OK).showAndWait(); }
  private static boolean confirm(String m){ var r=new Alert(Alert.AlertType.CONFIRMATION,m,ButtonType.OK,ButtonType.CANCEL).showAndWait(); return r.isPresent() && r.get()==ButtonType.OK; }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }
}
