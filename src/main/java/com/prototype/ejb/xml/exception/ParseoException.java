package com.prototype.ejb.xml.exception;

public class ParseoException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3412489353699451687L;
	
	private String situacion;
	
	public ParseoException() {
		super();
	}

	public ParseoException(String situacion) {
		super(situacion);
		this.situacion = situacion;
	}
	
	public String getSituacion() {
		return situacion;
	}

	public void setSituacion(String situacion) {
		this.situacion = situacion;
	}
	
}