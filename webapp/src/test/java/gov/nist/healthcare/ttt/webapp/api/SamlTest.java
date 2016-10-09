package gov.nist.healthcare.ttt.webapp.api;

import gov.nist.hit.ds.wsseTool.api.config.ContextFactory;
import gov.nist.hit.ds.wsseTool.api.config.GenContext;
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess;
import gov.nist.hit.ds.wsseTool.api.exceptions.GenerationException;
import gov.nist.hit.ds.wsseTool.generation.opensaml.OpenSamlWsseSecurityGenerator;
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils;
import gov.nist.hit.xdrsamlhelper.SamlHeaderApiImpl;
import org.w3c.dom.Document;

/**
 * Created by jnp3 on 9/12/16.
 */
public class SamlTest {

    public static void main(String[] args) {
        System.out.println("Test SAML");

        String keystoreFileWithPath = "/Users/jnp3/ett/ett/xdr/src/main/resources/goodKeystore/goodKeystore";
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
            System.out.println(e);
        }
        System.out.printf(MyXmlUtils.DomToString(doc));
    }
}
