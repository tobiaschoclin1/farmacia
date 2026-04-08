package com.tobias.model;

import java.time.LocalDate;

public class EntradaItem {
  private int productoId;
  private String nombreProducto;
  private LocalDate fechaLote;        // fecha de ingreso
  private LocalDate fechaVencimiento; // puede ser null
  private int cantidadBase;           // en unidad_base

  public EntradaItem(int productoId, String nombreProducto, LocalDate fechaLote, LocalDate fechaVencimiento, int cantidadBase) {
    this.productoId = productoId;
    this.nombreProducto = nombreProducto;
    this.fechaLote = fechaLote;
    this.fechaVencimiento = fechaVencimiento;
    this.cantidadBase = cantidadBase;
  }

  public int getProductoId() { return productoId; }
  public String getNombreProducto() { return nombreProducto; }
  public LocalDate getFechaLote() { return fechaLote; }
  public LocalDate getFechaVencimiento() { return fechaVencimiento; }
  public int getCantidadBase() { return cantidadBase; }
}
