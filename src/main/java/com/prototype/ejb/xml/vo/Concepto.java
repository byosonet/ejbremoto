package com.prototype.ejb.xml.vo;

import java.io.Serializable;

public class Concepto implements Serializable{
	private static final long serialVersionUID = 1L;	
	private Double importe;
	private Double valorUnitario;
	private String descripcion;
	private String unidad;
	private int cantidad;
	
	
	public Concepto() {
		super();
	}
	
	public Concepto(Double importe, Double valorUnitario, String descripcion,
			String unidad, int cantidad) {
		super();
		this.importe = importe;
		this.valorUnitario = valorUnitario;
		this.descripcion = descripcion;
		this.unidad = unidad;
		this.cantidad = cantidad;
	}


	public Double getImporte() {
		return importe;
	}
	public void setImporte(Double importe) {
		this.importe = importe;
	}
	public Double getValorUnitario() {
		return valorUnitario;
	}
	public void setValorUnitario(Double valorUnitario) {
		this.valorUnitario = valorUnitario;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getUnidad() {
		return unidad;
	}
	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

    @Override
    public String toString() {
        return "Concepto{" + "importe=" + importe + ", valorUnitario=" + valorUnitario + ", descripcion=" + descripcion + ", unidad=" + unidad + ", cantidad=" + cantidad + '}';
    }
        
}
