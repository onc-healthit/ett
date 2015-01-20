package gov.nist.healthcare.ttt.webapp.misc;

/* SslSocketClient.java
 - Copyright (c) 2014, HerongYang.com, All Rights Reserved.
 */

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URI;

/**
 * This Socket is not working for now.
 */

public class SslSocketClientWithTruststore {
    public static void main(String[] args) {

        String relativePath = "badKeystore" +File.separator+"keystore.jks";

        InputStream is = SslSocketClientWithTruststore.class.getClassLoader().getResourceAsStream(relativePath);
        char ksPass[] = "changeit".toCharArray();
        char ctPass[] = "changeit".toCharArray();

        SSLSocketFactory f = null;

        try {

            URI uri = SslSocketClientWithTruststore.class.getClassLoader().getResource(relativePath).toURI();
            File file = new File(uri);
            System.out.println("use keystore at : " + file.getAbsolutePath());

            System.setProperty("javax.net.ssl.trustStore", file.getAbsolutePath());
            f = (SSLSocketFactory) SSLSocketFactory.getDefault();



        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("unable to set s " +
                    "" +
                    " " +
                    "N BV sl factory");

        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        PrintStream out = System.out;

        try {
            SSLSocket c =
                    (SSLSocket) f.createSocket("hit-dev.nist.gov", 12084);
            printSocketInfo(c);
            c.startHandshake();
            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(c.getOutputStream()));
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(c.getInputStream()));
            String m = null;
            while ((m=r.readLine())!= null) {
                out.println(m);
                m = in.readLine();
                w.write(m,0,m.length());
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
        System.out.println("Socket class: "+s.getClass());
        System.out.println("   Remote address = "
                +s.getInetAddress().toString());
        System.out.println("   Remote port = "+s.getPort());
        System.out.println("   Local socket address = "
                +s.getLocalSocketAddress().toString());
        System.out.println("   Local address = "
                +s.getLocalAddress().toString());
        System.out.println("   Local port = "+s.getLocalPort());
        System.out.println("   Need client authentication = "
                +s.getNeedClientAuth());
        SSLSession ss = s.getSession();
        System.out.println("   Cipher suite = "+ss.getCipherSuite());
        System.out.println("   Protocol = "+ss.getProtocol());
    }
}
