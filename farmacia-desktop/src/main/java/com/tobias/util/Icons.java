package com.tobias.util;

import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;

/**
 * Sistema de iconos SVG profesionales para la aplicación
 * Iconos minimalistas y modernos sin emojis
 */
public class Icons {

  private static final String ICON_COLOR = "#6B7280"; // Gray-500

  /**
   * Crea un icono SVG con el path especificado
   */
  private static Region createIcon(String svgPath, double size) {
    SVGPath path = new SVGPath();
    path.setContent(svgPath);

    Region icon = new Region();
    icon.setShape(path);
    icon.setMinSize(size, size);
    icon.setPrefSize(size, size);
    icon.setMaxSize(size, size);
    icon.setStyle("-fx-background-color: " + ICON_COLOR + ";");

    return icon;
  }

  /**
   * Crea un icono con color personalizado
   */
  public static Region createIcon(String svgPath, double size, String color) {
    SVGPath path = new SVGPath();
    path.setContent(svgPath);

    Region icon = new Region();
    icon.setShape(path);
    icon.setMinSize(size, size);
    icon.setPrefSize(size, size);
    icon.setMaxSize(size, size);
    icon.setStyle("-fx-background-color: " + color + ";");

    return icon;
  }

  // ==================== NAVIGATION ICONS ====================

  public static Region home(double size) {
    return createIcon(
      "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6",
      size
    );
  }

  public static Region pill(double size) {
    return createIcon(
      "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z",
      size
    );
  }

  public static Region arrowDownTray(double size) {
    return createIcon(
      "M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3",
      size
    );
  }

  public static Region arrowUpTray(double size) {
    return createIcon(
      "M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5",
      size
    );
  }

  public static Region chartBar(double size) {
    return createIcon(
      "M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z",
      size
    );
  }

  public static Region clock(double size) {
    return createIcon(
      "M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z",
      size
    );
  }

  public static Region documentArrowDown(double size) {
    return createIcon(
      "M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m.75 12l3 3m0 0l3-3m-3 3v-6m-1.5-9H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z",
      size
    );
  }

  // ==================== ACTION ICONS ====================

  public static Region plus(double size) {
    return createIcon(
      "M12 4.5v15m7.5-7.5h-15",
      size
    );
  }

  public static Region pencil(double size) {
    return createIcon(
      "M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10",
      size
    );
  }

  public static Region check(double size) {
    return createIcon(
      "M4.5 12.75l6 6 9-13.5",
      size
    );
  }

  public static Region xMark(double size) {
    return createIcon(
      "M6 18L18 6M6 6l12 12",
      size
    );
  }

  public static Region magnifyingGlass(double size) {
    return createIcon(
      "M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z",
      size
    );
  }

  public static Region arrowDownOnSquare(double size) {
    return createIcon(
      "M9 8.25H7.5a2.25 2.25 0 00-2.25 2.25v9a2.25 2.25 0 002.25 2.25h9a2.25 2.25 0 002.25-2.25v-9a2.25 2.25 0 00-2.25-2.25H15m0-3l-3-3m0 0l-3 3m3-3V15",
      size
    );
  }

  public static Region trash(double size) {
    return createIcon(
      "M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0",
      size
    );
  }

  // ==================== STATUS ICONS ====================

  public static Region checkCircle(double size) {
    return createIcon(
      "M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z",
      size
    );
  }

  public static Region exclamationTriangle(double size) {
    return createIcon(
      "M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z",
      size
    );
  }

  public static Region xCircle(double size) {
    return createIcon(
      "M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z",
      size
    );
  }

  public static Region informationCircle(double size) {
    return createIcon(
      "M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z",
      size
    );
  }

  public static Region bell(double size) {
    return createIcon(
      "M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0",
      size
    );
  }

  // ==================== DATA ICONS ====================

  public static Region cubeTransparent(double size) {
    return createIcon(
      "M21 7.353v-.353m0 0a2.25 2.25 0 00-.659-1.591L16.5 1.568a2.25 2.25 0 00-1.591-.659h-.353a2.25 2.25 0 00-1.591.659L8.318 5.803A2.25 2.25 0 007.659 7.5H7.5a2.25 2.25 0 00-1.591.659L1.568 12.5a2.25 2.25 0 00-.659 1.591v.353a2.25 2.25 0 00.659 1.591l4.341 4.341a2.25 2.25 0 001.591.659h.353a2.25 2.25 0 001.591-.659l4.341-4.341a2.25 2.25 0 00.659-1.591v-.353a2.25 2.25 0 00-.659-1.591L9.841 8.818A2.25 2.25 0 009.182 7.5h-.353zm0 14.147v.353m0-.353l-4.341-4.341m0 0a2.25 2.25 0 00-.659-1.591L12 11.318m0 0l-4.341 4.341m0 0a2.25 2.25 0 01-1.591.659H5.5m14.5-7.5l-4.341-4.341m0 0A2.25 2.25 0 0014.5 3.909V3.5",
      size
    );
  }

  public static Region squares2x2(double size) {
    return createIcon(
      "M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z",
      size
    );
  }

  public static Region tag(double size) {
    return createIcon(
      "M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 005.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 009.568 3z",
      size
    );
  }

  public static Region calendar(double size) {
    return createIcon(
      "M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5",
      size
    );
  }

  // ==================== HELPER METHODS ====================

  /**
   * Agrega una clase CSS al icono
   */
  public static Region withStyleClass(Region icon, String styleClass) {
    icon.getStyleClass().add(styleClass);
    return icon;
  }

  /**
   * Crea un icono con color personalizado
   */
  public static Region withColor(String svgPath, double size, String color) {
    return createIcon(svgPath, size, color);
  }
}
