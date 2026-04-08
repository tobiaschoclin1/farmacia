package com.tobias.model;

public class Product {
  private Integer id;
  private String codigoBarra;
  private String nombre;
  private String unidadBase;        // TABLETA|UNI|ML|GR
  private Integer unidadesPorCaja;  // null si no aplica
  private Integer stockMinimo;
  private boolean activo;

  public Product() {}

  public Product(Integer id, String codigoBarra, String nombre, String unidadBase,
                 Integer unidadesPorCaja, Integer stockMinimo, boolean activo) {
    this.id = id;
    this.codigoBarra = codigoBarra;
    this.nombre = nombre;
    this.unidadBase = unidadBase;
    this.unidadesPorCaja = unidadesPorCaja;
    this.stockMinimo = stockMinimo;
    this.activo = activo;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getCodigoBarra() { return codigoBarra; }
  public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getUnidadBase() { return unidadBase; }
  public void setUnidadBase(String unidadBase) { this.unidadBase = unidadBase; }
  public Integer getUnidadesPorCaja() { return unidadesPorCaja; }
  public void setUnidadesPorCaja(Integer unidadesPorCaja) { this.unidadesPorCaja = unidadesPorCaja; }
  public Integer getStockMinimo() { return stockMinimo; }
  public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
}
