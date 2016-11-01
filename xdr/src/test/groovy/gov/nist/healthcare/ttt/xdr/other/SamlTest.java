package gov.nist.healthcare.ttt.xdr.other;

import org.w3c.dom.Document;

import gov.nist.hit.ds.wsseTool.api.config.ContextFactory;
import gov.nist.hit.ds.wsseTool.api.config.GenContext;
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess;
import gov.nist.hit.ds.wsseTool.generation.opensaml.OpenSamlWsseSecurityGenerator;
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils;


public class SamlTest {	
	
	public static void main( String[] args ) {

		String keystoreFileWithPath = SamlTest.class.getClassLoader().getResource("goodKeystore/goodKeystore").toString();
        String keyStorePass = "changeit";
        String alias = "1";
        String privateKeyPass = "changeit";

        GenContext context = ContextFactory.getInstance();
        Document doc = null;

        try {
            context.setKeystore(new KeystoreAccess(keystoreFileWithPath, keyStorePass, alias, privateKeyPass));
            context.setParam("patientId", "test");
            doc = new OpenSamlWsseSecurityGenerator().generateWsseHeader(context);
            //new WsseHeaderValidator().validate(doc.getDocumentElement(),context);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(MyXmlUtils.DomToString(doc));
    }
}
