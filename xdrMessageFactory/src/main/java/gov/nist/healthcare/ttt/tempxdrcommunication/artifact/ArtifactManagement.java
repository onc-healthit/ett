package gov.nist.healthcare.ttt.tempxdrcommunication.artifact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author mccaffrey
 */
public class ArtifactManagement {

    public enum Type {

        XDR_FULL_METADATA, // DONE
        XDR_MINIMAL_METADATA, // DONE
        NEGATIVE_BAD_SOAP_HEADER,
        NEGATIVE_BAD_SOAP_BODY,
        NEGATIVE_MISSING_DIRECT_BLOCK, // DONE
        NEGATIVE_MISSING_METADATA_ELEMENTS1,
        NEGATIVE_MISSING_METADATA_ELEMENTS2,
        NEGATIVE_MISSING_METADATA_ELEMENTS3,
        NEGATIVE_MISSING_METADATA_ELEMENTS4,
        NEGATIVE_MISSING_METADATA_ELEMENTS5,
        XDR_CCR, // DONE
        XDR_C32, // DONE
        NEGATIVE_MISSING_ASSOCIATION, // DONE
        DELIVERY_STATUS_NOTIFICATION_SUCCESS, //DONE
        DELIVERY_STATUS_NOTIFICATION_FAILURE //DONE
    };

    public static final String NIST_OID_PREFIX = "2.16.840.1.113883.3.72.5";

    private static final String FILENAME_XDR_FULL_METADATA = "Xdr_full_metadata.xml";
    private static final String FILENAME_XDR_FULL_METADATA_ONLY = "Xdr_full_metadata_only.xml";
    private static final String FILENAME_XDR_FULL_METADATA_ONLY_NO_SOAP = "Xdr_full_metadata_only_no_soap.xml";
    private static final String FILENAME_BAD_SOAP = "negative_bad_soap.xml";
    private static final String FILENAME_BAD_SOAP_BODY = "negative_bad_soap_body.xml";
    private static final String FILENAME_MISSING_DIRECT_BLOCK = "negative_missing_direct_block.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS1 = "negative_missing_metadata_elements1.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS2 = "negative_missing_metadata_elements2.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS3 = "negative_missing_metadata_elements3.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS4 = "negative_missing_metadata_elements4.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS5 = "negative_missing_metadata_elements5.xml";

    private static final String FILENAME_MISSING_METADATA_ELEMENTS1_NO_SOAP = "negative_missing_metadata_elements1_no_soap.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS2_NO_SOAP = "negative_missing_metadata_elements2_no_soap.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS3_NO_SOAP = "negative_missing_metadata_elements3_no_soap.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS4_NO_SOAP = "negative_missing_metadata_elements4_no_soap.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS5_NO_SOAP = "negative_missing_metadata_elements5_no_soap.xml";

    private static final String FILENAME_XDR_CCR = "Xdr_Ccr.xml";
    private static final String FILENAME_XDR_CCR_ENCODED = "ccr64.txt";
    private static final String FILENAME_XDR_C32 = "Xdr_C32.xml";
    private static final String FILENAME_XDR_C32_ENCODED = "c3264.txt";
    private static final String FILENAME_MISSING_ASSOCIATION = "negative_missing_association.xml";
    private static final String FILENAME_MISSING_ASSOCIATION_NO_SOAP = "negative_missing_association_no_soap.xml";
    private static final String FILENAME_XDR_MINIMAL_METADATA = "Xdr_minimal_metadata.xml";
    private static final String FILENAME_XDR_MINIMAL_METADATA_ONLY = "Xdr_minimal_metadata_only.xml";
    private static final String FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP = "Xdr_minimal_metadata_only_no_soap.xml";
    private static final String FILENAME_ENCODED_CCDA = "encodedCCDA.txt";
    private static final String FILENAME_IGNORE_PAYLOAD = "ignorePayload.txt";
    private static final String FILENAME_DELIVERY_STATUS_NOTIFICATION_SUCCESS = "DeliveryStatusNotification_success.xml";
    private static final String FILENAME_DELIVERY_STATUS_NOTIFICATION_FAILURE = "DeliveryStatusNotification_failure.xml";
    private static final String FILENAME_DELIVERY_STATUS_NOTIFICATION_SUCCESS_STANDALONE = "Xdr_positive_delivery.xml";
    private static final String FILENAME_DELIVERY_STATUS_NOTIFICATION_FAILURE_STANDALONE = "Xdr_negative_delivery.xml";

