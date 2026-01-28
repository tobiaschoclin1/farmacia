package com.tobias.ui;

import com.tobias.dao.ProductDao;
import com.tobias.model.Product;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;

public class ProductsView extends BorderPane {

  private final ProductDao productDao = new ProductDao();
  private final ObservableList<Product> data = FXCollections.observableArrayList();

  private final TextField tfBuscar = new TextField();
  private final Button btnNuevo   = new Button("Nuevo");
  private final Button btnEditar  = new Button("Editar");
  private final Button btnGuardar = new Button("Guardar");
  private final Button btnCancelar= new Button("Cancelar");

  private TableView<Product> table;
  private Product editing = null;

  private final TextField tfCodigo = new TextField();
  private final TextField tfNombre = new TextField();
  private final ComboBox<String> cbUnidad = new ComboBox<>();
  private final TextField tfUnidCaja = new TextField();
  private final TextField tfStockMin = new TextField();
  private final CheckBox chkActivo   = new CheckBox("Activo");

  public ProductsView() {
    setPadding(new Insets(10));
    setTop(buildTop());
    table = buildTable();
    setCenter(table);
    setBottom(buildForm());

    cbUnidad.getItems().addAll("TABLETA","CAJA","UNIDAD","ML","GR");
    onlyInts(tfUnidCaja); onlyInts(tfStockMin);

    tfBuscar.textProperty().addListener((o,a,b)->recargar());
    btnNuevo.setOnAction(e->startNew());
    btnEditar.setOnAction(e->startEdit());
    btnGuardar.setOnAction(e->save());
    btnCancelar.setOnAction(e->cancel());

    recargar();
    setFormDisabled(true);
  }

  private Node buildTop() {
    var hb = new HBox(8, new Label("Buscar:"), tfBuscar, btnNuevo, btnEditar);
    hb.setPadding(new Insets(0,0,10,0));
    hb.getStyleClass().add("toolbar");
    return hb;
  }

