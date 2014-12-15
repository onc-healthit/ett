package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.xdr.domain.TLSValidationReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
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
    SSLServerSocket server
    int maxConnections = 10
    ExecutorService executorService = Executors.newFixedThreadPool(maxConnections)
    Logger log = LoggerFactory.getLogger(TLSReceiverImpl.class)

    @Value('${xdr.tls.test.port}')
    private String port

    //TODO change that : either find a better way or rename property
    @Value('${direct.listener.domainName}')
    private String hostname

    @PostConstruct
    def bootstrap() {
        setupServerSocketKeystore()
        this.start()
    }

    @PreDestroy
    def cleanup() {
        executorService.shutdownNow()
    }

    @Override
    public String getEndpoint(){
        "$hostname:$port"
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

        while (true) {
            SSLSocket connection = (SSLSocket) server.accept();

            Runnable task = new Runnable() {
                @Override
                void run() {
                    handleRequest(connection)
                }
            }

            executorService.execute(task)
        }
    }

    void handleRequest(SSLSocket connection) {

        BufferedWriter w = null;
        BufferedReader r = null;
        XDRRecordInterface.CriteriaMet status = XDRRecordInterface.CriteriaMet.FAILED

        printSocketInfo(connection);

        try {
            log.info("tls receiver has accepted the connection.");
            w = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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
            System.err.println(e.toString());
            System.out.println("client has dropped the connection.");
            status = XDRRecordInterface.CriteriaMet.PASSED
        } finally {
//            w.close();
//            r.close();
            connection.close();
            String address = connection.getInetAddress().canonicalHostName
            println "tls receiver notification for address $address"
            notifyObserver(new Message(Message.Status.SUCCESS, "tls receiver notification for address $address" ,new TLSValidationReport(status,address)))
        }
    }

    def setupServerSocketKeystore() {

        def socketPort = Integer.parseInt(port);

        InputStream is = this.class.getClassLoader().getResourceAsStream("goodKeystore"+File.separator+"goodKeystore");
        char[] ksPass = "changeit".toCharArray();
        char[] ctPass = "changeit".toCharArray();

        server = null;

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(is, ksPass);
            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            server = (SSLServerSocket) ssf.createServerSocket(socketPort);
            printServerSocketInfo(server);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("unable to set ssl server");

        }
    }


    private static void printSocketInfo(SSLSocket socket) {
        System.out.println("Socket class: " + socket.getClass());
        System.out.println("   Remote address = "
                + socket.getInetAddress().toString());
        System.out.println("   Remote port = " + socket.getPort());
        System.out.println("   Local socket address = "
                + socket.getLocalSocketAddress().toString());
        System.out.println("   Local address = "
                + socket.getLocalAddress().toString());
        System.out.println("   Local port = " + socket.getLocalPort());
        System.out.println("   Need client authentication = "
                + socket.getNeedClientAuth());
        SSLSession ss = socket.getSession();
        System.out.println("   Cipher suite = " + ss.getCipherSuite());
        System.out.println("   Protocol = " + ss.getProtocol());
    }

    private static void printServerSocketInfo(SSLServerSocket server) {
        System.out.println("Server socket class: " + server.getClass());
        System.out.println("   Socker address = "
                + server.getInetAddress().toString());
        System.out.println("   Socker port = "
                + server.getLocalPort());
        System.out.println("   Need client authentication = "
                + server.getNeedClientAuth());
        System.out.println("   Want client authentication = "
                + server.getWantClientAuth());
        System.out.println("   Use client mode = "
                + server.getUseClientMode());
    }
}
