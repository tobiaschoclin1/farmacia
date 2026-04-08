package com.tobias.util;

import javafx.beans.property.SimpleLongProperty;

/** Bus minimalista para avisar cambios de stock entre pantallas. */
public final class AppBus {
  private static final SimpleLongProperty STOCK_TICK = new SimpleLongProperty(0);

  /** Llamar luego de entradas/salidas o cambios que afecten el stock. */
  public static void fireStockChanged() {
    STOCK_TICK.set(STOCK_TICK.get() + 1);
  }

  /** Suscribirse para refrescar vistas cuando cambia el stock. */
  public static void onStockChanged(Runnable r) {
    STOCK_TICK.addListener((obs, oldV, newV) -> r.run());
  }

  private AppBus() {}
}
