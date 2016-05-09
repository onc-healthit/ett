/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

import direct.addressing.MessageDisposition;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 *
 * @author mccaffrey
 */
public class DirectDisposition {

    //TODO: return report
    
    public static boolean isValidDirectDisposition(String xml) {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //    DirectDisposition.class.getClassLoader().getResourceAsStream("directDisposition.xsd");
            //      DirectDisposition.class.getClassLoader().getResource(xml);
//        sf.newSchema(DirectDisposition.class.getClassLoader().getResource("directDisposition.xsd"));
            Schema schema = sf.newSchema(DirectDisposition.class.getClassLoader().getResource("directDisposition.xsd"));
            MessageDisposition disposition = null;
            JAXBContext jc = JAXBContext.newInstance(MessageDisposition.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setSchema(schema);
            XDRValidationEventHandler event = new XDRValidationEventHandler();
            unmarshaller.setEventHandler(event);                       
            disposition = (MessageDisposition) unmarshaller.unmarshal(new StringReader(xml));
            //disposoition = (MessageDisposition) JAXB.unmarshal(new StringReader(xml), MessageDisposition.class);
            //ValidationEvent vEvent = event.getValidationEvent();
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public static final void main(String[] args) {

        try {
            //  String xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/samples/reg_response_failure.xml", Charset.defaultCharset());

            String xml = MiscUtil.readFile("/home/mccaffrey/ett/schema_to_java/sampleDirectDisposition.xml", Charset.defaultCharset());
            System.out.println(DirectDisposition.isValidDirectDisposition(xml));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
