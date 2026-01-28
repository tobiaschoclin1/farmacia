package com.tobias.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class ExcelUtil {

  private ExcelUtil(){}

  // ------------------- Escritura -------------------

  public interface RowWriter {
    void write(Row row, CellStyle dateStyle, CellStyle intStyle);
  }

  public static void setString(Row row, int col, String value) {
    Cell cell = ensureCell(row, col);
    if (value == null) {
      cell.setBlank();
    } else {
      cell.setCellValue(value);
    }
  }

  public static void setInt(Row row, int col, Integer value, CellStyle intStyle) {
    Cell cell = ensureCell(row, col);
    if (value == null) {
      cell.setBlank();
    } else {
      cell.setCellValue(value);
      if (intStyle != null) cell.setCellStyle(intStyle);
    }
  }

  /** isoDate: "yyyy-MM-dd" o null/"" */
  public static void setDate(Row row, int col, String isoDate, CellStyle dateStyle) {
    Cell cell = ensureCell(row, col);
    if (isoDate == null || isoDate.isBlank()) {
      cell.setBlank();
      return;
    }
    try {
      LocalDate d = LocalDate.parse(isoDate);
      cell.setCellValue(java.sql.Date.valueOf(d));
      if (dateStyle != null) cell.setCellStyle(dateStyle);
    } catch (DateTimeParseException e) {
      // Si no es ISO, lo escribimos como texto para no perder el dato
      cell.setCellValue(isoDate);
    }
  }

  private static Cell ensureCell(Row row, int col) {
    Cell cell = row.getCell(col);
    if (cell == null) cell = row.createCell(col);
    return cell;
  }

  /** Crea un Excel de una hoja con encabezados y filas, con zebra y auto filter. */
  public static void exportSheet(File file, String sheetName, String[] headers, List<RowWriter> rows) throws Exception {
    try (Workbook wb = new XSSFWorkbook()) {
      DataFormat df = wb.createDataFormat();

      Font bold = wb.createFont(); bold.setBold(true);

      CellStyle header = wb.createCellStyle();
      header.setFont(bold);
      header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      header.setBorderBottom(BorderStyle.THIN);

      CellStyle zebra = wb.createCellStyle();
      zebra.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
      zebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);

      CellStyle dateStyle = wb.createCellStyle();
      dateStyle.setDataFormat(df.getFormat("yyyy-mm-dd"));

      CellStyle intStyle = wb.createCellStyle();
      intStyle.setDataFormat(df.getFormat("0"));

      Sheet sh = wb.createSheet(sheetName);

      // header
      Row rh = sh.createRow(0);
      for (int c=0; c<headers.length; c++) {
        Cell cell = rh.createCell(c);
        cell.setCellValue(headers[c]);
        cell.setCellStyle(header);
      }

      // rows
      for (int i=0; i<rows.size(); i++) {
        Row r = sh.createRow(i+1);
        rows.get(i).write(r, dateStyle, intStyle);

        // zebra (pares visualmente)
        if ((i & 1) == 1) {
          for (int c=0; c<headers.length; c++) {
            Cell cell = r.getCell(c);
            if (cell == null) cell = r.createCell(c);
            CellStyle s = wb.createCellStyle();
            s.cloneStyleFrom(cell.getCellStyle());
            s.setFillForegroundColor(zebra.getFillForegroundColor());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(s);
          }
        }
      }

      for (int c=0; c<headers.length; c++) sh.autoSizeColumn(c);
      sh.createFreezePane(0,1);
      sh.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, rows.size(), 0, Math.max(0, headers.length-1)));

      try (FileOutputStream out = new FileOutputStream(file)) {
        wb.write(out);
      }
    }
  }

  // ------------------- Lectura -------------------

  /** Devuelve texto limpio o null si la celda está vacía. Si es fecha formateada, la intenta devolver como ISO. */
  public static String getStringCell(Row row, int idx) {
    if (row == null) return null;
    Cell cell = row.getCell(idx);
    if (cell == null) return null;

    return switch (cell.getCellType()) {
      case STRING -> trimToNull(cell.getStringCellValue());
      case NUMERIC -> {
        if (DateUtil.isCellDateFormatted(cell)) {
          LocalDate d = cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
          yield d.toString(); // ISO
        } else {
          // Evitar notación científica: formateamos simple
          double v = cell.getNumericCellValue();
          if (v == Math.rint(v)) yield String.valueOf((long) v);
          yield String.valueOf(v);
        }
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> {
        try {
          // Intentamos como string calculado
          yield trimToNull(cell.getStringCellValue());
        } catch (Exception ignore) {
          try {
            double v = cell.getNumericCellValue();
            if (v == Math.rint(v)) yield String.valueOf((long) v);
            yield String.valueOf(v);
          } catch (Exception ignore2) {
            yield null;
          }
        }
      }
      default -> null;
    };
  }

  /** Devuelve Integer o null si vacío / no convertible. */
  public static Integer getIntegerCell(Row row, int idx) {
    String s = getStringCell(row, idx);
    if (s == null) return null;
    try {
      // Permitir "123.0" -> 123
      double d = Double.parseDouble(s.replace(',', '.'));
      return (int) Math.round(d);
    } catch (Exception e) {
      return null;
    }
  }

  /** Intenta leer fecha como ISO (yyyy-MM-dd) desde celda numérica de Excel o texto en formatos comunes. */
  public static String getDateAsIso(Row row, int idx) {
    if (row == null) return null;
    Cell cell = row.getCell(idx);
    if (cell == null) return null;

    // Si es fecha Excel
    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
      LocalDate d = cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
      return d.toString();
    }

    // Si es texto, probamos varios formatos
    String s = getStringCell(row, idx);
    if (s == null) return null;
    s = s.trim();

    // ya es ISO
    if (looksIso(s)) return s;

    String[] patterns = {
      "dd/MM/yyyy", "dd-MM-yyyy", "d/M/yyyy", "d-M-yyyy",
      "MM/dd/yyyy", "M/d/yyyy",
      "yyyy/MM/dd", "yyyy-M-d"
    };
    for (String p : patterns) {
      try {
        LocalDate d = LocalDate.parse(s, DateTimeFormatter.ofPattern(p));
        return d.toString();
      } catch (DateTimeParseException ignored) {}
    }
    // si no se puede parsear, devolvemos el texto tal cual (que luego lo trate la app)
    return s;
  }

  private static boolean looksIso(String s) {
    if (s.length() != 10) return false;
    // formato simple yyyy-MM-dd
    return s.charAt(4)=='-' && s.charAt(7)=='-';
  }

  private static String trimToNull(String v) {
    if (v == null) return null;
    String t = v.trim();
    return t.isEmpty()? null : t;
  }
}
