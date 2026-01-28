package com.tobias.db;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class Db {
  private static String URL;

  public static synchronized Connection get() throws Exception {
    if (URL == null) {
      String programData = System.getenv("ProgramData");
      Path dir = Path.of(programData, "Farmacia", "data");
      new File(dir.toString()).mkdirs();
      URL = "jdbc:sqlite:" + dir.resolve("farmacia.db");
    }
    return DriverManager.getConnection(URL);
  }
}
