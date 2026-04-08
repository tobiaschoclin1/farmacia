package com.tobias.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

/**
 * Utilidad para crear iconos visuales en la aplicación
 */
public class IconHelper {

  /**
   * Crea un icono con emoji
   */
  public static Label emoji(String emoji, int size) {
    var label = new Label(emoji);
    label.setStyle("-fx-font-size: " + size + "px;");
    return label;
  }

  /**
   * Crea un icono con emoji de tamaño estándar (16px)
   */
  public static Label emoji(String emoji) {
    return emoji(emoji, 16);
  }

  /**
   * Crea un badge circular con número
   */
  public static Node badge(String text, String colorClass) {
    var label = new Label(text);
    label.getStyleClass().addAll("badge", colorClass);
    return label;
  }

  /**
   * Crea un icono circular con emoji
   */
  public static Node circularIcon(String emoji, String backgroundColor) {
    var circle = new Circle(24);
    circle.setStyle("-fx-fill: " + backgroundColor + ";");

    var label = new Label(emoji);
    label.setStyle("-fx-font-size: 20px;");

    var stack = new StackPane(circle, label);
    stack.setAlignment(Pos.CENTER);
    return stack;
  }

  /**
   * Crea un contenedor con icono y texto
   */
  public static Node iconText(String emoji, String text) {
    var icon = emoji(emoji, 14);
    var label = new Label(text);
    label.setStyle("-fx-padding: 0 0 0 6;");

    var container = new javafx.scene.layout.HBox(4, icon, label);
    container.setAlignment(Pos.CENTER_LEFT);
    return container;
  }

  // Emojis comunes para la aplicación
  public static final String HOME = "🏠";
  public static final String PRODUCTS = "💊";
  public static final String PACKAGE = "📦";
  public static final String INBOX = "📥";
  public static final String OUTBOX = "📤";
  public static final String STOCK = "📊";
  public static final String ALERT = "⏰";
  public static final String WARNING = "⚠️";
  public static final String CHECK = "✓";
  public static final String CROSS = "✗";
  public static final String PLUS = "+";
  public static final String EDIT = "✏️";
  public static final String SAVE = "💾";
  public static final String CANCEL = "✖";
  public static final String SEARCH = "🔍";
  public static final String FILTER = "🔎";
  public static final String IMPORT = "📁";
  public static final String EXPORT = "📤";
  public static final String EXCEL = "📊";
  public static final String CALENDAR = "📅";
  public static final String CHART = "📈";
  public static final String MEDICINE = "💊";
  public static final String PILL = "💊";
  public static final String SYRINGE = "💉";
  public static final String BANDAGE = "🩹";
  public static final String STETHOSCOPE = "🩺";
  public static final String INFO = "ℹ️";
  public static final String SUCCESS = "✅";
  public static final String ERROR = "❌";
  public static final String CLOCK = "🕐";
  public static final String HOURGLASS = "⏳";
  public static final String MONEY = "💰";
  public static final String CART = "🛒";
  public static final String BOX = "📦";
  public static final String TRUCK = "🚚";
  public static final String REFRESH = "🔄";
  public static final String SETTINGS = "⚙️";
  public static final String USER = "👤";
  public static final String FOLDER = "📁";
  public static final String FILE = "📄";
  public static final String TRASH = "🗑️";
  public static final String STAR = "⭐";
  public static final String HEART = "❤️";

  /**
   * Crea un SVG path para iconos más detallados
   */
  public static SVGPath createSVGIcon(String pathData, double size) {
    var svg = new SVGPath();
    svg.setContent(pathData);
    svg.setScaleX(size / 24.0);
    svg.setScaleY(size / 24.0);
    return svg;
  }
}
