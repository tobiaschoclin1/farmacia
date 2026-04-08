package com.tobias.ui;

import com.tobias.db.Db;
import com.tobias.util.AppBus;
import com.tobias.util.Icons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardView extends BorderPane {

  private Label lblTotalProductos = new Label("0");
  private Label lblEnStock = new Label("0");
  private Label lblStockBajo = new Label("0");
  private Label lblSinStock = new Label("0");
  private Label lblProxVencer = new Label("0");

  private VBox proximosVencer;
  private VBox stockBajoList;

  public DashboardView() {
    setPadding(new Insets(0));

    var content = new VBox(24);
    content.setPadding(new Insets(0));

    content.getChildren().addAll(
      buildHeader(),
      buildKPIGrid(),
      buildAlertsSection()
    );

    var scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");

    setCenter(scroll);

    AppBus.onStockChanged(this::recargar);
    recargar();
  }

  private Node buildHeader() {
    var title = new Label("Dashboard");
    title.getStyleClass().add("page-title");

    var subtitle = new Label("Resumen general del sistema de gestión de farmacia");
    subtitle.getStyleClass().add("page-subtitle");

    var headerText = new VBox(6, title, subtitle);

    var header = new VBox(headerText);
    header.getStyleClass().add("page-header");

    return header;
  }

  private Node buildKPIGrid() {
    var grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);

    // KPI 1: Total Productos
    var kpi1 = buildKPICard(
      "Productos Totales",
      lblTotalProductos,
      "Total de productos activos",
      "#5B7FFF",
      Icons.cubeTransparent(24)
    );

    // KPI 2: En Stock
    var kpi2 = buildKPICard(
      "En Stock",
      lblEnStock,
      "Con stock suficiente",
      "#12B76A",
      Icons.checkCircle(24)
    );

    // KPI 3: Stock Bajo
    var kpi3 = buildKPICard(
      "Stock Bajo",
      lblStockBajo,
      "Por debajo del mínimo",
      "#F79009",
      Icons.exclamationTriangle(24)
    );

    // KPI 4: Sin Stock
    var kpi4 = buildKPICard(
      "Sin Stock",
      lblSinStock,
      "Requieren reposición",
      "#F04438",
      Icons.xCircle(24)
    );

    // KPI 5: Próximos a vencer
    var kpi5 = buildKPICard(
      "Próx. a Vencer",
      lblProxVencer,
      "Vencen en 30 días",
      "#F79009",
      Icons.clock(24)
    );

    grid.add(kpi1, 0, 0);
    grid.add(kpi2, 1, 0);
    grid.add(kpi3, 2, 0);
    grid.add(kpi4, 0, 1);
    grid.add(kpi5, 1, 1);

    // Configurar columnas
    for (int i = 0; i < 3; i++) {
      var col = new ColumnConstraints();
      col.setHgrow(Priority.ALWAYS);
      col.setPercentWidth(33.33);
      grid.getColumnConstraints().add(col);
    }

    return grid;
  }

  private Node buildKPICard(String title, Label valueLabel, String description, String color, Region icon) {
    var card = new VBox(16);
    card.getStyleClass().add("metric-card");
    card.setAlignment(Pos.TOP_LEFT);

    // Icono con fondo de color
    var iconContainer = new StackPane(icon);
    iconContainer.setPrefSize(48, 48);
    iconContainer.setMaxSize(48, 48);
    iconContainer.setStyle(
      "-fx-background-color: " + hexToRgba(color, 0.1) + ";" +
      "-fx-background-radius: 12;"
    );
    icon.setStyle("-fx-background-color: " + color + ";");

    var lblTitle = new Label(title);
    lblTitle.getStyleClass().add("metric-label");

    valueLabel.getStyleClass().add("metric-value");
    valueLabel.setStyle("-fx-text-fill: " + color + ";");

    var lblDesc = new Label(description);
    lblDesc.getStyleClass().add("metric-change");

    card.getChildren().addAll(iconContainer, lblTitle, valueLabel, lblDesc);
    return card;
  }

  private Node buildAlertsSection() {
    var grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);

    // Columna izquierda: Próximos a vencer
    var cardVencer = new VBox(16);
    cardVencer.getStyleClass().add("card");
    VBox.setVgrow(cardVencer, Priority.ALWAYS);

    var iconVencer = Icons.clock(20);
    iconVencer.setStyle("-fx-background-color: #F79009;");

    var titleVencer = new Label(" Productos Próximos a Vencer");
    titleVencer.getStyleClass().add("card-title");

    var headerVencer = new HBox(8, iconVencer, titleVencer);
    headerVencer.setAlignment(Pos.CENTER_LEFT);

    proximosVencer = new VBox(12);

    var scrollVencer = new ScrollPane(proximosVencer);
    scrollVencer.setFitToWidth(true);
    scrollVencer.setStyle("-fx-background-color: transparent;");
    scrollVencer.setPrefHeight(300);
    VBox.setVgrow(scrollVencer, Priority.ALWAYS);

    cardVencer.getChildren().addAll(headerVencer, scrollVencer);

    // Columna derecha: Stock bajo
    var cardStock = new VBox(16);
    cardStock.getStyleClass().add("card");
    VBox.setVgrow(cardStock, Priority.ALWAYS);

    var iconStock = Icons.exclamationTriangle(20);
    iconStock.setStyle("-fx-background-color: #F04438;");

    var titleStock = new Label(" Productos con Stock Bajo");
    titleStock.getStyleClass().add("card-title");

    var headerStock = new HBox(8, iconStock, titleStock);
    headerStock.setAlignment(Pos.CENTER_LEFT);

    stockBajoList = new VBox(12);

    var scrollStock = new ScrollPane(stockBajoList);
    scrollStock.setFitToWidth(true);
    scrollStock.setStyle("-fx-background-color: transparent;");
    scrollStock.setPrefHeight(300);
    VBox.setVgrow(scrollStock, Priority.ALWAYS);

    cardStock.getChildren().addAll(headerStock, scrollStock);

    grid.add(cardVencer, 0, 0);
    grid.add(cardStock, 1, 0);

    // Configurar columnas
    for (int i = 0; i < 2; i++) {
      var col = new ColumnConstraints();
      col.setHgrow(Priority.ALWAYS);
      col.setPercentWidth(50);
      grid.getColumnConstraints().add(col);
    }

    return grid;
  }

  private void recargar() {
    cargarKPIs();
    cargarProximosVencer();
    cargarStockBajo();
  }

  private void cargarKPIs() {
    try (Connection c = Db.get()) {
      // Total productos activos
      String sqlTotal = "SELECT COUNT(*) FROM productos WHERE activo = 1";
      try (PreparedStatement ps = c.prepareStatement(sqlTotal);
           ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          lblTotalProductos.setText(String.valueOf(rs.getInt(1)));
        }
      }

      // Productos por estado de stock
      String sqlStock = """
        SELECT
          SUM(CASE WHEN total_base >= p.stock_minimo AND total_base > 0 THEN 1 ELSE 0 END) AS en_stock,
          SUM(CASE WHEN total_base > 0 AND total_base < p.stock_minimo THEN 1 ELSE 0 END) AS bajo_stock,
          SUM(CASE WHEN total_base = 0 THEN 1 ELSE 0 END) AS sin_stock
        FROM productos p
        LEFT JOIN (
          SELECT l.producto_id, COALESCE(SUM(sl.cantidad_base), 0) AS total_base
          FROM lotes l
          LEFT JOIN stock_lote sl ON sl.lote_id = l.id
          GROUP BY l.producto_id
        ) stock ON stock.producto_id = p.id
        WHERE p.activo = 1
        """;

      try (PreparedStatement ps = c.prepareStatement(sqlStock);
           ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          lblEnStock.setText(String.valueOf(rs.getInt("en_stock")));
          lblStockBajo.setText(String.valueOf(rs.getInt("bajo_stock")));
          lblSinStock.setText(String.valueOf(rs.getInt("sin_stock")));
        }
      }

      // Productos próximos a vencer (30 días)
      String sqlVencer = """
        SELECT COUNT(DISTINCT l.producto_id)
        FROM lotes l
        INNER JOIN productos p ON p.id = l.producto_id
        WHERE p.activo = 1
          AND l.fecha_vencimiento IS NOT NULL
          AND date(l.fecha_vencimiento) <= date('now', '+30 days')
          AND date(l.fecha_vencimiento) >= date('now')
        """;

      try (PreparedStatement ps = c.prepareStatement(sqlVencer);
           ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          lblProxVencer.setText(String.valueOf(rs.getInt(1)));
        }
      }

    } catch (Exception ex) {
      System.err.println("Error al cargar KPIs: " + ex.getMessage());
    }
  }

  private void cargarProximosVencer() {
    proximosVencer.getChildren().clear();

    String sql = """
      SELECT p.nombre, l.fecha_vencimiento, COALESCE(sl.cantidad_base, 0) AS cantidad
      FROM lotes l
      INNER JOIN productos p ON p.id = l.producto_id
      LEFT JOIN stock_lote sl ON sl.lote_id = l.id
      WHERE p.activo = 1
        AND l.fecha_vencimiento IS NOT NULL
        AND date(l.fecha_vencimiento) <= date('now', '+30 days')
        AND date(l.fecha_vencimiento) >= date('now')
      ORDER BY l.fecha_vencimiento
      LIMIT 10
      """;

    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      boolean hayDatos = false;
      while (rs.next()) {
        hayDatos = true;
        String nombre = rs.getString("nombre");
        String fechaVto = rs.getString("fecha_vencimiento");
        int cantidad = rs.getInt("cantidad");

        var item = buildAlertaItem(nombre, fechaVto, cantidad);
        proximosVencer.getChildren().add(item);
      }

      if (!hayDatos) {
        var icon = Icons.checkCircle(16);
        icon.setStyle("-fx-background-color: #12B76A;");

        var empty = new Label(" No hay productos próximos a vencer");
        empty.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");

        var box = new HBox(6, icon, empty);
        box.setAlignment(Pos.CENTER_LEFT);
        proximosVencer.getChildren().add(box);
      }

    } catch (Exception ex) {
      System.err.println("Error al cargar próximos a vencer: " + ex.getMessage());
    }
  }

  private void cargarStockBajo() {
    stockBajoList.getChildren().clear();

    String sql = """
      SELECT p.nombre, p.stock_minimo, COALESCE(SUM(sl.cantidad_base), 0) AS cantidad_actual
      FROM productos p
      LEFT JOIN lotes l ON l.producto_id = p.id
      LEFT JOIN stock_lote sl ON sl.lote_id = l.id
      WHERE p.activo = 1
      GROUP BY p.id, p.nombre, p.stock_minimo
      HAVING cantidad_actual > 0 AND cantidad_actual < p.stock_minimo
      ORDER BY (cantidad_actual * 1.0 / p.stock_minimo)
      LIMIT 10
      """;

    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      boolean hayDatos = false;
      while (rs.next()) {
        hayDatos = true;
        String nombre = rs.getString("nombre");
        int stockMin = rs.getInt("stock_minimo");
        int cantidadActual = rs.getInt("cantidad_actual");

        var item = buildStockBajoItem(nombre, cantidadActual, stockMin);
        stockBajoList.getChildren().add(item);
      }

      if (!hayDatos) {
        var icon = Icons.checkCircle(16);
        icon.setStyle("-fx-background-color: #12B76A;");

        var empty = new Label(" Todos los productos tienen stock suficiente");
        empty.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");

        var box = new HBox(6, icon, empty);
        box.setAlignment(Pos.CENTER_LEFT);
        stockBajoList.getChildren().add(box);
      }

    } catch (Exception ex) {
      System.err.println("Error al cargar stock bajo: " + ex.getMessage());
    }
  }

  private Node buildAlertaItem(String nombre, String fechaVto, int cantidad) {
    var container = new HBox(12);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPadding(new Insets(14));
    container.setStyle(
      "-fx-background-color: #FEF6EE; " +
      "-fx-background-radius: 10; " +
      "-fx-border-color: #F79009; " +
      "-fx-border-radius: 10; " +
      "-fx-border-width: 1;"
    );

    var icon = Icons.clock(18);
    icon.setStyle("-fx-background-color: #F79009;");

    var info = new VBox(4);
    var lblNombre = new Label(nombre);
    lblNombre.setStyle("-fx-font-weight: 600; -fx-text-fill: #212529; -fx-font-size: 14px;");

    // Formatear fecha
    String fechaFormateada = fechaVto;
    try {
      LocalDate fecha = LocalDate.parse(fechaVto);
      long dias = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fecha);
      fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                       " • " + dias + " días • " + cantidad + " unidades";
    } catch (Exception ignored) {}

    var lblFecha = new Label(fechaFormateada);
    lblFecha.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D;");

    info.getChildren().addAll(lblNombre, lblFecha);
    HBox.setHgrow(info, Priority.ALWAYS);

    container.getChildren().addAll(icon, info);
    return container;
  }

  private Node buildStockBajoItem(String nombre, int actual, int minimo) {
    var container = new HBox(12);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPadding(new Insets(14));
    container.setStyle(
      "-fx-background-color: #FEF3F2; " +
      "-fx-background-radius: 10; " +
      "-fx-border-color: #F04438; " +
      "-fx-border-radius: 10; " +
      "-fx-border-width: 1;"
    );

    var icon = Icons.exclamationTriangle(18);
    icon.setStyle("-fx-background-color: #F04438;");

    var info = new VBox(4);
    var lblNombre = new Label(nombre);
    lblNombre.setStyle("-fx-font-weight: 600; -fx-text-fill: #212529; -fx-font-size: 14px;");

    double porcentaje = (actual * 100.0) / minimo;
    var lblStock = new Label(String.format("Stock: %d / %d • %.0f%% del mínimo", actual, minimo, porcentaje));
    lblStock.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D;");

    info.getChildren().addAll(lblNombre, lblStock);
    HBox.setHgrow(info, Priority.ALWAYS);

    container.getChildren().addAll(icon, info);
    return container;
  }

  private String hexToRgba(String hex, double opacity) {
    // Convertir hex a rgba
    int r = Integer.valueOf(hex.substring(1, 3), 16);
    int g = Integer.valueOf(hex.substring(3, 5), 16);
    int b = Integer.valueOf(hex.substring(5, 7), 16);
    return String.format("rgba(%d,%d,%d,%.2f)", r, g, b, opacity);
  }
}
