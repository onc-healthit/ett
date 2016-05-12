/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
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
     * Enum type depicting the two allowed metadata level values: XDS (full) and minimal.
     */
    public enum MetadataLevel {
        MINIMAL,
        XDS
    }

    /**
     * Checks if the SOAP contains a syntactically correct Direct Address Block.
     * @param soap A string representing the entire SOAP wrapper.
     * @return
     */
    public static boolean isValidDirectAddressBlock(String soap) {
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(soap), Envelope.class);
        List<Object> headers = env.getHeader().getAny();
        if(headers == null)
            return false;
        Iterator it = headers.iterator();
        boolean foundDirectAddressBlock = false;
        while(it.hasNext()) {
            Element header = (Element) it.next();
            if (header.getLocalName().equals(ELEMENT_NAME_DIRECT_ADDRESS_BLOCK) && header.getNamespaceURI().equals(NAMESPACE_DIRECT)) {
                foundDirectAddressBlock = true;
                NodeList directFrom = header.getElementsByTagNameNS(NAMESPACE_DIRECT, ELEMENT_NAME_DIRECT_FROM);
                NodeList directTo = header.getElementsByTagNameNS(NAMESPACE_DIRECT, ELEMENT_NAME_DIRECT_TO);
                if(directFrom.getLength() > 0 && directTo.getLength() > 0 )
                    return true;
                else
                    return false;
            }            
        }
        return false;
        
    }
    
    /**
     * Given an XML string representing a SOAP wrapper, what is the Direct Edge
     * XDR metadata level: minimal or full (XDS).
     * @param soap A string representing the entire SOAP wrapper.
     * @return An enum representing one of two allowed metadata levels.
     */
    public static MetadataLevel getMetadataLevel(String soap) {
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(soap), Envelope.class);
        List<Object> headers = env.getHeader().getAny();
        if(headers == null)
            return MetadataLevel.XDS;
        Iterator it = headers.iterator();
        while(it.hasNext()) {
            Element header = (Element) it.next();
            if (header.getLocalName().equals(ELEMENT_NAME_METADATA_LEVEL) && header.getNamespaceURI().equals(NAMESPACE_DIRECT)) {
                String metadataLevel = header.getFirstChild().getTextContent();
                if(metadataLevel.equals(METADATA_LEVEL_MINIMAL)) 
                    return MetadataLevel.MINIMAL;
                else if(metadataLevel.equals(METADATA_LEVEL_XDS))
                    return MetadataLevel.XDS;
                
            }            
        }
        
        return MetadataLevel.XDS;
    }

    public final static void main(String[] args) {
        
         String xml;
        try {
            xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml", Charset.defaultCharset());
            
              System.out.println(Soap.getMetadataLevel(xml));
            System.out.println(Soap.isValidDirectAddressBlock(xml));
            
            
        } catch (IOException ex) {
            Logger.getLogger(Soap.class.getName()).log(Level.SEVERE, null, ex);
        }

            
          //  System.out.println(RegistryResponse.isSuccessSoap(xml));
            
//       
        
    }
    
    
}
