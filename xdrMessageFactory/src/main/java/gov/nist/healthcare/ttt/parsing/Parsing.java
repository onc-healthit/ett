/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.mail.util.SharedByteArrayInputStream;
import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3._2003._05.soap_envelope.Body;
import org.w3._2003._05.soap_envelope.Envelope;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author mccaffrey
 */
public class Parsing {

    public static final String ELEMENT_NAME_METADATA_LEVEL = "metadata-level";
    public static final String ELEMENT_NAME_DIRECT_ADDRESS_BLOCK = "addressBlock";
    public static final String ELEMENT_NAME_DIRECT_FROM = "from";
    public static final String ELEMENT_NAME_DIRECT_TO = "to";
    public static final String NAMESPACE_DIRECT = "urn:direct:addressing";

    public static final String ELEMENT_NAME_WSA_MESSAGEID = "MessageID";
    public static final String NAMESPACE_WSA = "http://www.w3.org/2005/08/addressing";

    public static final String ELEMENT_NAME_WSSE_SECURITY = "Security";
    public static final String NAMESPACE_WSSE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"; //"http://schemas.xmlsoap.org/ws/2002/12/secext";

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

    public static String getWsseHeaderFromMTOM(String mtom) throws IOException, MessagingException {

        SOAPWithAttachment swa = Parsing.parseMtom(mtom);
        String wsseHeader = null;
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(swa.getSoap()), Envelope.class);
        List<Object> headers = env.getHeader().getAny();
        if (headers == null) {
            return null;
        }
        Iterator it = headers.iterator();
        while (it.hasNext()) {
            Element header = (Element) it.next();
            if (header.getLocalName().equals(ELEMENT_NAME_WSSE_SECURITY) && header.getNamespaceURI().equals(NAMESPACE_WSSE)) {
                wsseHeader = MiscUtil.xmlToString(header);
                break;
            }
        }
        return wsseHeader;
    }

    public static String getPatientIDFromWsse(String mtom) throws IOException, MessagingException, SAXException, ParserConfigurationException {
        String wsseHeader = Parsing.getWsseHeaderFromMTOM(mtom);
        if (wsseHeader == null)
	              return ""; // NOSAML
        String patientId = null;
      //  Node security = JAXB.unmarshal(new StringReader(wsseHeader), Node.class);
        Node securityDoc = MiscUtil.stringToDom(wsseHeader);
        
        Node security = securityDoc.getFirstChild();        
        
        NodeList securityChildren = security.getChildNodes();
        for (int i = 0; i < securityChildren.getLength(); i++) {
            Node securityChild = securityChildren.item(i);           
            if (securityChild.getLocalName() != null && securityChild.getLocalName().equals("Assertion") && securityChild.getNamespaceURI().equals("urn:oasis:names:tc:SAML:2.0:assertion")) {
                Node assertion = securityChild;
                NodeList assertionChildren = assertion.getChildNodes();
                for (int j = 0; j < assertionChildren.getLength(); j++) {
                    Node assertionChild = assertionChildren.item(j);

                    if (assertionChild.getLocalName().equals("AttributeStatement") && assertionChild.getNamespaceURI().equals("urn:oasis:names:tc:SAML:2.0:assertion")) {
                        Node attributeStatement = assertionChild;
                        NodeList attributeStatementChildren = attributeStatement.getChildNodes();
                        for (int k = 0; k < attributeStatementChildren.getLength(); k++) {
                            Node attributeStatementChild = attributeStatementChildren.item(k);
                            if (attributeStatementChild.getLocalName().equals("Attribute") && attributeStatementChild.getNamespaceURI().equals("urn:oasis:names:tc:SAML:2.0:assertion")) {
                                Element attribute = (Element) attributeStatementChild;
                                if(attribute.getAttribute("Name").equals("urn:oasis:names:tc:xacml:2.0:resource:resource-id")) {
                                    NodeList attributeChildren = attribute.getChildNodes();
                                    for(int l = 0; l < attributeChildren.getLength(); l++){
                                        Node attributeChild = attributeChildren.item(l);
                                        if (attributeChild.getLocalName().equals("AttributeValue") && attributeChild.getNamespaceURI().equals("urn:oasis:names:tc:SAML:2.0:assertion")) {
                                            Node attributeValue = attributeChild;
                                            return attributeValue.getFirstChild().getNodeValue();
                                        }     
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return patientId;
    }

    public static boolean isValidDirectAddressBlock(String mtom) throws IOException, MessagingException {

        SOAPWithAttachment swa = Parsing.parseMtom(mtom);
        return Parsing.isSoapValidDirectAddressBlock(swa.getSoap());

    }

    private static boolean isSoapValidDirectAddressBlock(String soap) {
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

    private static String findSoapOfRegistryResponse(String mtom) throws IOException {

        BufferedReader reader = new BufferedReader(new StringReader(mtom));
        StringBuilder sb = new StringBuilder();
        boolean inSoap = false;
        while (reader.ready()) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (line.indexOf("Envelope") != -1) {
                inSoap = true;
            }
            if (inSoap && line.startsWith("--")) {
                break;
            }
            if (inSoap) {
                sb.append(line);
            }

        }
        return sb.toString();

    }

    public static boolean isRegistryResponseSuccessFullHeaders(String mtom) throws IOException {
        String soapEnv = Parsing.findSoapOfRegistryResponse(mtom);
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(soapEnv), Envelope.class);

        List<Object> body = env.getBody().getAny();
        Iterator it = body.iterator();
        if (it.hasNext()) {
            Element next = (Element) it.next();
            String regRep = MiscUtil.xmlToString(next);
            return Parsing.isRegistryResponseSuccess(regRep);
        } else {
            return false;
        }

    }

    public static boolean isRegistryResponseSuccess(String regResp) {
        return RegistryResponse.isSuccess(regResp);
    }

    public static Collection<ErrorItem> getRegistryResponseErrorReport(String regResp) {
        return RegistryResponse.getErrorReport(regResp);
    }

    /*
    public static boolean isRegistryResponseSuccess(String responseBody) throws MessagingException, IOException {
        
        String soap = findSoapOfRegistryResponse(responseBody);
        return RegistryResponse.isSuccessSoap(soap);
        
        
    }*/
    public static MetadataLevel getMetadataLevel(String mtom) throws MessagingException, IOException {

        SOAPWithAttachment swa = Parsing.parseMtom(mtom);
        return Parsing.getMetadataLevelFromSoap(swa.getSoap());
    }

    private static MetadataLevel getMetadataLevelFromSoap(String soap) {
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
            } else if (header.getLocalName().equals(ELEMENT_NAME_DIRECT_ADDRESS_BLOCK) && header.getNamespaceURI().equals(NAMESPACE_DIRECT)) {
                Element directAddressBlock = header;
                NodeList childrenDirectAddressBlock = directAddressBlock.getChildNodes();
                for (int i = 0; i < childrenDirectAddressBlock.getLength(); i++) {
                    Element child = null;
                    if (childrenDirectAddressBlock.item(i) instanceof Element) {
                        child = (Element) childrenDirectAddressBlock.item(i);
                        if (child.getLocalName().equals(ELEMENT_NAME_METADATA_LEVEL) && child.getNamespaceURI().equals(NAMESPACE_DIRECT)) {
                            String metadataLevel = child.getFirstChild().getTextContent();
                            if (metadataLevel.equals(METADATA_LEVEL_MINIMAL)) {
                                return MetadataLevel.MINIMAL;
                            } else if (metadataLevel.equals(METADATA_LEVEL_XDS)) {
                                return MetadataLevel.XDS;
                            }
                        }
                    }
                }
            }
        }
        return MetadataLevel.XDS;
    }

    public static DirectAddressing getDirectAddressing(String mtom) throws MessagingException, IOException {

        SOAPWithAttachment swa = Parsing.parseMtom(mtom);
        DirectAddressing directAddressing = new DirectAddressing();
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(swa.getSoap()), Envelope.class);
        List<Object> headers = env.getHeader().getAny();
        if (headers == null) {
            return directAddressing;
        }
        Iterator it = headers.iterator();
        boolean foundDirectAddressBlock = false;
        while (it.hasNext()) {
            Element header = (Element) it.next();
            if (header.getLocalName().equals(ELEMENT_NAME_DIRECT_ADDRESS_BLOCK) && header.getNamespaceURI().equals(NAMESPACE_DIRECT)) {
                foundDirectAddressBlock = true;
                NodeList directFrom = header.getElementsByTagNameNS(NAMESPACE_DIRECT, ELEMENT_NAME_DIRECT_FROM);
                NodeList directTo = header.getElementsByTagNameNS(NAMESPACE_DIRECT, ELEMENT_NAME_DIRECT_TO);

                directAddressing.setDirectFrom(directFrom.item(0).getFirstChild().getNodeValue());
                directAddressing.setDirectTo(directTo.item(0).getFirstChild().getNodeValue());
            } else if (header.getLocalName().equals(ELEMENT_NAME_WSA_MESSAGEID) && header.getNamespaceURI().equals(NAMESPACE_WSA)) {
                directAddressing.setMessageID(header.getFirstChild().getNodeValue());
            }
        }
        return directAddressing;
    }

    public static boolean isValidDirectDisposition(String mtom) throws MessagingException, IOException {
        SOAPWithAttachment swa = Parsing.parseMtom(mtom);
        Collection<byte[]> documents = swa.getAttachment();
        if (documents == null || documents.size() == 0) {
            return false;
        }
        String directDis = new String((byte[]) documents.toArray()[0]);
        return DirectDisposition.isValidDirectDisposition(directDis);

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
            if (Parsing.isBase64Encoded(payload)) {
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

    /* Wrong
    private static SimpleSoapOrMTOM isSoapOrMTOM(String incoming) throws IOException, MessagingException {
        SOAPWithAttachment swa = Parsing.parseMtom(incoming);
        if (swa.getAttachment() == null || swa.getAttachment().size() == 0) {
            return SimpleSoapOrMTOM.SIMPLE_SOAP;
        }
        return SimpleSoapOrMTOM.MTOM;
    }
     */
    private static String findContentType(String mtom) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(mtom));
        while (reader.ready()) {
            String line = reader.readLine();
            if (line.startsWith("Content-Type:") || line.startsWith("content-type:")) {
                return line.substring(14);
            }
        }
        return null;
    }

    private static void fixMissingEndBoundry(String mtom) throws IOException {

        BufferedReader reader = new BufferedReader(new StringReader(mtom));
        String line = null;
        String lastLine = null;
        String boundryString = null;
        while (reader.ready()) {
            line = reader.readLine();
            if (line.startsWith("Content-Type:")) {
                int beginBoundry = line.indexOf("boundary=\"");
                String postBoundryBegin = line.substring(beginBoundry + 10);
                System.out.println(postBoundryBegin);
                int endBoundry = postBoundryBegin.indexOf("\"");
                boundryString = postBoundryBegin.substring(0, endBoundry);

                // boundary="MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20"
            }
        }
        if (!line.equals(boundryString)) {
            StringBuilder sb = new StringBuilder();
            sb.append(mtom);
            sb.append("\r\n");
            sb.append(boundryString);
        }

    }

    private static byte[] read(ByteArrayInputStream bais) throws IOException {
        byte[] array = new byte[bais.available()];
        bais.read(array);

        return array;
    }

    public static SOAPWithAttachment parseMtom(String mtom) throws MessagingException, IOException {

//        Parsing.fixMissingEndBoundry(mtom);
        MimeMultipart mp;
        // String contentType = Parsing.findContentType(mtom);

        //     mp = new MimeMultipart(new ByteArrayDataSource(mtom.getBytes(), ""), new ContentType(contentType));
        mp = new MimeMultipart(new ByteArrayDataSource(mtom.getBytes(), "multipart/related"));
        SOAPWithAttachment swa = new SOAPWithAttachment();
        int count = mp.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bp = mp.getBodyPart(i);
            String contentType = bp.getContentType();
            if (contentType.startsWith("application/xop+xml")) {
                // SOAP
                ByteArrayInputStream content = (ByteArrayInputStream) bp.getContent();
                swa.setSoap(IOUtils.toString(content));

            } else {
                Object contentRaw = bp.getContent();
                if (contentRaw instanceof String) {
                    String content = (String) bp.getContent();
                    swa.getAttachment().add(content.getBytes());
                } else if (contentRaw instanceof SharedByteArrayInputStream) {
                    SharedByteArrayInputStream content = (SharedByteArrayInputStream) bp.getContent();
                    swa.getAttachment().add(read(content));
                } else {
                    System.out.println("UNKNOWN ATTACHMENT TYPE = " + contentRaw.getClass().getName());
                    swa.getAttachment().add(new String().getBytes());
                }
            }

            // System.out.println("contentype=" + bp.getContentType());
            //ByteArrayInputStream content = (ByteArrayInputStream) bp.getContent();
            //String contentString = IOUtils.toString(content);
            //String contentString = (String) bp.getContent();
            /*
            try {
                Envelope env = (Envelope) JAXB.unmarshal(new StringReader(contentString), Envelope.class);
                if (env.getHeader() == null && env.getBody() == null) {
                    swa.getAttachment().add(Parsing.read(content));
                    //swa.getAttachment().add(contentString.getBytes());
                } else {
                    swa.setSoap(contentString);
                }
            } catch (Exception saxe) {
                // Not SOAP so must be attachment.
                 swa.getAttachment().add(Parsing.read(content));
                //swa.getAttachment().add(contentString.getBytes());
            }
             */
        }

        if (swa.getAttachment() == null || swa.getAttachment().size() == 0) {
            byte[] document = Parsing.getDocumentFromSoap(swa.getSoap()).getBytes();
            Collection<byte[]> attachments = new ArrayList<byte[]>();
            attachments.add(document);
            swa.setAttachment(attachments);
        }

        return swa;
    }

    public final static void main(String[] args) {/*
        String line = "content-type: multipart/related; boundary=\"MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20\"; type=\"application/xop+xml\"; start=\"&lt;0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org&gt;\"; start-info=\"application/soap+xml\"; action=\"urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b\"";
        int beginBoundry = line.indexOf("boundary=\"");
        String postBoundryBegin = line.substring(beginBoundry + 10);
        System.out.println(postBoundryBegin);
        int endBoundry = postBoundryBegin.indexOf("\"");
        String boundryString = postBoundryBegin.substring(0, endBoundry);

        System.out.println(boundryString);
         */
        // boundary="MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20"
        String xml;
        try {
            //  xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml", Charset.defaultCharset());

//              System.out.println(Parsing.getMetadataLevel(xml));
            //          System.out.println(Parsing.isValidDirectAddressBlock(xml));
            //   xml = MiscUtil.readFile("/home/mccaffrey/ett/parsingSamples/MTOM.txt", Charset.defaultCharset());
            xml = MiscUtil.readFile("/home/mccaffrey/git/ett/artifacts/saml.txt", Charset.defaultCharset());

            //System.out.println(Parsing.getWsseHeaderFromMTOM(xml));
            System.out.println(Parsing.getPatientIDFromWsse(xml));
            /*
            System.out.println(Parsing.isValidDirectAddressBlock(xml));
            //   System.out.println(Parsing.isRegistryResponseSuccess(xml));
            System.out.println(Parsing.getMetadataLevel(xml));
            DirectAddressing da = Parsing.getDirectAddressing(xml);
            System.out.println(da.getDirectFrom());
            System.out.println(da.getDirectTo());
            System.out.println(da.getMessageID());

            //   xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml", Charset.defaultCharset());
            //    System.out.println(Parsing.isRegistryResponseSuccess(xml));
//          xml = MiscUtil.readFile("/home/mccaffrey/ett/parsingSamples/MTOM2.txt", Charset.defaultCharset());  
            //xml = MiscUtil.readFile("/home/mccaffrey/ett/parsingSamples/fromToolkit.txt", Charset.defaultCharset());
            xml = MiscUtil.readFile("/home/mccaffrey/ett/parsingSamples/xdr.txt", Charset.defaultCharset());
            
       //    System.out.println(Parsing.isRegistryResponseSuccessFullHeaders(xml));

            SOAPWithAttachment swa = Parsing.parseMtom(xml);

         //   System.out.println(swa.getSoap());
//            System.out.println(new String(swa.getAttachment().iterator().next()));
            //     System.out.println(Parsing.isValidDirectDisposition(xml));
            /*                    
             try {
                 SOAPWithAttachment swa = Parsing.parseMtom(xml);
                 
                 System.out.println("SOAP = " + swa.getSoap());
                System.out.println("Attachment " + new String(swa.getAttachment().iterator().next()));
                 
                 
             } catch (MessagingException ex) {
                 Logger.getLogger(Parsing.class.getName()).log(Level.SEVERE, null, ex);
             }
             */
            //  xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/Xdr_minimal_metadata.xml", Charset.defaultCharset());
            //  xml = MiscUtil.readFile("/home/mccaffrey/ett/parsingSamples/request.txt", Charset.defaultCharset());  
//   xml = MiscUtil.readFile("/home/mccaffrey/ett/parsingSamples/response.txt", Charset.defaultCharset());  
            //  System.out.println(Parsing.getMetadataLevel(xml));
        } catch (Exception ex) {
            Logger.getLogger(Parsing.class.getName()).log(Level.SEVERE, null, ex);
        }

        //  System.out.println(RegistryResponse.isSuccessSoap(xml));
//       
        //  System.out.println(RegistryResponse.isSuccessSoap(xml));
//       
    }

}
