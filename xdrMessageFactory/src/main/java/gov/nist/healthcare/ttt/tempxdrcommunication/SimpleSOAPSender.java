package gov.nist.healthcare.ttt.tempxdrcommunication;

import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement;
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement.Type;
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.PayloadManager;
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author mccaffrey
 */
public class SimpleSOAPSender {

    private static String sendSecureMessage(String endpoint, String payload) throws MalformedURLException, UnknownHostException, IOException {

        URL url = new URL(endpoint);

        String hostname = url.getHost();
        int port = url.getPort();
        if (port == -1) {
            port = 80;
        }
        String path = url.getPath();

        System.out.println("truststore = " + System.getProperty("javax.net.ssl.trustStore"));

        System.setProperty("javax.net.ssl.trustStore", "/home/mccaffrey/xdr/keystore");

        InetAddress addr = InetAddress.getByName(hostname);
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSock = (SSLSocket) factory.createSocket(addr, port);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(sslSock.getOutputStream(), "UTF-8"));
        bufferedWriter.write(payload);
        bufferedWriter.flush();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sslSock.getInputStream()));

        return SimpleSOAPSender.getResponse(bufferedReader);
    }

    private static String sendMessage(String endpoint, String payload) throws MalformedURLException, UnknownHostException, IOException {
        return sendMessage(endpoint, payload, null);
    }

    private static String sendMessage(String endpoint, String payload, SSLContext sc) throws MalformedURLException, UnknownHostException, IOException {

        URL url = new URL(endpoint);

        String hostname = url.getHost();
        int port = url.getPort();
        if (port == -1) {
            port = 80;
        }
        String path = url.getPath();

        InetAddress addr = InetAddress.getByName(hostname);

       // SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        // SSLSocket socket = (SSLSocket) factory.createSocket(addr,port);
        Socket socket = null;
        if (sc != null) {
            SSLSocketFactory f = (SSLSocketFactory) sc.getSocketFactory();
            socket = (SSLSocket) f.createSocket(addr, port);
        } else {
            socket = new Socket(addr, port);
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        //   String fullPayload = SimpleSOAPSender.addHttpHeaders(path, payload);
        bufferedWriter.write(payload);
        bufferedWriter.flush();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        return SimpleSOAPSender.getResponse(bufferedReader);

    }

    private static String getResponse(BufferedReader bufferedReader) throws IOException {

        StringBuilder response = new StringBuilder();
        String line = null;
        System.out.println("SOAPSender.getResponse");
        while ((line = bufferedReader.readLine()) != null) {
            response.append(line + "\n");
            System.out.println(line);
        }
        return response.toString();

    }

    private static String addHttpHeaders(String endpoint, String payload) {

        StringBuilder httpHeaders = new StringBuilder();
        httpHeaders.append("POST " + endpoint + " HTTP/1.1\r\n");
        httpHeaders.append("content-type: application/xml\r\n");
        httpHeaders.append("user-agent: TempXDRSender\r\n");
        httpHeaders.append("host: edge.nist.gov:8080\r\n");
        // httpHeaders.append("transfer-encoding: chunked\r\n\r\n");
        httpHeaders.append("Content-Length: " + payload.length() + "\r\n");
        httpHeaders.append("\r\n");
        httpHeaders.append(payload);

        return httpHeaders.toString();
    }

    public static String addHttpHeadersMtom(String endpoint, String payload) throws MalformedURLException {

        StringBuilder httpHeaders = new StringBuilder();

        URL url = new URL(endpoint);
        String path = url.getPath();
        // httpHeaders.append("POST " + endpoint + " HTTP/1.1\r\n");
        httpHeaders.append("POST " + path + " HTTP/1.1\r\n");

        httpHeaders.append("Content-Type: multipart/related; boundary=\"MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20\"; type=\"application/xop+xml\"; start=\"<0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org>\"; start-info=\"application/soap+xml\"; action=\"urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b\"\r\n");

        httpHeaders.append("User-Agent: TempXDRSender\r\n");
        httpHeaders.append("Host: edge.nist.gov:8080\r\n");
        httpHeaders.append("Content-Length: " + (payload.length()) + "\r\n");

        httpHeaders.append("\r\n");
        httpHeaders.append(payload);

        return httpHeaders.toString();

    }

    public static RequestResponse sendMTOMPackage(String endpoint, Type type, Settings settings) throws IOException {
        return sendMTOMPackage(endpoint, type, settings, null);
    }

    public static RequestResponse sendMTOMPackage(String endpoint, Type type, Settings settings, SSLContext sc) throws IOException {

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
            case XDR_CCR:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case XDR_C32:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case NEGATIVE_MISSING_ASSOCIATION:
                metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            case DELIVERY_STATUS_NOTIFICATION_SUCCESS:
            	metadata = ArtifactManagement.getMtomSoap(type, settings);
                break;
            default:
            // throw new UnsupportedOperationException();  // TODO
            	break;

        }

        String attachment = ArtifactManagement.getBaseEncodedCCDA();
        String mtom = buildMTOMPackage(metadata, attachment);
        String fullWithHttpHeaders = addHttpHeadersMtom(endpoint, mtom);

//        String toSend = readFile("/home/mccaffrey/xdr/captured/received_mod.txt");
        String response = sendMessage(endpoint, fullWithHttpHeaders, sc);
        //  String response = sendMessage(endpoint, toSend);

        RequestResponse rr = new RequestResponse(fullWithHttpHeaders, response);

        //      RequestResponse rr = new RequestResponse(toSend,response);
        return rr;

    }

    public static String buildMTOMPackage(String metadata, String attachment) {

        StringBuilder mtom = new StringBuilder();
        mtom.append("--MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20\r\n");
        mtom.append("Content-Type: application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"\r\n");
        mtom.append("Content-Transfer-Encoding: binary\r\n");
        mtom.append("Content-ID: <0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org>\r\n");
        mtom.append("\r\n");
        mtom.append(metadata);
        mtom.append("\r\n");
        mtom.append("--MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20\r\n");
        mtom.append("Content-Type: application/octet-stream\r\n");
        mtom.append("Content-Transfer-Encoding: binary\r\n");
        mtom.append("Content-ID: <1.3293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org>\r\n");
        mtom.append("\r\n");
        mtom.append(attachment);
        mtom.append("\r\n");
        mtom.append("--MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20--\r\n");

        return mtom.toString();

    }

    static String readFile(String path)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "UTF-8");
    }

    public static void main(String args[]) {

        try {

            //  String endpoint = "http://hit-dev.nist.gov:8080/hello";
            //String endpoint = "http://transport-testing.nist.gov:12080/ttt/sim/f8488a75-fc7d-4d70-992b-e5b2c852b412/rep/prb";
           //  String endpoint = "http://transport-testing.nist.gov:12080/ttt/sim/1b578eb5-d2a5-46c1-87ab-d1efdbfdbf72/rep/prb";
            //  String endpoint = "https://transport-testing.nist.gov:12080/ttt/sim/1b578eb5-d2a5-46c1-87ab-d1efdbfdbf72/rep/prb";
            String endpoint = "http://transport-testing.nist.gov:12080/ttt/sim/9fdc17ba-0191-4d0c-be2a-c4ea5294b861/rec/xdrpr";

            // String endpoint = "http://transport-testing.nist.gov:12080/ttt/sim/ecb4e054-9581-439f-9f12-de2d052a3132/rep/prb";
            // String endpoint = "http://ihexds.nist.gov:12090/tf6/services/xdsregistryb";
          //  String endpoint = "http://hit-dev.nist.gov:12090/xdstools2/sim/811fd97a-6ea3-437e-bf42-e0a8a505ba98/rec/xdrpr";
            String directTo = "directTo";
            String directFrom = "directFrom";
            String relatesTo = "relatesTo";
            String recipient = "recipient";
            String wsaTo = endpoint;

            Settings settings = new Settings();
            settings.setDirectFrom(directFrom);
            settings.setDirectTo(directTo);
            settings.setWsaTo(wsaTo);

            System.out.println("sending to... " + endpoint);

           // StringBuilder payload = new StringBuilder();
            // payload.append("POST /random HTTP/1.1\r\n");
            // payload.append("\r\n");
        //    String payload = readFile("/home/mccaffrey/src/TempXDRCommunication/src/main/resources/sample_request.txt");
            //String response = sendSecureMessage(endpoint,payload);
            // String payload = PayloadManager.getPayload(endpoint,Type.XDR_FULL_METADATA, settings);
/*
            String payload = PayloadManager.getPayload(endpoint, Type.XDR_C32, settings);
            String response = sendMessage(endpoint, payload);
            //   String response = sendMTOMPackage(endpoint,Type.XDR_FULL_METADATA, settings);
            System.out.println(payload);
            System.out.println("------ ABOVE WAS THE REQUEST ----  BELOW IS THE RESPONSE ----");
            System.out.println(response);
*/
            
             // RequestResponse rr = sendMTOMPackage(endpoint, Type.NEGATIVE_BAD_SOAP_HEADER, settings);
             //   RequestResponse rr = sendMTOMPackage(endpoint, Type.XDR_FULL_METADATA, settings);
              RequestResponse rr = sendMTOMPackage(endpoint, Type.XDR_FULL_METADATA, settings);
           //  RequestResponse rr = sendMTOMPackage(endpoint, Type.NEGATIVE_MISSING_METADATA_ELEMENTS5, settings);

             System.out.println(rr.getRequest());
             System.out.println("--------------\n");
             System.out.println(rr.getResponse());
             
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     String request = ArtifactManagement.getPayload(Type.XDR_MINIMAL_METADATA, settings);

     String response = SimpleSOAPSender.sendMessage(endpoint, request);
     */
}
