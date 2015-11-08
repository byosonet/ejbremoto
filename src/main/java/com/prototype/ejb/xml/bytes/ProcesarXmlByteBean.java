package com.prototype.ejb.xml.bytes;

/**
 *
 * @author ulysses.mac
 */
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.prototype.ejb.xml.exception.ParseoException;
import com.prototype.ejb.xml.vo.Concepto;
import com.prototype.ejb.xml.vo.Impuesto;
import com.prototype.ejb.xml.vo.Pago;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * Session Bean implementation class ProcesarXmlToByte
 */
@Stateless(name = "procesarXmlByteBean", mappedName = "procesarXmlByteBean")
public class ProcesarXmlByteBean implements ProcesarXmlByteRemote {

    private static final String PATH_SUB_REPORTES_FACTURA = "reportes/factura/";
    private static final String PATH_REPORTE_FACTURA = PATH_SUB_REPORTES_FACTURA + "FacturaElectronica.jasper";
    private final Logger log = Logger.getLogger(ProcesarXmlByteBean.class);

    @Override
    public byte[] descargarFacturaXmlToPDF(String xmlTimbrado, String xmlPago)
            throws IOException, ParseoException {
        
        xmlTimbrado = xmlTimbrado.replace("&lt;", "<");
        xmlTimbrado = xmlTimbrado.replace("&gt;", ">");
        
        xmlPago = xmlPago.replace("&lt;", "<");
        xmlPago = xmlPago.replace("&gt;", ">");
        xmlPago = xmlPago.replace("<mx:xmlTimbradoSAT>", "<!--");
        xmlPago = xmlPago.replace("</mx:xmlTimbradoSAT>", "-->");

        this.log.info("-- Xml Timbrado recibido: \n\n"+xmlTimbrado+"\n");
        this.log.info("-- Xml Pago recibido: \n\n"+xmlPago+"\n");
        byte[] bytePDF = null;
        Map<CampoCFDI, String> campos;
        try {
            campos = this.extraerCampos(xmlTimbrado,
                    CampoCFDI.COMPROBANTE_SELLO_DIGITAL_CFDI,
                    CampoCFDI.COMPROBANTE_FECHA_TIMBRADO,
                    CampoCFDI.COMPROBANTE_FORMA_PAGO,
                    CampoCFDI.COMPROBANTE_NO_CERTIFICADO,
                    CampoCFDI.COMPROBANTE_SUBTOTAL,
                    CampoCFDI.COMPROBANTE_TOTAL,
                    CampoCFDI.COMPROBANTE_METODO_PAGO,
                    CampoCFDI.COMPROBANTE_LUGAR_EXPEDICION,
                    CampoCFDI.EMISOR_RFC, CampoCFDI.EMISOR_NOMBRE,
                    CampoCFDI.EMISOR_CALLE, CampoCFDI.EMISOR_NO_EXTERIOR,
                    CampoCFDI.EMISOR_COLONIA, CampoCFDI.EMISOR_MUNICIPIO,
                    CampoCFDI.EMISOR_ESTADO, CampoCFDI.EMISOR_PAIS,
                    CampoCFDI.EMISOR_CODIGO_POSTAL,
                    CampoCFDI.EMISOR_REGIMEN_FISCAL,
                    CampoCFDI.EXPEDIDO_EN_CALLE,
                    CampoCFDI.EXPEDIDO_EN_NO_EXTERIOR,
                    CampoCFDI.EXPEDIDO_EN_COLONIA,
                    CampoCFDI.EXPEDIDO_EN_ESTADO, CampoCFDI.EXPEDIDO_EN_PAIS,
                    CampoCFDI.EXPEDIDO_EN_CODIGO_POSTAL,
                    CampoCFDI.RECEPTOR_RFC, CampoCFDI.RECEPTOR_PAIS,
                    CampoCFDI.TFD_UUID, CampoCFDI.TFD_FECHA_HORA_CERTIFICACION,
                    CampoCFDI.TFD_SELLO_DIGITAL_CFDI_SAT,
                    CampoCFDI.TFD_SERIE_CERTIFICADO_SAT,
                    CampoCFDI.TFD_SELLO_SAT, CampoCFDI.TFD_VERSION_SAT);

            Map<CampoCFDI, List<Concepto>> conceptos = this.extraerConceptos(xmlTimbrado, CampoCFDI.CONCEPTOS_CFDI);

            Map<CampoCFDI, String> datosPago = this.extraerDatosPago(xmlPago, CampoCFDI.REGISTRO_PATRONAL, CampoCFDI.RAZON_SOCIAL,
                    CampoCFDI.PERIODO, CampoCFDI.FOLIO_SUA, CampoCFDI.FECHA_PAGO);

            Pago pago = new Pago();
            String versionXML = "";
            if (campos != null) {
                for (Map.Entry<CampoCFDI, String> entry : campos.entrySet()) {
                    this.log.info("-- Campo Extraido:: " + entry.getKey() + " = " + entry.getValue());
                    if (entry.getKey() == CampoCFDI.TFD_UUID) {
                        pago.setFolioFiscal(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.COMPROBANTE_NO_CERTIFICADO) {
                        pago.setSerieCertificadoCSD(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.COMPROBANTE_LUGAR_EXPEDICION) {
                        pago.setLugarExpedicion(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.COMPROBANTE_FECHA_TIMBRADO) {
                        pago.setFechaHoraEmision(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.RECEPTOR_RFC) {
                        pago.setRfcReceptor(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.COMPROBANTE_SUBTOTAL) {
                        pago.setSubTotal(Double.valueOf(entry.getValue()));
                    } else if (entry.getKey() == CampoCFDI.COMPROBANTE_TOTAL) {
                        pago.setTotal(Double.valueOf(entry.getValue()));
                        pago.setTotalConLetra(this.totalConLetras(new BigDecimal(entry.getValue())));
                    } else if (entry.getKey() == CampoCFDI.COMPROBANTE_METODO_PAGO) {
                        pago.setMetodoDePago(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.COMPROBANTE_FORMA_PAGO) {
                        pago.setFormaDePago(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.TFD_SELLO_DIGITAL_CFDI_SAT) {
                        pago.setSelloDigitalCFDI(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.TFD_SELLO_SAT) {
                        pago.setSelloSAT(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.TFD_SERIE_CERTIFICADO_SAT) {
                        pago.setSerieCertificadoSAT(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.TFD_FECHA_HORA_CERTIFICACION) {
                        pago.setFechaHoraCertificacion(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.TFD_VERSION_SAT) {
                        versionXML = entry.getValue();
                    } else if (entry.getKey() == CampoCFDI.EMISOR_RFC) {
                        pago.setRfcEmisor(entry.getValue());
                    }
                }
            }

            StringBuilder sbCadenaOriginalSat = new StringBuilder();
            sbCadenaOriginalSat.append("||").append(versionXML).append("|")
                    .append(pago.getFolioFiscal()).append("|")
                    .append(pago.getFechaHoraCertificacion()).append("|")
                    .append(pago.getSelloDigitalCFDI()).append("|")
                    .append(pago.getSerieCertificadoSAT()).append("||");
            pago.setCadenaOriginalSAT(sbCadenaOriginalSat.toString());

            List<Concepto> listaConceptos = new ArrayList<Concepto>();
            if (conceptos != null) {
                for (Map.Entry<CampoCFDI, List<Concepto>> entry : conceptos
                        .entrySet()) {
                    List<Concepto> lista = entry.getValue();
                    for (Concepto concepto : lista) {
                        this.log.info("-- Campo Extraido:: Cantidad = " + concepto.getCantidad()
                                + " unidad = " + concepto.getUnidad()
                                + " descripcion = " + concepto.getDescripcion()
                                + " valorUnitario = "
                                + concepto.getValorUnitario() + " importe = "
                                + concepto.getImporte());
                        listaConceptos.add(concepto);
                    }
                }
            }

            if (datosPago != null) {
                for (Map.Entry<CampoCFDI, String> entry : datosPago.entrySet()) {
                    this.log.info("-- Campo Extraido:: " + entry.getKey() + " = " + entry.getValue());
                    if (entry.getKey() == CampoCFDI.RAZON_SOCIAL) {
                        pago.setNombreRazonSocial(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.REGISTRO_PATRONAL) {
                        pago.setNumeroRegistroPatronal(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.FOLIO_SUA) {
                        pago.setFolioSua(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.PERIODO) {
                        pago.setPeriodo(entry.getValue());
                    } else if (entry.getKey() == CampoCFDI.FECHA_PAGO) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        String dateInString = entry.getValue();
                        Date date = formatter.parse(dateInString);
                        pago.setFechaPago(entry.getValue().isEmpty() ? null : date);
                    }
                }
            }
            pago.setConceptos(listaConceptos);
            bytePDF = descargarFacturaElectronicaXmlToPDF(pago);
            this.log.info("-- Tamano de Bytes devueltos: "+bytePDF.length);
        } catch (Exception ex) {
            this.log.error(" -- Xml Timbrado Recibido: \n"+xmlTimbrado);
            this.log.error(" -- Xml Pago Recibido: \n"+xmlPago);
            throw new ParseoException("No se puede procesar el XML: " + ex.getMessage());
        }
        return bytePDF;
    }

    private byte[] descargarFacturaElectronicaXmlToPDF(Pago pago) {
        String imagenCodeQR = this.generarCadenaCodigoQR(pago);
        this.log.info("-- Generando ImageQR:: " + imagenCodeQR);
        BufferedImage img = this.generadorCodigoQrCadena(imagenCodeQR, 160, 150);
        this.log.info("-- Generando ImageBuffered:: " + img);
        byte[] bytePDF = this.descargarFacturaElectronicaXmlToByte(pago, img);
        return bytePDF;
    }

    @Override
    public byte[] descargarFacturaXmlToBase64(String xmlTimbrado, String xmlPago) throws IOException, ParseoException {
        byte[] encode64 = null;
        try {
            byte[] pdfBytes = this.descargarFacturaXmlToPDF(xmlTimbrado, xmlPago);
            Base64 base64 = new Base64();
            encode64 = base64.encode(pdfBytes);
            this.log.info("-- File Encode Base64: "+new String(encode64));
        } catch (Exception ex) {
            this.log.error(" -- Xml Timbrado Recibido: \n"+xmlTimbrado);
            this.log.error(" -- Xml Pago Recibido: \n"+xmlPago);
            throw new ParseoException("No se puede procesar el XML: " + ex.getMessage());
        }
        
        return encode64;
    }

    private static enum CampoCFDI {
        COMPROBANTE_SELLO_DIGITAL_CFDI,
        COMPROBANTE_FECHA_TIMBRADO,
        COMPROBANTE_FORMA_PAGO,
        COMPROBANTE_NO_CERTIFICADO,
        COMPROBANTE_SUBTOTAL,
        COMPROBANTE_TOTAL,
        COMPROBANTE_METODO_PAGO,
        COMPROBANTE_LUGAR_EXPEDICION,
        EMISOR_RFC,
        EMISOR_NOMBRE,
        EMISOR_CALLE,
        EMISOR_NO_EXTERIOR,
        EMISOR_COLONIA,
        EMISOR_MUNICIPIO,
        EMISOR_ESTADO,
        EMISOR_PAIS,
        EMISOR_CODIGO_POSTAL,
        EMISOR_REGIMEN_FISCAL,
        EXPEDIDO_EN_CALLE,
        EXPEDIDO_EN_NO_EXTERIOR,
        EXPEDIDO_EN_COLONIA,
        EXPEDIDO_EN_ESTADO,
        EXPEDIDO_EN_PAIS,
        EXPEDIDO_EN_CODIGO_POSTAL,
        RECEPTOR_RFC,
        RECEPTOR_PAIS,
        TFD_UUID,
        TFD_FECHA_HORA_CERTIFICACION,
        TFD_SELLO_DIGITAL_CFDI_SAT,
        TFD_SERIE_CERTIFICADO_SAT,
        TFD_SELLO_SAT,
        TFD_VERSION_SAT,
        CONCEPTOS_CFDI,
        REGISTRO_PATRONAL,
        RAZON_SOCIAL,
        PERIODO,
        FOLIO_SUA,
        FECHA_PAGO;
    }

    private Map<CampoCFDI, String> extraerDatosPago(String xml, CampoCFDI... DatosPago) throws ParseoException {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            XPathFactory factory = XPathFactory.newInstance();
            Map<CampoCFDI, String> map = new EnumMap<CampoCFDI, String>(CampoCFDI.class);

            for (CampoCFDI datoPago : DatosPago) {
                String valor = null;
                XPath xp = factory.newXPath();
                switch (datoPago) {
                    case REGISTRO_PATRONAL: {
                        NodeList result = (NodeList) xp.compile(
                                "//*[contains(name(),'mx:registroPatronal')]")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0).getFirstChild().getNodeValue() : "";
                        break;
                    }
                    case RAZON_SOCIAL: {
                        NodeList result = (NodeList) xp.compile(
                                "//*[contains(name(),'mx:nombre')]").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getFirstChild().getNodeValue() : "";
                        break;
                    }
                    case FECHA_PAGO: {
                        NodeList result = (NodeList) xp.compile(
                                "//*[contains(name(),'mx:fechaPago')]").evaluate(
                                        doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getFirstChild().getNodeValue() : "";
                        if (!valor.isEmpty()) {
                            SimpleDateFormat formatDate = new SimpleDateFormat(
                                    "yyyyMMdd");
                            SimpleDateFormat formatDateDMA = new SimpleDateFormat(
                                    "dd/MM/yyyy");
                            Date date = formatDate.parse(valor);
                            valor = formatDateDMA.format(date);
                        }
                        break;
                    }
                    case FOLIO_SUA: {
                        NodeList result = (NodeList) xp.compile(
                                "//*[contains(name(),'mx:folioSUA')]").evaluate(
                                        doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getFirstChild().getNodeValue() : "";
                        break;
                    }
                    case PERIODO: {
                        NodeList result = (NodeList) xp.compile(
                                "//*[contains(name(),'mx:periodoPago')]").evaluate(
                                        doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getFirstChild().getNodeValue() : "";
                        break;
                    }
                }
                map.put(datoPago, valor);
            }
            return map;
        } catch (ParserConfigurationException e) {
            throw new ParseoException(e.getMessage());
        } catch (SAXException e) {
            throw new ParseoException(e.getMessage());
        } catch (IOException e) {
            throw new ParseoException(e.getMessage());
        } catch (XPathExpressionException e) {
            throw new ParseoException(e.getMessage());
        } catch (DOMException e) {
            throw new ParseoException(e.getMessage());
        } catch (ParseException e) {
            throw new ParseoException(e.getMessage());
        }
    }

    private Map<CampoCFDI, String> extraerCampos(String xml,
            CampoCFDI... CamposTimbrado) throws ParseoException {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            XPathFactory factory = XPathFactory.newInstance();
            Map<CampoCFDI, String> map = new EnumMap<CampoCFDI, String>(CampoCFDI.class);

            for (CampoCFDI camposTimbrado : CamposTimbrado) {
                String valor = null;
                XPath xp = factory.newXPath();
                switch (camposTimbrado) {
                    case COMPROBANTE_SELLO_DIGITAL_CFDI: {
                        NodeList result = (NodeList) xp.compile("//@sello")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case COMPROBANTE_FECHA_TIMBRADO: {
                        NodeList result = (NodeList) xp.compile("//@fecha")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case COMPROBANTE_FORMA_PAGO: {
                        NodeList result = (NodeList) xp.compile("//@formaDePago")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case COMPROBANTE_NO_CERTIFICADO: {
                        NodeList result = (NodeList) xp.compile("//@noCertificado")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case COMPROBANTE_SUBTOTAL: {
                        NodeList result = (NodeList) xp.compile("//@subTotal")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case COMPROBANTE_TOTAL: {
                        NodeList result = (NodeList) xp.compile("//@total")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case COMPROBANTE_METODO_PAGO: {
                        NodeList result = (NodeList) xp.compile("//@metodoDePago")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case COMPROBANTE_LUGAR_EXPEDICION: {
                        NodeList result = (NodeList) xp.compile(
                                "//@LugarExpedicion").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_RFC: {
                        NodeList result = (NodeList) xp.compile("//Emisor/@rfc")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_NOMBRE: {
                        NodeList result = (NodeList) xp.compile("//Emisor/@nombre")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_CALLE: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/DomicilioFiscal/@calle").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_NO_EXTERIOR: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/DomicilioFiscal/@noExterior").evaluate(
                                        doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_COLONIA: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/DomicilioFiscal/@colonia").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_MUNICIPIO: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/DomicilioFiscal/@municipio").evaluate(
                                        doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_ESTADO: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/DomicilioFiscal/@estado").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_PAIS: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/DomicilioFiscal/@pais").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_CODIGO_POSTAL: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/DomicilioFiscal/@codigoPostal").evaluate(
                                        doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EMISOR_REGIMEN_FISCAL: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/RegimenFiscal/@Regimen").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EXPEDIDO_EN_NO_EXTERIOR: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/ExpedidoEn/@noExterior").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EXPEDIDO_EN_CALLE: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/ExpedidoEn/@calle").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EXPEDIDO_EN_COLONIA: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/ExpedidoEn/@colonia").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EXPEDIDO_EN_ESTADO: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/ExpedidoEn/@estado").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EXPEDIDO_EN_PAIS: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/ExpedidoEn/@pais").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case EXPEDIDO_EN_CODIGO_POSTAL: {
                        NodeList result = (NodeList) xp.compile(
                                "//Emisor/ExpedidoEn/@codigoPostal").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case RECEPTOR_RFC: {
                        NodeList result = (NodeList) xp.compile("//Receptor/@rfc")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case RECEPTOR_PAIS: {
                        NodeList result = (NodeList) xp.compile(
                                "//Receptor/Domicilio/@pais").evaluate(doc,
                                        XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }

                    case TFD_UUID: {
                        NodeList result = (NodeList) xp.compile(
                                "//Complemento/TimbreFiscalDigital/@UUID")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case TFD_FECHA_HORA_CERTIFICACION: {
                        NodeList result = (NodeList) xp.compile(
                                "//Complemento/TimbreFiscalDigital/@FechaTimbrado")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case TFD_SELLO_DIGITAL_CFDI_SAT: {
                        NodeList result = (NodeList) xp.compile(
                                "//Complemento/TimbreFiscalDigital/@selloCFD")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case TFD_SERIE_CERTIFICADO_SAT: {
                        NodeList result = (NodeList) xp
                                .compile(
                                        "//Complemento/TimbreFiscalDigital/@noCertificadoSAT")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case TFD_SELLO_SAT: {
                        NodeList result = (NodeList) xp.compile(
                                "//Complemento/TimbreFiscalDigital/@selloSAT")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    case TFD_VERSION_SAT: {
                        NodeList result = (NodeList) xp.compile(
                                "//Complemento/TimbreFiscalDigital/@version")
                                .evaluate(doc, XPathConstants.NODESET);
                        valor = result.item(0) != null ? result.item(0)
                                .getNodeValue() : "";
                        break;
                    }
                    default:
                        break;

                }
                map.put(camposTimbrado, valor);
            }
            return map;
        } catch (ParserConfigurationException e) {
            throw new ParseoException(e.getMessage());
        } catch (SAXException e) {
            throw new ParseoException(e.getMessage());
        } catch (IOException e) {
            throw new ParseoException(e.getMessage());
        } catch (XPathExpressionException e) {
            throw new ParseoException(e.getMessage());
        } catch (DOMException e) {
            throw new ParseoException(e.getMessage());
        }
    }

    private Map<CampoCFDI, List<Concepto>> extraerConceptos(String xml,
            CampoCFDI... Conceptos) throws ParseoException {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            XPathFactory factory = XPathFactory.newInstance();
            Map<CampoCFDI, List<Concepto>> map = new EnumMap<CampoCFDI, List<Concepto>>(CampoCFDI.class);
            List<Concepto> conceptos = new ArrayList<Concepto>();
            for (CampoCFDI campoConcepto : Conceptos) {
                XPath xp = factory.newXPath();
                switch (campoConcepto) {
                    case CONCEPTOS_CFDI: {
                        NodeList result = (NodeList) xp.compile(
                                "//Conceptos/Concepto").evaluate(doc,
                                        XPathConstants.NODESET);
                        NodeList nodes = (NodeList) result;
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Concepto concepto = new Concepto();
                            concepto.setCantidad(Double.valueOf(
                                    nodes.item(i).getAttributes()
                                    .getNamedItem("cantidad")
                                    .getNodeValue()).intValue());
                            concepto.setUnidad(nodes.item(i).getAttributes()
                                    .getNamedItem("unidad").getNodeValue());
                            concepto.setDescripcion(nodes.item(i).getAttributes()
                                    .getNamedItem("descripcion").getNodeValue());
                            concepto.setValorUnitario(Double.valueOf(nodes.item(i)
                                    .getAttributes().getNamedItem("valorUnitario")
                                    .getNodeValue()));

                            concepto.setImporte(Double.valueOf(nodes.item(i)
                                    .getAttributes().getNamedItem("importe")
                                    .getNodeValue()));
                            conceptos.add(concepto);
                        }
                        break;
                    }
                    default:
                        break;

                }
                map.put(campoConcepto, conceptos);
            }
            return map;
        } catch (ParserConfigurationException e) {
            throw new ParseoException(e.getMessage());
        } catch (SAXException e) {
            throw new ParseoException(e.getMessage());
        } catch (IOException e) {
            throw new ParseoException(e.getMessage());
        } catch (XPathExpressionException e) {
            throw new ParseoException(e.getMessage());
        } catch (DOMException e) {
            throw new ParseoException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new ParseoException(e.getMessage());
        }
    }

    private String totalConLetras(BigDecimal total) {
        String sTotal = "";
        if (total != null) {
            String[] pos = total.toString().split("\\.");
            String s1 = numeroEnTexto(Long.valueOf(pos[0]));
            s1 = s1.replace(s1.substring(0), s1.substring(0).toUpperCase());
            String s2 = "00";
            if (pos.length > 1) {
                s2 = pos[1];
                s2 = (s2.length() == 1) ? s2 + "0" : s2;
            }
            sTotal = s1 + " PESOS " + s2 + "/100 M.N.";
        }
        return sTotal;
    }

    private String numeroEnTexto(Long iNumero) {
        long iUnidad = iNumero % 10;
        iNumero = iNumero / 10;
        String sTexto = unidadEnTexto((int) iUnidad);

        long iDecena = iNumero % 10;
        iNumero = iNumero / 10;
        sTexto = ValidaDecenas(sTexto, iUnidad, iDecena, "");
        sTexto = sTexto.replaceAll("uno", "un");

        long iCentena = iNumero % 10;
        iNumero = iNumero / 10;
        sTexto = ValidaCentenas(sTexto, iUnidad, iDecena, iCentena, "");

        String sTxtCen = sTexto;
        long iMil = iNumero % 10;
        iNumero = iNumero / 10;
        if (iMil == 1) {
            sTexto = "mil " + sTexto;
        } else if (iMil > 1) {
            sTexto = unidadEnTexto((int) iMil) + " mil " + sTexto;
        }

        long iDecMil = iNumero % 10;
        iNumero = iNumero / 10;
        if (iDecMil > 0) {
            sTexto = ValidaDecenas(sTexto, iMil, iDecMil, " mil");
            sTexto = sTexto.replaceAll("uno", "un");
            sTexto = sTexto + " " + sTxtCen;
        }

        long iCienMil = iNumero % 10;
        iNumero = iNumero / 10;
        if (iCienMil > 0) {
            if (iDecMil == 0 && iMil == 1) {
                sTexto = "un " + sTexto;
            }
            sTexto = ValidaCentenas(sTexto, iMil, iDecMil, iCienMil, "mil");
        }
        String sTxtMiles = sTexto;
        long iMillon = iNumero % 10;
        iNumero = iNumero / 10;
        if (iMillon > 0) {
            sTexto = (iMillon == 1) ? "un millon " + sTexto
                    : unidadEnTexto((int) iMillon) + " millones " + sTexto;
        }

        long iDecMillones = iNumero % 10;
        iNumero = iNumero / 10;
        if (iDecMillones > 0) {
            sTexto = ValidaDecenas(sTexto, iMillon, iDecMillones, " millones");
            sTexto = sTexto.replaceAll("uno", "un");
            sTexto = sTexto + " " + sTxtMiles;
        }

        long iCienMillones = iNumero % 10;
        iNumero = iNumero / 10;
        if (iCienMillones > 0) {
            if (iDecMil == 0 && iMil == 1) {
                sTexto = "un " + sTexto;
            }
            sTexto = ValidaCentenas(sTexto, iMillon, iDecMillones, iCienMillones, "millones");
        }
        String sTxtMillones = sTexto;
        long iMilMi = iNumero % 10;
        iNumero = iNumero / 10;
        if (iMilMi > 0) {
            if (iMilMi == 1) {
                sTexto = (iMillon > 0) ? "mil " + sTexto.replaceAll("millon ", "millones ")
                        : "mil millones " + sTexto;
            } else {
                sTexto = (iMillon > 0) ? unidadEnTexto((int) iMilMi) + " mil " + sTexto
                        : unidadEnTexto((int) iMilMi) + " mil millones " + sTexto;
            }
        }

        long iDecMilMi = iNumero % 10;
        if (iDecMilMi > 0) {
            if (iMilMi == 0) {
                sTexto = (iCienMillones > 0) ? ValidaDecenas(sTexto, iMilMi, iDecMilMi, " mil ") + sTxtMillones
                        : ValidaDecenas(sTexto, iMilMi, iDecMilMi, " mil millones") + sTxtMillones;
            } else {
                sTexto = (iCienMillones > 0) ? ValidaDecenas(sTexto, iMilMi, iDecMilMi, " mil ") + sTxtMillones
                        : ValidaDecenas(sTexto, iMilMi, iDecMilMi, " mil millones ");
            }
            sTexto = sTexto.replaceAll("uno", "un");
        }
        return sTexto;
    }

    private String ValidaCentenas(String sTexto, long iUnidad,
            long iDecena, long iCentena, String numero) {
        if ((iCentena != 1) && (iCentena != 5) && (iCentena != 7) && (iCentena != 9) && (iCentena != 0)) {
            sTexto = (iDecena == 0 && iUnidad == 0) ? unidadEnTexto((int) iCentena) + "cientos " + numero + " " + sTexto
                    : unidadEnTexto((int) iCentena) + "cientos " + sTexto;
        } else if ((iCentena == 1) || (iCentena == 5) || (iCentena == 7) || (iCentena == 9)) {
            sTexto = (iDecena == 0 && iUnidad == 0) ? (iCentena == 1) ? "cien " + numero + " " + sTexto
                    : centenaEnTexto((int) iCentena) + " " + numero + " " + sTexto
                    : centenaEnTexto((int) iCentena) + " " + sTexto;
        }
        return sTexto;
    }

    private String ValidaDecenas(String sTexto, long iUnidad,
            long iDecena, String numero) {
        if ((iUnidad == 0) && (iDecena > 0)) {
            sTexto = decenaEnTexto((int) iDecena);
        } else if (iDecena == 1) {
            sTexto = decenas(10 + ((int) iUnidad));
        } else if (iDecena > 1) {
            sTexto = decenaEnTexto((int) iDecena) + " y " + unidadEnTexto((int) iUnidad);
            sTexto = sTexto.replaceAll("veinte y ", "veinti");
        }
        return sTexto + numero;
    }

    private String unidadEnTexto(int iNumero) {
        switch (iNumero) {
            case 1:
                return "uno";
            case 2:
                return "dos";
            case 3:
                return "tres";
            case 4:
                return "cuatro";
            case 5:
                return "cinco";
            case 6:
                return "seis";
            case 7:
                return "siete";
            case 8:
                return "ocho";
            case 9:
                return "nueve";
            default:
                return "";
        }
    }

    private String decenaEnTexto(int iDecena) {
        switch (iDecena) {
            case 1:
                return "diez";
            case 2:
                return "veinte";
            case 3:
                return "treinta";
            case 4:
                return "cuarenta";
            case 5:
                return "cincuenta";
            case 6:
                return "sesenta";
            case 7:
                return "setenta";
            case 8:
                return "ochenta";
            case 9:
                return "noventa";
            default:
                return "";
        }
    }

    private String decenas(int iDecena) {
        switch (iDecena) {
            case 11:
                return "once";
            case 12:
                return "doce";
            case 13:
                return "trece";
            case 14:
                return "catorce";
            case 15:
                return "quince";
            case 16:
                return "dieciseis";
            case 17:
                return "diecisiete";
            case 18:
                return "dieciocho";
            case 19:
                return "diecinueve";
            default:
                return "";
        }
    }

    private String centenaEnTexto(int iCentena) {
        switch (iCentena) {
            case 1:
                return "ciento";
            case 5:
                return "quinientos";
            case 7:
                return "setecientos";
            case 9:
                return "novecientos";
            default:
                return "";
        }
    }

    private String generarCadenaCodigoQR(Pago pago) {
        DecimalFormat df = new DecimalFormat("$ #,##0.000000");
        StringBuilder codigoQR = new StringBuilder();
        if (pago.getTotal() == null)
            pago.setTotal(0.0D);
        String totalFormat = df.format(pago.getTotal());
        totalFormat = totalFormat.replace("$", "").replace(",", "");
        codigoQR.append("?re=");
        codigoQR.append(pago.getRfcEmisor() == null ? "" : pago.getRfcEmisor());
        codigoQR.append("&rr=");
        codigoQR.append(pago.getRfcReceptor() == null ? "" : pago.getRfcReceptor());
        codigoQR.append("&tt=");
        codigoQR.append(totalFormat.trim());
        codigoQR.append("&id=");
        codigoQR.append(pago.getFolioFiscal() == null ? "" : pago.getFolioFiscal());
        return codigoQR.toString();
    }

    private BufferedImage generadorCodigoQrCadena(String cadena, int tamanioWidth, int tamanioHeight) {
        BufferedImage image = null;
        String iso88591charset = "ISO-8859-1";
        try {
            Charset charset = Charset.forName(iso88591charset);
            CharsetEncoder encoder = charset.newEncoder();
            byte[] bytes;
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(cadena));
            bytes = bbuf.array();
            String data = new String(bytes, iso88591charset);
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(data,
                    BarcodeFormat.QR_CODE, tamanioWidth, tamanioHeight);
            image = new BufferedImage(tamanioWidth, tamanioHeight, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < tamanioHeight; y++) {
                for (int x = 0; x < tamanioWidth; x++) {
                    int grayValue = (matrix.get(x, y) ? 0 : 1) & 0xff;
                    image.setRGB(x, y, (grayValue == 0 ? 0 : 0xFFFFFF));
                }
            }
        } catch (WriterException e) {
            this.log.error(e);
        } catch (CharacterCodingException e) {
            this.log.error(e);
        } catch (UnsupportedEncodingException e) {
            this.log.error(e);
        }
        return image;
    }

    private byte[] descargarFacturaElectronicaXmlToByte(Pago pago, BufferedImage imagenCodeQR) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytePDF = null;
        try {
            Map<String, Object> parametersPago = getParametrosFacturaElectronicaXMLPDF(pago, imagenCodeQR);
            JasperReport reportXmlToPdf = (JasperReport) JRLoader.loadObject(new ClassPathResource(PATH_REPORTE_FACTURA).getInputStream());
            JasperPrint print = JasperFillManager.fillReport(reportXmlToPdf, parametersPago, new JREmptyDataSource());
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
            exporter.exportReport();
            bytePDF = byteArrayOutputStream.toByteArray();
            this.log.info("-- Documento llenado correctamente.");
        } catch (JRException e) {
            this.log.error(e.getMessage());
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        return bytePDF;
    }

    private Map<String, Object> getParametrosFacturaElectronicaXMLPDF(Pago pago, BufferedImage imagenCodeQR) {
        this.log.info("-- Realizando llenado de parametros de pago: " + pago.toString());
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("SUBREPORT_DIR", new ClassPathResource(PATH_SUB_REPORTES_FACTURA).getPath());
        parameters.put("IMAGENES_DIR", new ClassPathResource("reportes/").getPath());
        parameters.put("imagenQR", imagenCodeQR);
        parameters.put("rp", getNRPFormateado(pago.getNumeroRegistroPatronal()));
        parameters.put("periodo", pago.getPeriodo());
        parameters.put("folioSua", pago.getFolioSua());
        parameters.put("fechaPago", pago.getFechaPago());
        parameters.put("folioFiscal", pago.getFolioFiscal());
        parameters.put("serieCertificadoCSD", pago.getSerieCertificadoCSD());
        parameters.put("lugarExpedicion", pago.getLugarExpedicion() == null ? "" : pago.getLugarExpedicion());
        parameters.put("fechaHoraEmision", pago.getFechaHoraEmision() == null ? "" : pago.getFechaHoraEmision());
        parameters.put("nombreRazonSocial", pago.getNombreRazonSocial());
        parameters.put("rfcReceptor", pago.getRfcReceptor());
        parameters.put("listaConceptos", eliminarConceptosEnCero(pago.getConceptos()));
        parameters.put("totalConLetra", pago.getTotalConLetra());
        parameters.put("total", pago.getTotal());
        parameters.put("subTotal", pago.getSubTotal());
        parameters.put("metodoDePago", pago.getMetodoDePago());
        parameters.put("formaDePago", pago.getFormaDePago());
        parameters.put("listaImpuestos", this.getImpuestosTotales(pago));
        parameters.put("selloDigitalCFDI", pago.getSelloDigitalCFDI());
        parameters.put("selloSAT", pago.getSelloSAT());
        parameters.put("cadenaOriginalSAT", pago.getCadenaOriginalSAT());
        parameters.put("serieCertificadoSAT", pago.getSerieCertificadoSAT());
        parameters.put("fechaHoraCertificacion", pago.getFechaHoraCertificacion());

        return parameters;
    }

    private String getNRPFormateado(String numeroRegistroPatronal) {
        if (numeroRegistroPatronal != null && numeroRegistroPatronal.length() >= 8) {
            StringBuilder nrp = new StringBuilder();
            int iCaracteres = numeroRegistroPatronal.length();
            nrp.append(numeroRegistroPatronal.substring(0, 8));
            if (iCaracteres > 8) {
                nrp.append("-");
                nrp.append(numeroRegistroPatronal.substring(8, 10));
                if (iCaracteres > 10) {
                    nrp.append("-");
                    nrp.append(numeroRegistroPatronal.substring(10));
                }
            }
            return nrp.toString();
        }
        return null;
    }

    private List<Concepto> eliminarConceptosEnCero(List<Concepto> conceptos) {
        if (!CollectionUtils.isEmpty(conceptos)) {
            List<Concepto> listaConcepto = new ArrayList<Concepto>();
            for (Concepto concepto : conceptos) {
                if (!(esValorCero(concepto.getImporte())
                        || esValorCero(concepto.getValorUnitario()))) 
                    listaConcepto.add(concepto);
            }
            return listaConcepto;
        }
        return null;
    }

    private boolean esValorCero(Double cifra) {
        if (cifra == null)
            return true;
        else {
            if ((cifra.compareTo(0.00D) <= 0))
                return true;
        }
        return false;
    }

    private List<Impuesto> getImpuestosTotales(Pago pago) {
        List<Impuesto> listaTotalImpuestos;
        listaTotalImpuestos = pago.getImpuestosTrasladados();
        if (!CollectionUtils.isEmpty(pago.getImpuestosRetenidos())) {
            if (!CollectionUtils.isEmpty(listaTotalImpuestos))
                listaTotalImpuestos.addAll(pago.getImpuestosRetenidos());
            else 
                listaTotalImpuestos = pago.getImpuestosRetenidos();
        }
        return eliminarImpuestosEnCero(listaTotalImpuestos);
    }

    private List<Impuesto> eliminarImpuestosEnCero(List<Impuesto> impuestos) {
        if (!CollectionUtils.isEmpty(impuestos)) {
            List<Impuesto> listaImpuestos = new ArrayList<Impuesto>();
            for (Impuesto impuesto : impuestos) {
                if (!(esValorCero(impuesto.getImporte()))) 
                    listaImpuestos.add(impuesto);
            }
            return listaImpuestos;
        }
        return null;
    }
}
