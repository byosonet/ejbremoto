package com.prototype.ejb.xml.bytes;

import com.prototype.ejb.xml.exception.ParseoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;


public class XmlToBytesEjb {
    @Test
     public void ClienteEJB(String modo, boolean encode) throws NamingException, ParseoException, IOException {
        System.setProperty("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
        System.setProperty("org.omg.CORBA.ORBInitialPort", "15117");
        InitialContext jndi = new InitialContext();
        ProcesarXmlByteRemote remoto;
        ProcesarXmlByteBean   local;
        byte[] archivo = null;
        try {
            File fileTimbrado = new File("C:\\Users\\User\\Documents\\NetBeansProjects\\EJB-xml-bytes\\src\\test\\java\\com\\prototype\\ejb\\xml\\bytes\\CFDI_COD.xml");
            String xmlTimbrado = new String(Files.readAllBytes(Paths.get(fileTimbrado.toString())));
            
            File filePago = new File("C:\\Users\\User\\Documents\\NetBeansProjects\\EJB-xml-bytes\\src\\test\\java\\com\\prototype\\ejb\\xml\\bytes\\PAGO_COD.xml");
            String xmlPago = new String(Files.readAllBytes(Paths.get(filePago.toString())));
            
            if(modo.equals("REMOTO")){
                remoto = (ProcesarXmlByteRemote) jndi.lookup("java:global/EJB-xml-bytes-1.0/procesarXmlByteBean");
                if(encode)
                    archivo = remoto.descargarFacturaXmlToBase64(xmlTimbrado, xmlPago);
                else
                    archivo = remoto.descargarFacturaXmlToPDF(xmlTimbrado, xmlPago);
            }
            else if(modo.equals("LOCAL")){
                local = new ProcesarXmlByteBean();
                if(encode)
                    archivo = local.descargarFacturaXmlToBase64(xmlTimbrado, xmlPago);
                else
                    archivo = local.descargarFacturaXmlToPDF(xmlTimbrado, xmlPago);
            }
            
            
            Base64 base64 = new Base64();
            System.err.println(" -- EL ARCHIVO TIENE ENCODING BASE64: " + (encode?"TRUE":"FALSE"));
            System.err.println(" -- ARCHIVO CREADO::: " + "PRUEBA_IMSS.PDF");
            File fichero = new File("C:\\Users\\User\\Documents\\NetBeansProjects\\EJB-xml-bytes\\src\\test\\java\\com\\prototype\\ejb\\xml\\bytes\\PRUEBA_IMSS.PDF");
            FileOutputStream output = new FileOutputStream(fichero);
            output.write(encode?base64.decode(archivo):archivo);
            output.close();
            System.err.println(" -- PATH DEL ARCHIVO::: " + fichero.getAbsolutePath());
            //Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + fichero.getAbsolutePath());
            
        
        } catch (NamingException ex) {
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } catch (ParseoException ex) {
            System.err.println(ex.getMessage());
        }
    }
   
    public static void main(String args[]){
        try {
            String modo = "REMOTO";
            boolean retornoBase64 = false;
            XmlToBytesEjb cte = new XmlToBytesEjb();
            int numeroVecesEjecutar = 1;
            for(int i=0; i<numeroVecesEjecutar; i++){
                cte.ClienteEJB(modo,retornoBase64);
            }
                System.err.println(" -- EJB EJECUTADO EN MODO: "+modo);
                System.err.println(" -- NUMERO DE EJECUCIONES: "+numeroVecesEjecutar);
        } catch (NamingException ex) {
            System.err.println(" -- No se puede procesar " + ex.getMessage());
        } catch (ParseoException ex) {
            System.err.println(" -- No se puede procesar " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println(" -- No se puede procesar " + ex.getMessage());
        }
    }
}
