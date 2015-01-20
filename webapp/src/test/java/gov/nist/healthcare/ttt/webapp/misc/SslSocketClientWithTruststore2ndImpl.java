package gov.nist.healthcare.ttt.webapp.misc;

/* SslSocketClient.java
 - Copyright (c) 2014, HerongYang.com, All Rights Reserved.
 */

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

/**
 * This Socket is not working for now.
 */

public class SslSocketClientWithTruststore2ndImpl {
    public static void main(String[] args) {

        String relativePath = "badKeystore" + File.separator + "keystore.jks";
        InputStream keystoreInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
        InputStream truststoreInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);

        SSLContext sc = null;
        try {
            sc = setSSLFactories(keystoreInput, "changeit", "changeit", truststoreInput);
            keystoreInput.close();
            truststoreInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SSLSocketFactory f = null;

        try {
            f = (SSLSocketFactory) sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("unable to set ssl factory");

        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        PrintStream out = System.out;

        try {
            SSLSocket c =
                    (SSLSocket) f.createSocket("localhost", 8888);
            printSocketInfo(c);
            c.startHandshake();
            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(c.getOutputStream()));
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(c.getInputStream()));
            String m = null;
            while ((m = r.readLine()) != null) {
                out.println(m);
                m = in.readLine();
                w.write(m, 0, m.length());
                w.newLine();
                w.flush();
            }
            w.close();
            r.close();
            c.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private static void printSocketInfo(SSLSocket s) {
        System.out.println("Socket class: " + s.getClass());
        System.out.println("   Remote address = "
                + s.getInetAddress().toString());
        System.out.println("   Remote port = " + s.getPort());
        System.out.println("   Local socket address = "
                + s.getLocalSocketAddress().toString());
        System.out.println("   Local address = "
                + s.getLocalAddress().toString());
        System.out.println("   Local port = " + s.getLocalPort());
        System.out.println("   Need client authentication = "
                + s.getNeedClientAuth());
        SSLSession ss = s.getSession();
        System.out.println("   Cipher suite = " + ss.getCipherSuite());
        System.out.println("   Protocol = " + ss.getProtocol());
    }


    private static SSLContext setSSLFactories(InputStream keyStream, String keyStorePassword, String trustStorePassword, InputStream trustStream) throws Exception {
        // Get keyStore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        // if your store is password protected then declare it (it can be null however)
        char[] keyPassword = keyStorePassword.toCharArray();

        // load the stream to your store
        keyStore.load(keyStream, keyPassword);

        // initialize a trust manager factory with the trusted store
        KeyManagerFactory keyFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, keyPassword);

        // get the trust managers from the factory
        KeyManager[] keyManagers = keyFactory.getKeyManagers();

        // Now get trustStore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        // if your store is password protected then declare it (it can be null however)
        char[] trustPassword = trustStorePassword.toCharArray();

        // load the stream to your store
        trustStore.load(trustStream, trustPassword);

        // initialize a trust manager factory with the trusted store
        TrustManagerFactory trustFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);

        // get the trust managers from the factory
        TrustManager[] trustManagers = trustFactory.getTrustManagers();

        // initialize an ssl context to use these managers and set as default
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagers, trustManagers, null);

        return sslContext;
    }
}
