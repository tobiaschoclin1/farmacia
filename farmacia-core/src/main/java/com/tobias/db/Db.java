package com.tobias.db;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class Db {
  private static String URL;
  private static String USER;
  private static String PASSWORD;
  private static boolean initialized = false;

  public static synchronized Connection get() throws Exception {
    if (!initialized) {
      initialize();
      initialized = true;
    }

    if (USER != null && PASSWORD != null) {
      // PostgreSQL con credenciales
      return DriverManager.getConnection(URL, USER, PASSWORD);
    } else {
      // SQLite sin credenciales
      return DriverManager.getConnection(URL);
    }
  }

  private static void initialize() {
    // Prioridad 1: Variables de entorno (producción - PostgreSQL)
    String dbUrl = System.getenv("DATABASE_URL");
    String dbUser = System.getenv("DATABASE_USER");
    String dbPassword = System.getenv("DATABASE_PASSWORD");

    if (dbUrl != null && !dbUrl.isEmpty()) {
      // Modo PostgreSQL (producción)
      URL = dbUrl;
      USER = dbUser;
      PASSWORD = dbPassword;
      System.out.println("Db: Using PostgreSQL from environment variables");
      return;
    }

    // Prioridad 2: System properties (desarrollo con Spring Boot)
    dbUrl = System.getProperty("spring.datasource.url");
    dbUser = System.getProperty("spring.datasource.username");
    dbPassword = System.getProperty("spring.datasource.password");

    if (dbUrl != null && !dbUrl.isEmpty()) {
      URL = dbUrl;
      USER = dbUser;
      PASSWORD = dbPassword;
      System.out.println("Db: Using database from Spring properties");
      return;
    }

    // Prioridad 3: SQLite local (desarrollo desktop y fallback)
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
    USER = null;
    PASSWORD = null;
    System.out.println("Db: Using SQLite at " + URL);
  }
}