    public static String getPayload(Type type, Settings settings) throws IOException {
        makeSettingsSafe(settings);
        String payload = null;
        switch (type) {
            case XDR_FULL_METADATA:
                payload = getXdrFullMetadata(settings);
                break;
            case XDR_MINIMAL_METADATA:
                payload = getXdrMinimalMetadata(settings);
                break;
            case DELIVERY_STATUS_NOTIFICATION_SUCCESS:
                payload = getDeliveryStatusNotificationSuccess(settings);
                break;
            case DELIVERY_STATUS_NOTIFICATION_FAILURE:
                break;

        }

        return payload;

    }

    public static String getMtomSoap(Type type, Settings settings) {
        makeSettingsSafe(settings);
        String payload = null;
        switch (type) {
            case XDR_FULL_METADATA:
                payload = getXdrFullMetadataOnly(settings);
                break;
            case XDR_MINIMAL_METADATA:
                payload = getXdrMinimalMetadataOnly(settings);
                break;
            case NEGATIVE_BAD_SOAP_HEADER:
                payload = getXdrBadSoap(settings);
                break;
            case NEGATIVE_MISSING_DIRECT_BLOCK:
                payload = getXdrMissingDirectBlock(settings);
                break;
            case NEGATIVE_BAD_SOAP_BODY:
                payload = getXdrBadSoapBody(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS1:
                payload = getXdrMissingMetadataElements1(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS2:
                payload = getXdrMissingMetadataElements2(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS3:
                payload = getXdrMissingMetadataElements3(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS4:
                payload = getXdrMissingMetadataElements4(settings);
                break;

            case NEGATIVE_MISSING_METADATA_ELEMENTS5:
                payload = getXdrMissingMetadataElements5(settings);
                break;
            case XDR_CCR:
                payload = getXdrCcr(settings);
                break;
            case XDR_C32:
                payload = getXdrC32(settings);
                break;
            case NEGATIVE_MISSING_ASSOCIATION:
                payload = getXdrMissingAssociation(settings);
                break;
        }

        return payload;

    }

    private static String getDeliveryStatusNotificationSuccessStandalone(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_DELIVERY_STATUS_NOTIFICATION_SUCCESS_STANDALONE);
        message = message.replaceAll("#DIRECT_RECIPIENT#", settings.getDirectFrom());
        message = setIds(message, settings.getMessageId());

        return message;
    }
    
    private static String getDeliveryStatusNotificationFailureStandalone(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_DELIVERY_STATUS_NOTIFICATION_SUCCESS_STANDALONE);
        message = message.replaceAll("#DIRECT_RECIPIENT#", settings.getDirectFrom());
        message = setIds(message, settings.getMessageId());

        return message;
    }
    
    
    
    // if messageId null or empty, creates one
    private static String getDeliveryStatusNotificationSuccess(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_DELIVERY_STATUS_NOTIFICATION_SUCCESS);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = message.replaceAll("#DIRECT_RELATESTO#", settings.getDirectRelatesTo());
        message = message.replaceAll("#DIRECT_RECIPIENT#", settings.getDirectRecipient());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    private static String getDeliveryStatusNotificationFailure(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_DELIVERY_STATUS_NOTIFICATION_FAILURE);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = message.replaceAll("#DIRECT_RELATESTO#", settings.getDirectRelatesTo());
        message = message.replaceAll("#DIRECT_RECIPIENT#", settings.getDirectRecipient());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }    
    
    public static String getXdrFullMetadata(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_FULL_METADATA);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrFullMetadataOnly(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_FULL_METADATA_ONLY);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMinimalMetadata(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_MINIMAL_METADATA);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMinimalMetadataOnly(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrBadSoap(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_BAD_SOAP);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrBadSoapBody(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_BAD_SOAP_BODY);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingDirectBlock(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_DIRECT_BLOCK);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements1(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS1);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements2(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS2);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements3(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS3);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements4(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS4);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements5(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS5);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrCcr(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_CCR);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrC32(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_C32);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingAssociation(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_ASSOCIATION);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    private static String getTemplate(String resourceName) {

        InputStream is = ArtifactManagement.class.getClassLoader().getResourceAsStream(resourceName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder out = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line + "\r\n");
            }
            reader.close();
        } catch (IOException ioe) {
            //TODO
            System.err.println("RESOURCE NOT FOUND: " + resourceName);
            ioe.printStackTrace();
        }
        return out.toString();
    }

