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
  private boolean dark = false;

  @Override public void start(Stage stage) {
    String programData = System.getenv("ProgramData");
    Path dbDir = Path.of(programData, "Farmacia", "data");
    new File(dbDir.toString()).mkdirs();
    String jdbcUrl = "jdbc:sqlite:" + dbDir.resolve("farmacia.db");

    Flyway.configure()
        .dataSource(jdbcUrl, null, null)
        .locations("classpath:db/migration")
        .load()
        .migrate();

    var tabs = new TabPane();
    tabs.getTabs().addAll(
      new Tab("Productos", new ProductsView()),
      new Tab("Entradas",  new StockEntryView()),
      new Tab("Salidas", new StockOutView()),
      new Tab("Stock",     new StockView()),
      new Tab("Próx. a vencer", new ExpiryView()),
      new Tab("Import/Export", new ImportExportView())
    );
    tabs.getTabs().forEach(t -> t.setClosable(false));

    // Barra simple con “Tema”
    var root = new SplitPane();
    root.getItems().add(tabs);

    scene = new Scene(root, 1280, 760);
    applyTheme(); // carga tema por defecto (light)

    // // Menú para cambiar tema + tecla F2
    // var menu = new MenuBar();
    // var mVer = new Menu("Ver");
    // var miTema = new CheckMenuItem("Tema oscuro (F2)");
    // miTema.setSelected(dark);
    // miTema.setOnAction(e -> { dark = miTema.isSelected(); applyTheme(); });
    // mVer.getItems().add(miTema);
    // menu.getMenus().add(mVer);
    // ((SplitPane) scene.getRoot()).setDividerPositions(0);
    // // insertamos el menú arriba “sin costo” con un overlay ligero:
    // var layout = new javafx.scene.layout.BorderPane();
    // layout.setTop(menu);
    // layout.setCenter(tabs);
    // scene.setRoot(layout);

    // scene.setOnKeyPressed(ev -> {
    //   switch (ev.getCode()) {
    //     case F2 -> { dark = !dark; miTema.setSelected(dark); applyTheme(); }
    //   }
    // });

    stage.setTitle("Farmacia — Stock");
    stage.setScene(scene);
    stage.show();
  }

  private void applyTheme() {
    scene.getStylesheets().clear();
    var url = getClass().getResource("/ui/theme-light.css");
    if (url != null) {
        scene.getStylesheets().add(url.toExternalForm());
    } else {
        System.err.println("No se encontró el tema claro en classpath.");
    }
}


  public static void main(String[] args) { launch(args); }
}
