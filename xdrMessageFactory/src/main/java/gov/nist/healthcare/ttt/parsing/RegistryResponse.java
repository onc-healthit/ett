/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXB;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.w3._2003._05.soap_envelope.Body;
import org.w3._2003._05.soap_envelope.Envelope;
import org.w3c.dom.Element;

/**
 *
 * @author mccaffrey
 */
public class RegistryResponse {

    public static final String SUCCESS = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
    public static final String FAILURE = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure";

    public static boolean isSuccessSoap(String soap) {
        
        Element regresp = RegistryResponse.pullRegistryResponseFromSoap(soap);
        return RegistryResponse.isSuccess(MiscUtil.xmlToString(regresp));
        
    }
    
    private static Element pullRegistryResponseFromSoap(String soap) {
        Envelope env = (Envelope) JAXB.unmarshal(new StringReader(soap), Envelope.class);
        Body body = env.getBody();
        List<Object> any = body.getAny();
        Element regresp = (Element) any.get(0);    
        return regresp;
    }
    
    public static boolean isSuccess(String registryResponse) {
        RegistryResponseType registryResponseElement = (RegistryResponseType) JAXB.unmarshal(new StringReader(registryResponse), RegistryResponseType.class);
        if (registryResponseElement.getStatus() == null) {
            return false;
       }
        if (registryResponseElement.getStatus().equals(RegistryResponse.SUCCESS)) {
            return true;
        }
        return false;
    }           

    public static Collection<ErrorItem> getErrorReportSoap(String soap) {
        Element regresp = RegistryResponse.pullRegistryResponseFromSoap(soap);
        return RegistryResponse.getErrorReport(MiscUtil.xmlToString(regresp));
        
    }
    
    public static Collection<ErrorItem> getErrorReport(String registryResponse) {
        Collection<ErrorItem> report = new ArrayList<ErrorItem>();
        //   if(RegistryResponse.isSuccess(registryResponse))
        //       return report;

        RegistryResponseType registryResponseElement = (RegistryResponseType) JAXB.unmarshal(new StringReader(registryResponse), RegistryResponseType.class);
        RegistryErrorList errorListElement = registryResponseElement.getRegistryErrorList();
        if (errorListElement == null) {
            return report;
        }
        List<RegistryError> errorList = errorListElement.getRegistryError();
        Iterator<RegistryError> it = errorList.iterator();
        while (it.hasNext()) {
            report.add(RegistryResponse.convertSchemaErrorToErrorItem(it.next()));
        }
        return report;

    }

    private static ErrorItem convertSchemaErrorToErrorItem(RegistryError error) {

        ErrorItem errorItem = new ErrorItem();
        errorItem.setCodeContext(error.getCodeContext());
        errorItem.setErrorCode(error.getErrorCode());
        errorItem.setLocation(error.getLocation());
        errorItem.setSeverity(error.getSeverity());
        return errorItem;
    }


    public static void main(String[] args) {

        try {
            //  String xml = readFile("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml", Charset.defaultCharset());

            // org.xmlsoap.schemas.soap.envelope.ObjectFactory OFSoap = new org.xmlsoap.schemas.soap.envelope.ObjectFactory();
            /*
           JAXBContext jc = JAXBContext.newInstance("org.xmlsoap.schemas.soap.envelope");
           Unmarshaller u = jc.createUnmarshaller();
            Envelope envelope = (Envelope) u.unmarshal(new FileInputStream("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml"));
           
           
            
            Body body = envelope.getBody();
            //Envelope envelope = JAXB.unmarshal(new StringReader(xml), Envelope.class);
      
            Iterator<Object> it  = body.getAny().iterator();
            
            
          
            while(it.hasNext()) {
                ElementNSImpl element = (ElementNSImpl) it.next();
             //   System.out.println(element.)
            }
            
             */
            String xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml", Charset.defaultCharset());

            
            System.out.println(RegistryResponse.isSuccessSoap(xml));
            
//             RegistryResponseType registryResponse = (RegistryResponseType) u.unmarshal(new FileInputStream("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure_no_soap.xml"));
            //      RegistryResponseType registryResponse = (RegistryResponseType) JAXB.unmarshal(new StringReader(xml), RegistryResponseType.class);
            //    System.out.println(registryResponse.getStatus());
            Collection<ErrorItem> items = RegistryResponse.getErrorReport(xml);
            Iterator<ErrorItem> it = items.iterator();
            while (it.hasNext()) {
                ErrorItem item = it.next();
                System.out.println(item.getCodeContext());
                System.out.println(item.getErrorCode());
                System.out.println(item.getLocation());
                System.out.println(item.getSeverity());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
