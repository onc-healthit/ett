/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.internal.ws.util.ByteArrayDataSource;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.bind.JAXB;

import org.apache.commons.io.IOUtils;
import org.w3._2003._05.soap_envelope.Body;
import org.w3._2003._05.soap_envelope.Envelope;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 * @author mccaffrey
 */
public class Soap {

    public static final String ELEMENT_NAME_METADATA_LEVEL = "metadata-level";
    public static final String ELEMENT_NAME_DIRECT_ADDRESS_BLOCK = "addressBlock";
    public static final String ELEMENT_NAME_DIRECT_FROM = "from";
    public static final String ELEMENT_NAME_DIRECT_TO = "to";
    public static final String NAMESPACE_DIRECT = "urn:direct:addressing";

    public static final String METADATA_LEVEL_MINIMAL = "minimal";
    public static final String METADATA_LEVEL_XDS = "XDS";

    /**
     * Enum type depicting the two allowed metadata level values: XDS (full) and
     * minimal.
     */
    public enum MetadataLevel {
        MINIMAL,
        XDS
    }

    public enum SimpleSoapOrMTOM {
        MTOM,
        SIMPLE_SOAP,
        UNKNOWN
    }

    /**
     * Checks if the SOAP contains a syntactically correct Direct Address Block.
     *
     * @param soap A string representing the entire SOAP wrapper.
     * @return
     */
    public static boolean isValidDirectAddressBlock(String soap) {
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(soap), Envelope.class);
        List<Object> headers = env.getHeader().getAny();
        if (headers == null) {
            return false;
        }
        Iterator it = headers.iterator();
        boolean foundDirectAddressBlock = false;
        while (it.hasNext()) {
            Element header = (Element) it.next();
            if (header.getLocalName().equals(ELEMENT_NAME_DIRECT_ADDRESS_BLOCK) && header.getNamespaceURI().equals(NAMESPACE_DIRECT)) {
                foundDirectAddressBlock = true;
                NodeList directFrom = header.getElementsByTagNameNS(NAMESPACE_DIRECT, ELEMENT_NAME_DIRECT_FROM);
                NodeList directTo = header.getElementsByTagNameNS(NAMESPACE_DIRECT, ELEMENT_NAME_DIRECT_TO);
                if (directFrom.getLength() > 0 && directTo.getLength() > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;

    }

    /**
     * Given an XML string representing a SOAP wrapper, what is the Direct Edge
     * XDR metadata level: minimal or full (XDS). To use minimal, the SOAP
     * header must be labeled as minimal. If the header doesn't exist, it must
     * be assumed to be full (XDS). Specification: XDR and XDM For Direct
     * Messaging Specification version 1.0, Section 6.1.1.
     *
     * @param soap A string representing the entire SOAP wrapper.
     * @return An enum representing one of two allowed metadata levels.
     */
    public static MetadataLevel getMetadataLevel(String soap) {
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(soap), Envelope.class);
        List<Object> headers = env.getHeader().getAny();
        if (headers == null) {
            return MetadataLevel.XDS;
        }
        Iterator it = headers.iterator();
        while (it.hasNext()) {
            Element header = (Element) it.next();
            if (header.getLocalName().equals(ELEMENT_NAME_METADATA_LEVEL) && header.getNamespaceURI().equals(NAMESPACE_DIRECT)) {
                String metadataLevel = header.getFirstChild().getTextContent();
                if (metadataLevel.equals(METADATA_LEVEL_MINIMAL)) {
                    return MetadataLevel.MINIMAL;
                } else if (metadataLevel.equals(METADATA_LEVEL_XDS)) {
                    return MetadataLevel.XDS;
                }

            }
        }

        return MetadataLevel.XDS;
    }

    private static String getDocumentFromSoap(String soap) {
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(soap), Envelope.class);

        Body body = env.getBody();
        List<Object> any = body.getAny();
        Element regresp = (Element) any.get(0);
        
        ProvideAndRegisterDocumentSetRequestType pnr = (ProvideAndRegisterDocumentSetRequestType) JAXB.unmarshal(new StringReader(MiscUtil.xmlToString(regresp)), ProvideAndRegisterDocumentSetRequestType.class);
        List<Document> documents = pnr.getDocument();
        if (!documents.isEmpty()) {
            Document document = documents.get(0);
            byte[] documentByteArray = document.getValue();
            String payload = new String(documentByteArray);
            if(Soap.isBase64Encoded(payload)){ 
                return new String(Base64.getDecoder().decode(documentByteArray));
            } else {
                return payload;
            }            
        }

        return null;
    }

