package com.prototype.ejb.xml.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Pago implements Serializable{
	private static final long serialVersionUID = 1L;
	private String numeroRegistroPatronal;
	private String rfc;
	private String periodo;
	private String folioSua;
	private Date fechaPago;
	private String strFechaPago;
	private String xmlComprobante;
	
	private String folioFiscal;
	private String serieCertificadoCSD;
	private String lugarExpedicion;
	private String fechaHoraEmision;
	private String regimenFiscal;
	private String nombreRazonSocial;
	private String rfcEmisor;
	private String rfcReceptor;	
	private List<Concepto> conceptos;	
	private Double total;
	private Double subTotal;
	private String metodoDePago;
	private String formaDePago;
	private String totalConLetra;
	private List<Impuesto> impuestosTrasladados;
	private List<Impuesto> impuestosRetenidos;	
	private String selloDigitalCFDI;
	private String selloSAT;
	private String cadenaOriginalSAT;
	private String serieCertificadoSAT;
	private String fechaHoraCertificacion;
	public String getNumeroRegistroPatronal() {
		return numeroRegistroPatronal;
	}
	public void setNumeroRegistroPatronal(String numeroRegistroPatronal) {
		this.numeroRegistroPatronal = numeroRegistroPatronal;
	}
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public String getPeriodo() {
		return periodo;
	}
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}
	public String getFolioSua() {
		return folioSua;
	}
	public void setFolioSua(String folioSua) {
		this.folioSua = folioSua;
	}
	public Date getFechaPago() {
		return fechaPago;
	}
	public void setFechaPago(Date fechaPago) {
		this.fechaPago = fechaPago;
	}
	public String getStrFechaPago() {
		return strFechaPago;
	}
	public void setStrFechaPago(String strFechaPago) {
		this.strFechaPago = strFechaPago;
	}
	public String getXmlComprobante() {
		return xmlComprobante;
	}
	public void setXmlComprobante(String xmlComprobante) {
		this.xmlComprobante = xmlComprobante;
	}
	public String getFolioFiscal() {
		return folioFiscal;
	}
	public void setFolioFiscal(String folioFiscal) {
		this.folioFiscal = folioFiscal;
	}
	public String getSerieCertificadoCSD() {
		return serieCertificadoCSD;
	}
	public void setSerieCertificadoCSD(String serieCertificadoCSD) {
		this.serieCertificadoCSD = serieCertificadoCSD;
	}
	public String getLugarExpedicion() {
		return lugarExpedicion;
	}
	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}
	public String getFechaHoraEmision() {
		return fechaHoraEmision;
	}
	public void setFechaHoraEmision(String fechaHoraEmision) {
		this.fechaHoraEmision = fechaHoraEmision;
	}
	public String getRegimenFiscal() {
		return regimenFiscal;
	}
	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}
	public String getNombreRazonSocial() {
		return nombreRazonSocial;
	}
	public void setNombreRazonSocial(String nombreRazonSocial) {
		this.nombreRazonSocial = nombreRazonSocial;
	}
	public String getRfcEmisor() {
		return rfcEmisor;
	}
	public void setRfcEmisor(String rfcEmisor) {
		this.rfcEmisor = rfcEmisor;
	}
	public String getRfcReceptor() {
		return rfcReceptor;
	}
	public void setRfcReceptor(String rfcReceptor) {
		this.rfcReceptor = rfcReceptor;
	}
	public List<Concepto> getConceptos() {
		return conceptos;
	}
	public void setConceptos(List<Concepto> conceptos) {
		this.conceptos = conceptos;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		this.total = total;
	}
	public Double getSubTotal() {
		return subTotal;
	}
	public void setSubTotal(Double subTotal) {
		this.subTotal = subTotal;
	}
	public String getMetodoDePago() {
		return metodoDePago;
	}
	public void setMetodoDePago(String metodoDePago) {
		this.metodoDePago = metodoDePago;
	}
	public String getFormaDePago() {
		return formaDePago;
	}
	public void setFormaDePago(String formaDePago) {
		this.formaDePago = formaDePago;
	}
	public String getTotalConLetra() {
		return totalConLetra;
	}
	public void setTotalConLetra(String totalConLetra) {
		this.totalConLetra = totalConLetra;
	}
	public String getSelloDigitalCFDI() {
		return selloDigitalCFDI;
	}
	public void setSelloDigitalCFDI(String selloDigitalCFDI) {
		this.selloDigitalCFDI = selloDigitalCFDI;
	}
	public String getSelloSAT() {
		return selloSAT;
	}
	public void setSelloSAT(String selloSAT) {
		this.selloSAT = selloSAT;
	}
	public String getCadenaOriginalSAT() {
		return cadenaOriginalSAT;
	}
	public void setCadenaOriginalSAT(String cadenaOriginalSAT) {
		this.cadenaOriginalSAT = cadenaOriginalSAT;
	}
	public String getSerieCertificadoSAT() {
		return serieCertificadoSAT;
	}
	public void setSerieCertificadoSAT(String serieCertificadoSAT) {
		this.serieCertificadoSAT = serieCertificadoSAT;
	}
	public String getFechaHoraCertificacion() {
		return fechaHoraCertificacion;
	}
	public void setFechaHoraCertificacion(String fechaHoraCertificacion) {
		this.fechaHoraCertificacion = fechaHoraCertificacion;
	}
	public List<Impuesto> getImpuestosTrasladados() {
		return impuestosTrasladados;
	}
	public void setImpuestosTrasladados(List<Impuesto> impuestosTrasladados) {
		this.impuestosTrasladados = impuestosTrasladados;
	}
	public List<Impuesto> getImpuestosRetenidos() {
		return impuestosRetenidos;
	}
	public void setImpuestosRetenidos(List<Impuesto> impuestosRetenidos) {
		this.impuestosRetenidos = impuestosRetenidos;
	}

    @Override
    public String toString() {
        return "Pago{" + "numeroRegistroPatronal=" + numeroRegistroPatronal + ", rfc=" + rfc + ", periodo=" + periodo + ", folioSua=" + folioSua + ", fechaPago=" + fechaPago + ", strFechaPago=" + strFechaPago + ", xmlComprobante=" + xmlComprobante + ", folioFiscal=" + folioFiscal + ", serieCertificadoCSD=" + serieCertificadoCSD + ", lugarExpedicion=" + lugarExpedicion + ", fechaHoraEmision=" + fechaHoraEmision + ", regimenFiscal=" + regimenFiscal + ", nombreRazonSocial=" + nombreRazonSocial + ", rfcEmisor=" + rfcEmisor + ", rfcReceptor=" + rfcReceptor + ", conceptos=" + conceptos + ", total=" + total + ", subTotal=" + subTotal + ", metodoDePago=" + metodoDePago + ", formaDePago=" + formaDePago + ", totalConLetra=" + totalConLetra + ", impuestosTrasladados=" + impuestosTrasladados + ", impuestosRetenidos=" + impuestosRetenidos + ", selloDigitalCFDI=" + selloDigitalCFDI + ", selloSAT=" + selloSAT + ", cadenaOriginalSAT=" + cadenaOriginalSAT + ", serieCertificadoSAT=" + serieCertificadoSAT + ", fechaHoraCertificacion=" + fechaHoraCertificacion + '}';
    }
	
	
}
