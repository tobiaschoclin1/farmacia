package com.tobias.ui;

import com.tobias.dao.ProductDao;
import com.tobias.model.EntradaItem;
import com.tobias.model.Product;
import com.tobias.service.StockService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StockEntryView extends BorderPane {
  private final ProductDao productDao = new ProductDao();
  private final StockService stockService = new StockService();
  private final ObservableList<EntradaItem> items = FXCollections.observableArrayList();

  private final TextField tfCodigo = new TextField();
  private final Button btnBuscarCod = new Button("Buscar");

  private final TextField tfBuscarNombre = new TextField();
  private final Button btnBuscarNom = new Button("Buscar");

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

  private TableView<EntradaItem> table;

  public StockEntryView() {
    setPadding(new Insets(0));

    var content = new VBox(16);
    content.setPadding(new Insets(0));

    content.getChildren().addAll(
      buildHeader(),
      buildProductSelector(),
      buildEntryForm(),
      buildTableCard(),
      buildActions()
    );

    // Envolver en ScrollPane para permitir scroll
    var scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");

    setCenter(scroll);

    // Configuracion
    tfCodigo.setPromptText("Codigo de barras");
    tfBuscarNombre.setPromptText("Buscar por nombre...");
    tfCajas.setPromptText("0");
    tfTabletas.setPromptText("0");
    tfCantidadBase.setPromptText("0");

    dpLote.setPromptText("Seleccionar fecha");
    dpVenc.setPromptText("Seleccionar fecha");
    dpLote.setValue(LocalDate.now());

    btnBuscarCod.getStyleClass().add("secondary");
    btnBuscarNom.getStyleClass().add("secondary");
    btnAgregar.getStyleClass().add("success");
    btnConfirmar.getStyleClass().add("primary");

    // Configurar iconos en botones
    btnBuscarCod.setGraphic(Icons.magnifyingGlass(16));
    btnBuscarNom.setGraphic(Icons.magnifyingGlass(16));
    btnAgregar.setGraphic(Icons.plus(16));
    btnConfirmar.setGraphic(Icons.check(16));

    onlyInts(tfCajas);
    onlyInts(tfTabletas);
    onlyInts(tfCantidadBase);

    btnBuscarCod.setOnAction(e -> buscarProductoPorCodigo());
    btnBuscarNom.setOnAction(e -> buscarProductoPorNombre());
    btnAgregar.setOnAction(e -> agregarItem());
    btnConfirmar.setOnAction(e -> confirmarEntrada());
  }

  private Node buildHeader() {
    var title = new Label("Registro de Entradas");
    title.getStyleClass().add("page-title");

    var subtitle = new Label("Registra nuevas entradas de stock al inventario");
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

    // Busqueda por codigo
    var lblCodigo = new Label("Codigo de barras");
    lblCodigo.getStyleClass().add("form-label");

    var hbCod = new HBox(8, tfCodigo, btnBuscarCod);
    HBox.setHgrow(tfCodigo, Priority.ALWAYS);

    grid.add(lblCodigo, 0, r);
    grid.add(hbCod, 1, r++);

    // Busqueda por nombre
    var lblNombre = new Label("Buscar por nombre");
    lblNombre.getStyleClass().add("form-label");

    var hbNom = new HBox(8, tfBuscarNombre, btnBuscarNom);
    HBox.setHgrow(tfBuscarNombre, Priority.ALWAYS);

    grid.add(lblNombre, 0, r);
    grid.add(hbNom, 1, r++);

    // Separador
    var sep = new Separator();
    grid.add(sep, 0, r++, 2, 1);

    // Info del producto seleccionado
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

    card.getChildren().addAll(cardTitle, grid);
    return card;
  }

  private Node buildEntryForm() {
    var card = new VBox(16);
    card.getStyleClass().add("card");

    var cardTitle = new Label("2. Datos de la Entrada");
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

    var lblLote = new Label("Fecha de lote *");
    lblLote.getStyleClass().add("form-label");
    grid.add(lblLote, 0, r);
    grid.add(dpLote, 1, r++);

    var lblVenc = new Label("Fecha de vencimiento");
    lblVenc.getStyleClass().add("form-label");
    grid.add(lblVenc, 0, r);
    grid.add(dpVenc, 1, r++);

    // Separador
    var sep = new Separator();
    grid.add(sep, 0, r++, 2, 1);

    var lblCantidad = new Label("Cantidad");
    lblCantidad.getStyleClass().add("form-label");
    lblCantidad.setStyle("-fx-font-size: 15px; -fx-font-weight: 700;");
    grid.add(lblCantidad, 0, r++, 2, 1);

    // Cajas y tabletas
    var lblCajas = new Label("Cajas");
    var lblTabs = new Label("Tabletas sueltas");
    tfCajas.setMaxWidth(120);
    tfTabletas.setMaxWidth(120);

    var hbCantidades = new HBox(16, lblCajas, tfCajas, lblTabs, tfTabletas);
    hbCantidades.setAlignment(Pos.CENTER_LEFT);

    grid.add(new Label("Por caja/tabletas"), 0, r);
    grid.add(hbCantidades, 1, r++);

    var lblOr = new Label("- O -");
    lblOr.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
    grid.add(lblOr, 0, r++, 2, 1);

    var lblBase = new Label("Cantidad (unidad base)");
    lblBase.getStyleClass().add("form-label");
    grid.add(lblBase, 0, r);
    grid.add(tfCantidadBase, 1, r++);

    grid.add(new Label(""), 0, r);
    grid.add(btnAgregar, 1, r);

    card.getChildren().addAll(cardTitle, grid);
    return card;
  }

  private Node buildTableCard() {
    var card = new VBox(12);
    card.getStyleClass().add("card");
    VBox.setVgrow(card, Priority.ALWAYS);

    var cardTitle = new Label("3. Items a Registrar");
    cardTitle.getStyleClass().add("card-title");
    cardTitle.setStyle("-fx-font-size: 18px;");

    table = buildTable();
    VBox.setVgrow(table, Priority.ALWAYS);

    card.getChildren().addAll(cardTitle, table);
    return card;
  }

  private TableView<EntradaItem> buildTable() {
    var table = new TableView<>(items);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    table.setPlaceholder(new Label("No hay items agregados. Agrega productos para registrar la entrada."));

    var cProd = new TableColumn<EntradaItem, String>("Producto");
    cProd.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNombreProducto()));

    var cLote = new TableColumn<EntradaItem, String>("Fecha lote");
    cLote.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFechaLote()==null? "" : p.getValue().getFechaLote().toString()));
    cLote.setStyle("-fx-alignment: CENTER-LEFT;");

    var cVto = new TableColumn<EntradaItem, String>("Vencimiento");
    cVto.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFechaVencimiento()==null? "" : p.getValue().getFechaVencimiento().toString()));
    cVto.setStyle("-fx-alignment: CENTER-LEFT;");

    var cCant = new TableColumn<EntradaItem, Number>("Cantidad");
    cCant.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getCantidadBase()));
    cCant.setStyle("-fx-alignment: CENTER-RIGHT;");

    bindPercentWidth(cProd, table, 0.40);
    bindPercentWidth(cLote, table, 0.20);
    bindPercentWidth(cVto,  table, 0.20);
    bindPercentWidth(cCant, table, 0.20);

    table.getColumns().setAll(List.of(cProd, cLote, cVto, cCant));
    return table;
  }

  private Node buildActions() {
    var card = new HBox(12);
    card.getStyleClass().add("card");
    card.setAlignment(Pos.CENTER_RIGHT);

    var lblTotal = new Label("Total de items:");
    lblTotal.setStyle("-fx-font-weight: 600;");

    var lblCount = new Label("0");
    lblCount.textProperty().bind(javafx.beans.binding.Bindings.size(items).asString());
    lblCount.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #2563EB;");

    var spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    card.getChildren().addAll(lblTotal, lblCount, spacer, btnConfirmar);
    return card;
  }

  private void buscarProductoPorCodigo() {
    try {
      String codigo = tfCodigo.getText();
      if (codigo==null || codigo.isBlank()) {
        alert("Ingresa un codigo de barras.");
        return;
      }
      var p = productDao.findByCodigoBarra(codigo.trim());
      if (p==null) {
        alert("No existe un producto con ese codigo.");
        limpiarProductoSel();
        return;
      }
      setProductoSel(p);
    } catch (Exception ex) {
      alert("Error al buscar producto:\n"+ex.getMessage());
    }
  }

  private void buscarProductoPorNombre() {
    try {
      String filtro = tfBuscarNombre.getText();
      if (filtro==null || filtro.isBlank()) {
        alert("Ingresa un nombre para buscar.");
        return;
      }
      var lista = productDao.findAll(filtro.trim());
      if (lista.isEmpty()) {
        alert("No se encontraron productos con ese nombre.");
        limpiarProductoSel();
        return;
      }
      if (lista.size() == 1) {
        setProductoSel(lista.get(0));
      } else {
        var dialog = new ChoiceDialog<>(lista.get(0), lista);
        dialog.setTitle("Seleccionar Producto");
        dialog.setHeaderText("Se encontraron varios productos");
        dialog.setContentText("Elige uno:");
        Optional<Product> res = dialog.showAndWait();
        res.ifPresent(this::setProductoSel);
      }
    } catch (Exception ex) {
      alert("Error al buscar producto:\n"+ex.getMessage());
    }
  }

  private void setProductoSel(Product p) {
    productoSel = p;
    lbNombre.setText(p.getNombre());
    lbUnidad.setText(p.getUnidadBase());
    lbUnidCaja.setText(p.getUnidadesPorCaja()==null? "-" : String.valueOf(p.getUnidadesPorCaja()));
  }

  private void limpiarProductoSel() {
    productoSel = null;
    lbNombre.setText("-");
    lbUnidad.setText("-");
    lbUnidCaja.setText("-");
  }

  private void agregarItem() {
    try {
      if (productoSel == null) { alert("Selecciona un producto primero."); return; }

      LocalDate lote = dpLote.getValue();
      if (lote == null) { alert("Ingresa la fecha de lote."); return; }

      LocalDate venc = dpVenc.getValue();

      int cajas = parseInt(tfCajas.getText());
      int tabs = parseInt(tfTabletas.getText());
      int base = parseInt(tfCantidadBase.getText());

      int cantidadFinal = base;
      if (base == 0 && productoSel.getUnidadesPorCaja() != null && productoSel.getUnidadesPorCaja() > 0) {
        cantidadFinal = cajas * productoSel.getUnidadesPorCaja() + tabs;
      }

      if (cantidadFinal <= 0) { alert("Ingresa una cantidad valida."); return; }

      var item = new EntradaItem(
        productoSel.getId(),
        productoSel.getNombre(),
        lote,
        venc,
        cantidadFinal
      );

      items.add(item);

      tfCajas.clear();
      tfTabletas.clear();
      tfCantidadBase.clear();
      dpVenc.setValue(null);

    } catch (Exception ex) {
      alert("Error al agregar item:\n"+ex.getMessage());
    }
  }

  private void confirmarEntrada() {
    try {
      if (items.isEmpty()) { alert("No hay items para registrar."); return; }

      String usuario = System.getProperty("user.name", "sistema");
      stockService.registrarEntrada(usuario, items);
      items.clear();
      limpiarProductoSel();

      AppBus.fireStockChanged();

      info("Entrada registrada exitosamente.");
    } catch (Exception ex) {
      alert("Error al confirmar entrada:\n"+ex.getMessage());
    }
  }

  private static void onlyInts(TextField tf) {
    tf.textProperty().addListener((o,a,b)->{ if(b!=null && !b.matches("\\d*")) tf.setText(b.replaceAll("[^\\d]",""));});
  }

  private static int parseInt(String s) {
    try { return (s==null||s.isBlank())? 0 : Integer.parseInt(s.trim()); }
    catch(Exception e){ return 0; }
  }

  private static void alert(String m) {
    new Alert(Alert.AlertType.ERROR,m,ButtonType.OK).showAndWait();
  }

  private static void info(String m) {
    new Alert(Alert.AlertType.INFORMATION,m,ButtonType.OK).showAndWait();
  }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }
}
