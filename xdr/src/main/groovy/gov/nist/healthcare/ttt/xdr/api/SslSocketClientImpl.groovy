package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.xdr.ssl.SSLContextManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component;

/* SslSocketClient.java
 - Copyright (c) 2014, HerongYang.com, All Rights Reserved.
 */
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
/**
 * This Socket is not working for now.
 */


@Component
public class SslSocketClientImpl implements TLSClient {

    SSLContextManager sslContextManager

    @Autowired
    public SslSocketClientImpl(SSLContextManager manager){
        sslContextManager = manager
    }

    public void connectOverBadTLS(){

        SSLContext sc = sslContextManager.badSSLContext
        SSLSocketFactory f = (SSLSocketFactory) sc.getSocketFactory();

        BufferedReader inb = new BufferedReader(
                new InputStreamReader(System.in));
        PrintStream outb = System.out;

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
                outb.println(m);
                m = inb.readLine();
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
}
