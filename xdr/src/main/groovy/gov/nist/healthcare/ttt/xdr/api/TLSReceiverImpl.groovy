package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.net.ssl.*
import java.security.KeyStore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * TODO make it configurable
 * Little server that listens on port 8888
 * and then notify observer when a request come.
 * Different requests will be identified by their remote addresses.
 * Created by gerardin on 12/8/14.
 */

@Component
public class TLSReceiverImpl extends Thread implements TLSReceiver {

    IObserver observer
    SSLServerSocket s
    int maxConnections = 10
    ExecutorService executorService = Executors.newFixedThreadPool(maxConnections)
    Logger log = LoggerFactory.getLogger(TLSReceiverImpl.class)

    public TLSReceiverImpl(){
        InputStream is = this.class.getClassLoader().getResourceAsStream("keystore/keystore");
        char[] ksPass = "changeit".toCharArray();
        char[] ctPass = "changeit".toCharArray();

        boolean run = true;
        s = null;

        try {
            URI uri = this.class.getClassLoader().getResource("keystore/keystore").toURI();
            log.info("set up tls/ssl receiver with keystore : " + uri.toString())

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(is, ksPass);
            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            s = (SSLServerSocket) ssf.createServerSocket(8888);
            printServerSocketInfo(s);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("unable to set ssl server");

        }
    }

    @PostConstruct
     bootstrap(){
        this.start()
    }

    @Override
    def notifyObserver(Message m) {
        observer.getNotification(m)
    }

    @Override
    def registerObserver(IObserver o) {
        this.observer = o
    }

    @Override
    void run() {

        while(true) {

            SSLSocket c = null;
            BufferedWriter w = null;
            BufferedReader r = null;

            try {
                c = (SSLSocket) s.accept();
                printSocketInfo(c);
                w = new BufferedWriter(new OutputStreamWriter(
                        c.getOutputStream()));
                r = new BufferedReader(new InputStreamReader(
                        c.getInputStream()));
                String m = "Welcome to SSL Reverse Echo Server." +
                        " Please type in some words.";
                w.write(m, 0, m.length());
                w.newLine();
                w.flush();

                while ((m = r.readLine()) != null) {
                    if (m.equals(".")) break;
                    char[] a = m.toCharArray();
                    int n = a.length;
                    for (int i = 0; i < n / 2; i++) {
                        char t = a[i];
                        a[i] = a[n - 1 - i];
                        a[n - i - 1] = t;
                    }
                    w.write(a, 0, n);
                    w.newLine();
                    w.flush();
                }
            } catch (Exception e) {
                executorService.execute(new Runnable(){
                    @Override
                    void run() {
                        System.err.println(e.toString());
                        System.out.println("client has dropped the connection");
                    }
                })

            } finally {

                w.close();
                r.close();
                c.close();
                executorService.execute(new Runnable(){
                    @Override
                    void run() {
                        String address = c.getInetAddress().toString()+c.remoteSocketAddress.toString()
                        println "notification started for address $address"
                        notifyObserver(new Message(Message.Status.SUCCESS,address))
                    }
                })

            }

        }

        s.close();
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

    private static void printServerSocketInfo(SSLServerSocket s) {
        System.out.println("Server socket class: " + s.getClass());
        System.out.println("   Socker address = "
                + s.getInetAddress().toString());
        System.out.println("   Socker port = "
                + s.getLocalPort());
        System.out.println("   Need client authentication = "
                + s.getNeedClientAuth());
        System.out.println("   Want client authentication = "
                + s.getWantClientAuth());
        System.out.println("   Use client mode = "
                + s.getUseClientMode());
    }
}
