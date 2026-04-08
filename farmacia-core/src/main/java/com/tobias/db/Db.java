package com.tobias.db;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class Db {
  private static String URL;

  public static synchronized Connection get() throws Exception {
    if (URL == null) {
      // Detectar SO y usar ruta apropiada
      String osName = System.getProperty("os.name").toLowerCase();
      Path dir;
      
      if (osName.contains("win")) {
        // Windows
        String programData = System.getenv("ProgramData");
        dir = Path.of(programData, "Farmacia", "data");
      } else if (osName.contains("mac")) {
        // macOS
        String home = System.getProperty("user.home");
        dir = Path.of(home, "Library", "Application Support", "Farmacia", "data");
      } else {
        // Linux y otros
        String home = System.getProperty("user.home");
        dir = Path.of(home, ".farmacia", "data");
      }
      
      new File(dir.toString()).mkdirs();
      URL = "jdbc:sqlite:" + dir.resolve("farmacia.db");
    }
    return DriverManager.getConnection(URL);
  }
}
