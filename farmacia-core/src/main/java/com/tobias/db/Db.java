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
      // Normalizar la URL para que siempre tenga el prefijo jdbc:
      dbUrl = normalizeJdbcUrl(dbUrl);

      // Limpiar parámetros incompatibles de Neon
      dbUrl = cleanNeonParameters(dbUrl);

      // Extraer credenciales si están embebidas en la URL
      String[] extracted = extractCredentialsFromUrl(dbUrl);
      URL = extracted[0];
      USER = extracted[1] != null ? extracted[1] : dbUser;
      PASSWORD = extracted[2] != null ? extracted[2] : dbPassword;

      System.out.println("Db: Using PostgreSQL from environment variables");
      System.out.println("Db: URL normalized to: " + maskPassword(URL));
      System.out.println("Db: Using username: " + USER);
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

  private static String normalizeJdbcUrl(String url) {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("Database URL cannot be null or empty");
    }

    // Si ya tiene el prefijo jdbc:, devolverla tal cual
    if (url.startsWith("jdbc:")) {
      return url;
    }

    // Si empieza con postgresql://, agregar jdbc: al inicio
    if (url.startsWith("postgresql://")) {
      return "jdbc:" + url;
    }

    // Si empieza con postgres://, convertir a jdbc:postgresql://
    if (url.startsWith("postgres://")) {
      return "jdbc:postgresql://" + url.substring("postgres://".length());
    }

    // Si no reconocemos el formato, lanzar excepción
    throw new IllegalArgumentException("Unsupported database URL format: " + url);
  }

  private static String cleanNeonParameters(String url) {
    if (url == null) return null;

    // Eliminar channel_binding=require (incompatible con algunos drivers PostgreSQL)
    url = url.replaceAll("[&?]channel_binding=require", "");

    // Si quedó con ? al final, eliminarlo
    url = url.replaceAll("\\?$", "");

    // Si quedó con & duplicado, limpiarlo
    url = url.replaceAll("&&", "&");

    // Si el primer parámetro quedó con &, cambiarlo a ?
    url = url.replaceAll("\\?&", "?");

    return url;
  }

  private static String[] extractCredentialsFromUrl(String url) {
    if (url == null || !url.contains("@")) {
      // No hay credenciales en la URL
      return new String[]{url, null, null};
    }

    try {
      // Patrón: jdbc:postgresql://username:password@host:port/database?params
      String protocol = url.substring(0, url.indexOf("://") + 3);  // "jdbc:postgresql://"
      String rest = url.substring(url.indexOf("://") + 3);  // "user:pass@host:port/db?params"

      if (!rest.contains("@")) {
        return new String[]{url, null, null};
      }

      String credentials = rest.substring(0, rest.indexOf("@"));  // "user:pass"
      String hostAndRest = rest.substring(rest.indexOf("@") + 1);  // "host:port/db?params"

      String extractedUsername = null;
      String extractedPassword = null;

      if (credentials.contains(":")) {
        extractedUsername = credentials.substring(0, credentials.indexOf(":"));
        extractedPassword = credentials.substring(credentials.indexOf(":") + 1);
      } else {
        extractedUsername = credentials;
      }

      String cleanUrl = protocol + hostAndRest;

      return new String[]{cleanUrl, extractedUsername, extractedPassword};

    } catch (Exception e) {
      System.err.println("Error extracting credentials from URL: " + e.getMessage());
      return new String[]{url, null, null};
    }
  }

  private static String maskPassword(String url) {
    if (url == null) return null;
    return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
  }
}
