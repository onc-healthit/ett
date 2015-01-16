package gov.nist.healthcare.ttt.webapp.testFramework

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.net.ssl.*
import java.security.KeyStore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
/**
 * Used as a fixture for test 8 and 9.
 * We need to mock the SUT that accept the connection from TTT in test case 8 and rejects it in test case 9.
 *
 * Created by gerardin on 12/8/14.
 */

@Component
public class MockSUTThatAcceptsTLSWithGoodCert extends Thread {

    SSLServerSocket server
    int maxConnections = 10
    ExecutorService executorService = Executors.newFixedThreadPool(maxConnections)
    Logger log = LoggerFactory.getLogger(MockSUTThatAcceptsTLSWithGoodCert.class)

    private String port = '12085'

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

        printSocketInfo(connection);

        def w,r

        try {
            log.info("tls receiver has accepted the connection.");
            w = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
//            r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String m = "Welcome to SSL Reverse Echo Server." +
                    " Please type in some words.";
            w.write(m, 0, m.length());
            w.newLine();
            w.flush();

//            while ((m = r.readLine()) != null) {
//                if (m.equals(".")) break;
//                char[] a = m.toCharArray();
//                int n = a.length;
//                for (int i = 0; i < n / 2; i++) {
//                    char t = a[i];
//                    a[i] = a[n - 1 - i];
//                    a[n - i - 1] = t;
//                }
//                w.write(a, 0, n);
//                w.newLine();
//                w.flush();
//            }
        } catch (Exception e) {
            //e.printStackTrace()
            System.err.println(e.toString());
            System.out.println("client has dropped the connection.");
        } finally {
            w.close();
            r.close();
            connection.close();

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