    public static String getIgnorePayload(){
        return getTemplate(FILENAME_IGNORE_PAYLOAD);
    }
    
    public static String getBaseEncodedCCDA() {
        return getTemplate(FILENAME_ENCODED_CCDA);
    }

    public static String getBaseEncodedC32() {
        return getTemplate(FILENAME_XDR_C32_ENCODED);
    }

    public static String getBaseEncodedCCR() {
        return getTemplate(FILENAME_XDR_CCR_ENCODED);
    }

    private static String setIds(String metadata, String messageId, String documentId) {

        metadata = metadata.replaceAll("#MESSAGE_ID#", messageId);
        String entryUuid = UUID.randomUUID().toString();
        metadata = metadata.replaceAll("#ENTRY_UUID#", entryUuid);
        metadata = metadata.replaceAll("#DOCUMENT_ID#", documentId);
        long timestamp = Calendar.getInstance().getTimeInMillis();
        String uniqueId = NIST_OID_PREFIX + "." + timestamp;
        metadata = metadata.replaceAll("#UNIQUE_ID_SS#", uniqueId);

        return metadata;

    }

    private static String setIds(
            String message,
            String messageId) {

        if (messageId == null || "".equals(messageId)) {
            messageId = UUID.randomUUID().toString();
        }
        message = message.replaceAll("#MESSAGE_ID#", messageId);
        String entryUuid = UUID.randomUUID().toString();
        message = message.replaceAll("#ENTRY_UUID#", entryUuid);
        String documentUuid = UUID.randomUUID().toString();
        message = message.replaceAll("#DOCUMENT_ID#", documentUuid);
        long timestamp = Calendar.getInstance().getTimeInMillis();
        String uniqueId = NIST_OID_PREFIX + "." + timestamp;
        message = message.replaceAll("#UNIQUE_ID_SS#", uniqueId);

        return message;
    }

    private static String setDirectAddressBlock(String message, String directTo, String directFrom) {
        message = message.replaceAll("#DIRECT_TO#", directTo);
        message = message.replaceAll("#DIRECT_FROM#", directFrom);
        return message;
    }

    private static String setSOAPHeaders(String message, String wsaTo) {
        return message.replaceAll("#WSA_TO#", wsaTo);
    }

    private static void makeSettingsSafe(Settings settings) {
        if (settings == null) {
            settings = new Settings();
        }
        if (settings.getDirectFrom() == null) {
            settings.setDirectFrom("");
        }
        if (settings.getDirectRecipient() == null) {
            settings.setDirectRecipient("");
        }
        if (settings.getDirectRelatesTo() == null) {
            settings.setDirectRelatesTo("");
        }
        if (settings.getDirectTo() == null) {
            settings.setDirectTo("");
        }
        if (settings.getWsaTo() == null) {
            settings.setWsaTo("");
        }

    }

