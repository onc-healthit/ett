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

        def w

        printServerSocketInfo(server)

        try {
            log.info("SUT is receiver. Responding to incoming request...")
            w = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            String m = "TLS test : try to send stuff across"
            w.write(m, 0, m.length());
            w.flush();
        } catch (Exception e) {
            log.error(e.toString());
            log.error("client has dropped the connection.");
        } finally {
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

        //That is forcing clients authenticate
        server.setNeedClientAuth(true)
    }


    private static def socketInfo(SSLSocket socket) {
        StringBuffer info = new StringBuffer("\n")
        info << "   Socket class: " + socket.getClass() + "\n"
        info << "   Remote address = " + socket.getInetAddress().toString() + "\n"
        info << "   Remote port = " + socket.getPort() + "\n"
        info << "   Local socket address = " + socket.getLocalSocketAddress().toString() + "\n"
        info << "   Local address = " + socket.getLocalAddress().toString() + "\n"
        info << "   Local port = " + socket.getLocalPort() + "\n"
        info << "   Need client authentication = " + socket.getNeedClientAuth() + "\n"

        SSLSession ss = socket.getSession()
        info << "   Cipher suite = " + ss.getCipherSuite() + "\n"
        info << "   Protocol = " + ss.getProtocol()

        return info.toString()
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