  private TableView<Product> buildTable() {
    var table = new TableView<Product>(data);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var cCod = new TableColumn<Product, String>("Código de barras");
    cCod.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCodigoBarra()==null?"":p.getValue().getCodigoBarra()));

    var cNom = new TableColumn<Product, String>("Nombre");
    cNom.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNombre()));

    var cUni = new TableColumn<Product, String>("Unidad base");
    cUni.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getUnidadBase()));

    var cUpc = new TableColumn<Product, Number>("Unid/Caja");
    cUpc.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getUnidadesPorCaja()==null?0:p.getValue().getUnidadesPorCaja()));
    cUpc.setStyle("-fx-alignment: CENTER;");

    var cMin = new TableColumn<Product, Number>("Stock mínimo");
    cMin.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getStockMinimo()));
    cMin.setStyle("-fx-alignment: CENTER-RIGHT;");

    var cAct = new TableColumn<Product, String>("Activo");
    cAct.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().isActivo() ? "true":"false"));
    cAct.setStyle("-fx-alignment: CENTER;");

    bindPercentWidth(cCod, table, 0.18);
    bindPercentWidth(cNom, table, 0.32);
    bindPercentWidth(cUni, table, 0.14);
    bindPercentWidth(cUpc, table, 0.12);
    bindPercentWidth(cMin, table, 0.12);
    bindPercentWidth(cAct, table, 0.12);

    table.getColumns().setAll(cCod,cNom,cUni,cUpc,cMin,cAct);
    table.getSelectionModel().selectedItemProperty().addListener((o,a,b)->btnEditar.setDisable(b==null));
    btnEditar.setDisable(true);
    return table;
  }

  private Node buildForm() {
    var g = new GridPane();
    g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(10,0,0,0));
    g.getStyleClass().add("toolbar");
    int r=0;
    g.add(new Label("Código de barras"), 0,r); g.add(tfCodigo, 1,r++);
    g.add(new Label("Nombre"), 0,r); g.add(tfNombre, 1,r++);
    g.add(new Label("Unidad base"), 0,r); g.add(cbUnidad, 1,r++);
    g.add(new Label("Unid/Caja"), 0,r); g.add(tfUnidCaja, 1,r++);
    g.add(new Label("Stock mínimo"), 0,r); g.add(tfStockMin, 1,r++);
    g.add(chkActivo, 1,r++);
    var buttons = new HBox(8, btnGuardar, btnCancelar);
    g.add(buttons, 1,r);
    return g;
  }

  private void recargar() {
  String filtro = tfBuscar.getText();
  try {
    List<Product> lista = productDao.findAll(filtro==null? "" : filtro.trim());
    data.setAll(lista);
  } catch (Exception ex) {
    new Alert(Alert.AlertType.ERROR, "No se pudieron cargar los productos:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
  }
}


  private void startNew() {
    editing = null;
    clearForm();
    setFormDisabled(false);
  }

  private void startEdit() {
    var p = table.getSelectionModel().getSelectedItem();
    if (p==null) return;
    editing = p;
    tfCodigo.setText(p.getCodigoBarra());
    tfNombre.setText(p.getNombre());
    cbUnidad.getSelectionModel().select(p.getUnidadBase());
    tfUnidCaja.setText(p.getUnidadesPorCaja()==null? "" : String.valueOf(p.getUnidadesPorCaja()));
    tfStockMin.setText(String.valueOf(p.getStockMinimo()));
    chkActivo.setSelected(p.isActivo());
    setFormDisabled(false);
  }

  private void save() {
    try {
      var p = (editing==null)? new Product() : editing;
      p.setCodigoBarra(nullIfBlank(tfCodigo.getText()));
      p.setNombre(req(tfNombre.getText(),"Nombre"));
      p.setUnidadBase(req(cbUnidad.getValue(),"Unidad base"));
      p.setUnidadesPorCaja(parseInteger(tfUnidCaja.getText()));
      p.setStockMinimo(parseInt(tfStockMin.getText()));
      p.setActivo(chkActivo.isSelected());

      if (editing==null) productDao.insert(p); else productDao.update(p);
      setFormDisabled(true); clearForm(); recargar(); info("Producto guardado.");
    } catch (Exception ex) { alert("No se pudo guardar:\n"+ex.getMessage()); }
  }

  private void cancel() { setFormDisabled(true); clearForm(); }

  private void setFormDisabled(boolean d) {
    tfCodigo.setDisable(d); tfNombre.setDisable(d); cbUnidad.setDisable(d);
    tfUnidCaja.setDisable(d); tfStockMin.setDisable(d); chkActivo.setDisable(d);
    btnGuardar.setDisable(d); btnCancelar.setDisable(d);
  }
  private void clearForm() {
    tfCodigo.clear(); tfNombre.clear(); cbUnidad.getSelectionModel().clearSelection();
    tfUnidCaja.clear(); tfStockMin.clear(); chkActivo.setSelected(true);
  }

  // utils
  private static void onlyInts(TextField tf){ tf.textProperty().addListener((o,a,b)->{ if(b!=null && !b.matches("\\d*")) tf.setText(b.replaceAll("[^\\d]",""));});}
  private static String req(String v,String campo){ if(v==null||v.isBlank()) throw new IllegalArgumentException("Falta "+campo); return v.trim();}
  private static String nullIfBlank(String s){ return (s==null||s.isBlank())? null : s.trim();}
  private static Integer parseInteger(String s){ try{ return (s==null||s.isBlank())? null : Integer.valueOf(s.trim()); }catch(Exception e){ return null; } }
  private static int parseInt(String s){ try{ return (s==null||s.isBlank())? 0 : Integer.parseInt(s.trim()); }catch(Exception e){ return 0; } }
  private static void alert(String m){ new Alert(Alert.AlertType.ERROR,m,ButtonType.OK).showAndWait(); }
  private static void info(String m){ new Alert(Alert.AlertType.INFORMATION,m,ButtonType.OK).showAndWait(); }

  private static void bindPercentWidth(TableColumn<?,?> col, TableView<?> table, double pct){
    col.prefWidthProperty().bind(table.widthProperty().subtract(18).multiply(pct));
  }
}