    // TODO: This is -- of course -- terrible.  Re-do this if we ever get some time.
    public static String generateDirectMessageBlock(Settings settings) {

        StringBuilder directBlock = new StringBuilder();
        directBlock.append("<direct:addressBlock xmlns:direct=\"urn:direct:addressing\" xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" ");
        directBlock.append("soapenv:role=\"urn:direct:addressing:destination\" ");
        directBlock.append("soapenv:relay=\"true\"> ");
        directBlock.append("<direct:from>" + settings.getDirectFrom() + "</direct:from> ");
        directBlock.append("<direct:to>" + settings.getDirectTo() + "</direct:to> ");
        String[] additionalDirectTo = settings.getAdditionalDirectTo();
        if (additionalDirectTo != null && additionalDirectTo.length > 0) {
            for (int i = 0; i < additionalDirectTo.length; i++) {
                directBlock.append("<direct:to>" + additionalDirectTo[i] + "</direct:to> ");
            }
        }
        String finalDestinationDelivery = settings.getFinalDestinationDelivery();
        if (finalDestinationDelivery != null && !finalDestinationDelivery.equals("")) {
            directBlock.append("<direct:X-DIRECT-FINAL-DESTINATION-DELIVERY>" + finalDestinationDelivery + "</direct:X-DIRECT-FINAL-DESTINATION-DELIVERY> ");

        }
        directBlock.append("</direct:addressBlock>");

        return directBlock.toString();

    }

    public static String generateExtraHeaders(Settings settings, boolean full) {

        StringBuilder headers = new StringBuilder();
        if (full) {
            headers.append("<direct:metadata-level xmlns:direct=\"urn:direct:addressing\">XDS</direct:metadata-level>");
        } else {
            headers.append("<direct:metadata-level xmlns:direct=\"urn:direct:addressing\">minimal</direct:metadata-level>");
        }
        headers.append(generateDirectMessageBlock(settings));
        return headers.toString();

    }

    public static String removeXmlDeclaration(String xml) {
        
        int start = xml.indexOf("<?xml ");
        int end = xml.indexOf("?>");
        if (start != -1 && end != -1 && end > start) {
            xml = xml.substring(0, start) + xml.substring(end + 2);
        }
        return xml;
    }
    public static String escapeXml(String xml) {
        xml = xml.replace("<", "&lt;");
        xml = xml.replace(">", "&gt;");
        return xml;
    
    
    }
    public static Artifacts generateArtifacts(Type type, Settings settings) {

        Artifacts artifacts = new Artifacts();
        artifacts.setDocumentId("id_extrinsicobject"); //TODO
        if (settings.getMessageId() != null && !settings.getMessageId().isEmpty()) {
            artifacts.setMessageId(settings.getMessageId());
        } else {
            String messageId = UUID.randomUUID().toString();
            settings.setMessageId(messageId);
            artifacts.setMessageId(messageId);
        }
        artifacts.setMimeType("text/xml"); // TODO: make this configurable
        String metadata = null;
        String payload = null;
        payload = settings.getPayload();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        System.out.println(formattedDate);
        if (payload != null && !payload.isEmpty()) {
            System.out.println("Payload is not empty " + formattedDate);
     //       payload = ArtifactManagement.removeXmlDeclaration(payload);
     //       payload = ArtifactManagement.escapeXml(payload);
            if (!ArtifactManagement.isBase64Encoded(payload)) {
             //   System.out.println("!!!Payload is not base64encoded " + payload);                                                             
                try {
                    payload = Base64.getEncoder().encodeToString(payload.getBytes("utf-8"));                 
                  //  System.out.println("!!!now base64encoded " + payload);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(ArtifactManagement.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
              //  System.out.println("!!! Payload IS base64 encoded " + payload);
            }

            artifacts.setExtraHeaders(generateExtraHeaders(settings, false));
            artifacts.setDocument(payload);
            System.out.println("Setting doc to payload " + formattedDate);
            metadata = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP);
        } else {
            System.out.println("PAYLOAD EMPTY " + formattedDate);
            switch (type) {
                case XDR_FULL_METADATA:
                    artifacts.setExtraHeaders(generateExtraHeaders(settings, true));
                    artifacts.setDocument(getBaseEncodedCCDA());
                    metadata = getTemplate(FILENAME_XDR_FULL_METADATA_ONLY_NO_SOAP);
                    break;
                case XDR_MINIMAL_METADATA:
                    artifacts.setExtraHeaders(generateExtraHeaders(settings, false));
                    artifacts.setDocument(getBaseEncodedCCDA());
                    metadata = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP);
                    break;
                case DELIVERY_STATUS_NOTIFICATION_SUCCESS:
                    artifacts.setExtraHeaders(generateExtraHeaders(settings, false));                                                          
                    artifacts.setDocument(getDeliveryStatusNotificationSuccessStandalone(settings));
                    metadata = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP);                                        
                    break;
                case DELIVERY_STATUS_NOTIFICATION_FAILURE:
                    artifacts.setExtraHeaders(generateExtraHeaders(settings, false));                                                          
                    artifacts.setDocument(getDeliveryStatusNotificationFailureStandalone(settings));
                    metadata = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP);                                        
                    break;
                case XDR_C32:
                    artifacts.setExtraHeaders(generateExtraHeaders(settings, false));
                    artifacts.setDocument(getBaseEncodedC32());
                    metadata = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP);
                    break;
                case XDR_CCR:
                    artifacts.setExtraHeaders(generateExtraHeaders(settings, false));
                    artifacts.setDocument(getBaseEncodedCCR());
                    metadata = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP);
                    break;
                case NEGATIVE_MISSING_DIRECT_BLOCK:
                    artifacts.setExtraHeaders(new String());
                    // artifacts.setDocument(getBaseEncodedCCDA());
                    artifacts.setDocument(getIgnorePayload());
                    artifacts.setMimeType("text/plain");
                    metadata = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY_NO_SOAP);
                    break;
                case NEGATIVE_MISSING_ASSOCIATION:
                    artifacts.setExtraHeaders(generateExtraHeaders(settings, false));
                    // artifacts.setDocument(getBaseEncodedCCDA());
                    artifacts.setDocument(getIgnorePayload());
                    artifacts.setMimeType("text/plain");
                    metadata = getTemplate(FILENAME_MISSING_ASSOCIATION_NO_SOAP);
                    break;
                default:
                    throw new UnsupportedOperationException("not yet, guys");
            }
        }
        metadata = setIds(metadata, artifacts.getMessageId(), artifacts.getDocumentId());
        artifacts.setMetadata(metadata);

        return artifacts;
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

    public final static void main(String args[]) {

        try {

            Settings settings = new Settings();
            settings.setDirectFrom("directFrom");
            settings.setDirectTo("directTo");
            settings.setWsaTo("wsaTo");
            //    settings.setPayload("THIS IS MY PAYLOAD IN BASE64!!!");
          //  settings.setPayload("VEhJUyBJUyBNWSBQQVlMT0FEIElOIEJBU0U2NCEhIQ==");
            String[] directTos = {};
            settings.setAdditionalDirectTo(directTos);
            //   String payload = getPayload(Type.XDR_MINIMAL_METADATA, settings);
            // System.out.println("here!\n" + payload);
            //    URL url = ClassLoader.getSystemResource("DeliveryStatusNotification_success.xml");
            //  System.out.println(url.getPath());
            /*    
             System.out.println(getDeliveryStatusNotificationSuccess("/home/mccaffrey/xdr/DeliveryStatusNotification_success.xml",
             "directTo",
             "directFrom",
             "relatesTo",
             "recipient",
             "wsaTo",
             null));
             */
     //       Artifacts art = ArtifactManagement.generateArtifacts(Type.NEGATIVE_MISSING_ASSOCIATION, settings);
//            Artifacts art = ArtifactManagement.generateArtifacts(Type.NEGATIVE_BAD_SOAP_HEADER, settings);

Artifacts art = ArtifactManagement.generateArtifacts(Type.DELIVERY_STATUS_NOTIFICATION_FAILURE, settings);

            System.out.println("docId = " + art.getDocumentId());
            System.out.println("headers = " + art.getExtraHeaders());
            System.out.println("messageId = " + art.getMessageId());
            System.out.println("metadata = " + art.getMetadata());
            System.out.println("mimetype = " + art.getMimeType());
            System.out.println("document = " + art.getDocument());

            
            String testXMl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<?xml-stylesheet type=\"text/xsl\" href=\"CDA.xsl\"?>\n" +
                            "<ClinicalDocument>\n" +
                            "hello world\n" +
                            "</ClinicalDocument>";
            
            
            
       //     System.out.println(testXMl + "\n\n");
//            System.out.println(ArtifactManagement.escapeXml(testXMl));
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
