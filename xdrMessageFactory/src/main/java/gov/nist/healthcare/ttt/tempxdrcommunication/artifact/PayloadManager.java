
package gov.nist.healthcare.ttt.tempxdrcommunication.artifact;

import gov.nist.healthcare.ttt.tempxdrcommunication.SimpleSOAPSender;
import static gov.nist.healthcare.ttt.tempxdrcommunication.SimpleSOAPSender.buildMTOMPackage;
import java.net.MalformedURLException;

/**
 *
 * @author mccaffrey
 */
public class PayloadManager {

    public static String getPayload(String endpoint, ArtifactManagement.Type type, Settings settings) throws MalformedURLException {

        String metadata = null;

        // TODO: can get rid of switch when everything is implemented
        switch (type) {
            case XDR_FULL_METADATA:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case XDR_MINIMAL_METADATA:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_BAD_SOAP_HEADER:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_DIRECT_BLOCK:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_BAD_SOAP_BODY:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS1:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS2:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS3:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS4:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS5:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_ASSOCIATION:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            default:
                throw new UnsupportedOperationException();  // TODO

        }

        String attachment = ArtifactManagement.getBaseEncodedCCDA();
        String mtom = buildMTOMPackage(metadata, attachment);
        String fullWithHttpHeaders = SimpleSOAPSender.addHttpHeadersMtom(endpoint, mtom);
        return fullWithHttpHeaders;
    }

    public static void main(String[] args) {
        try {
            Settings settings = new Settings();
            settings.setDirectFrom("directFrom");
            settings.setDirectTo("directTo");
            settings.setWsaTo("wsaTo");
            String payload = PayloadManager.getPayload("http://fake.com:8080/abc", ArtifactManagement.Type.XDR_FULL_METADATA, settings);

            System.out.println(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
