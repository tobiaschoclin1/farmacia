package com.tobias;

import com.tobias.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.flywaydb.core.Flyway;

import java.io.File;
import java.nio.file.Path;

public class App extends Application {

  private Scene scene;

  @Override public void start(Stage stage) {
    // Detectar SO y usar ruta apropiada
    String osName = System.getProperty("os.name").toLowerCase();
    Path dbDir;

    if (osName.contains("win")) {
      // Windows
      String programData = System.getenv("ProgramData");
      dbDir = Path.of(programData, "Farmacia", "data");
    } else if (osName.contains("mac")) {
      // macOS
      String home = System.getProperty("user.home");
      dbDir = Path.of(home, "Library", "Application Support", "Farmacia", "data");
    } else {
      // Linux y otros
      String home = System.getProperty("user.home");
      dbDir = Path.of(home, ".farmacia", "data");
    }

    new File(dbDir.toString()).mkdirs();
    String jdbcUrl = "jdbc:sqlite:" + dbDir.resolve("farmacia.db");

    Flyway.configure()
        .dataSource(jdbcUrl, null, null)
        .locations("classpath:db/migration")
        .load()
        .migrate();

    // Crear layout principal con contenedor de tabs
    var tabs = new TabPane();
    tabs.getTabs().addAll(
      createTab("Dashboard", new DashboardView()),
      createTab("Productos", new ProductsView()),
      createTab("Entradas", new StockEntryView()),
      createTab("Salidas", new StockOutView()),
      createTab("Stock", new StockView()),
      createTab("Vencimientos", new ExpiryView()),
      createTab("Import/Export", new ImportExportView())
    );
    tabs.getTabs().forEach(t -> t.setClosable(false));

    // Layout principal con padding
    var mainContent = new javafx.scene.layout.BorderPane();
    mainContent.getStyleClass().add("main-content");
    mainContent.setCenter(tabs);

    scene = new Scene(mainContent, 1400, 840);
    applyTheme();

    stage.setTitle("Sistema de Gestión - Farmacia");
    stage.setScene(scene);
    stage.show();
  }

  private Tab createTab(String text, javafx.scene.Node content) {
    var tab = new Tab(text, content);
    tab.setClosable(false);
    return tab;
  }

  private void applyTheme() {
    scene.getStylesheets().clear();
    var url = getClass().getResource("/ui/theme-professional.css");
    if (url != null) {
        scene.getStylesheets().add(url.toExternalForm());
    } else {
        System.err.println("No se encontró el tema profesional en classpath.");
    }
}


  public static void main(String[] args) { launch(args); }
}
