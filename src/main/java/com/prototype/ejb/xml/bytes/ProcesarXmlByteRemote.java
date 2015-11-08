package com.prototype.ejb.xml.bytes;

import com.prototype.ejb.xml.exception.ParseoException;
import java.io.IOException;
import javax.ejb.Remote;

/**
 *
 * @author ulysses.mac
 */
@Remote
public interface ProcesarXmlByteRemote {
   public byte[] descargarFacturaXmlToPDF(String xmlTimbrado, String xmlPago) throws IOException, ParseoException;
   public byte[] descargarFacturaXmlToBase64(String xmlTimbrado, String xmlPago) throws IOException, ParseoException;
}