        private static boolean isBase64Encoded(String stringBase64) {
        String base64Regex = "([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)";
        Pattern pattern = Pattern.compile(base64Regex);
        if (pattern.matcher(stringBase64).matches()) {
            return true;
        } else {
            return false;
        }
    }
    
    private static SimpleSoapOrMTOM isSoapOrMTOM(String incoming) throws IOException, MessagingException {
        SOAPWithAttachment swa = Soap.parseMtom(incoming);
        if(swa.getAttachment() == null || swa.getAttachment().size() == 0)
            return SimpleSoapOrMTOM.SIMPLE_SOAP;
        return SimpleSoapOrMTOM.MTOM;
    }

    private static String findContentType(String mtom) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(mtom));
        while (reader.ready()) {
            String line = reader.readLine();
            if (line.startsWith("Content-Type:")) {
                return line.substring(14);
            }
        }
        return null;
    }

    private static SOAPWithAttachment parseMtom(String mtom) throws MessagingException, IOException {

        //MimeMultipart mp = new MimeMultipart(mtom);
        // MimeMultipart mp = new MimeMultipart(6,5,4);
        MimeMultipart mp;
        String contentType = Soap.findContentType(mtom);

        mp = new MimeMultipart(new ByteArrayDataSource(mtom.getBytes(), ""), new ContentType(contentType));
        SOAPWithAttachment swa = new SOAPWithAttachment();
        int count = mp.getCount();
        for (int i = 0; i < count; i++) {
            MimeBodyPart bp = mp.getBodyPart(i);
            ByteArrayInputStream content = (ByteArrayInputStream) bp.getContent();
            String contentString = IOUtils.toString(content);
            try {
                Envelope env = (Envelope) JAXB.unmarshal(new StringReader(contentString), Envelope.class);
                if (env.getHeader() == null && env.getBody() == null) {
                    swa.getAttachment().add(Soap.read(content));
                } else {
                    swa.setSoap(contentString);
                }
            } catch (Exception saxe) {
                // Not SOAP so must be attachment.
                swa.getAttachment().add(Soap.read(content));
            }

        }
        return swa;
    }

    private static byte[] read(ByteArrayInputStream bais) throws IOException {
        byte[] array = new byte[bais.available()];
        bais.read(array);
        return array;
    }

    public final static void main(String[] args) {

        String xml;
        try {
            //  xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml", Charset.defaultCharset());

//              System.out.println(Soap.getMetadataLevel(xml));
            //          System.out.println(Soap.isValidDirectAddressBlock(xml));
            /*
          xml = MiscUtil.readFile("/home/mccaffrey/ett/parsingSamples/MTOM.txt", Charset.defaultCharset());  
  
             try {
                 SOAPWithAttachment swa = Soap.parseMtom(xml);
                 
                 System.out.println("SOAP = " + swa.getSoap());
                System.out.println("Attachment " + new String(swa.getAttachment().iterator().next()));
                 
                 
             } catch (MessagingException ex) {
                 Logger.getLogger(Soap.class.getName()).log(Level.SEVERE, null, ex);
             }
             */
            xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/Xdr_minimal_metadata.xml", Charset.defaultCharset());

            //System.out.println(Soap.getDocumentFromSoap(xml));

        } catch (Exception ex) {
            Logger.getLogger(Soap.class.getName()).log(Level.SEVERE, null, ex);
        }

        //  System.out.println(RegistryResponse.isSuccessSoap(xml));
//       
    }

}
