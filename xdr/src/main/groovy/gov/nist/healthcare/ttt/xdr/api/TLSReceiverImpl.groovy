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

        log.info("Request coming from a socket: \n" + socketInfo(connection))

        try {
            log.info("trying to send response to client...")
            w = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            String m = "TLS test : try to send stuff across"
            w.write(m, 0, m.length());
            w.flush();

        } catch (Exception e) {
            //e.printStackTrace()
            System.err.println(e.toString());
            System.out.println("client has dropped the connection.");
            status = XDRRecordInterface.CriteriaMet.PASSED
        } finally {
            w.close();
//            r.close();
            connection.close();
            String address = connection.inetAddress.hostAddress
            println "tls receiver notification for IP address $address"
            notifyObserver(new Message(Message.Status.SUCCESS, "tls receiver notification for address $address" ,new TLSValidationReport(status,address)))
        }
    }

    def setupServerSocketKeystore() {

        def socketPort = Integer.parseInt(port);

        InputStream is = this.class.getClassLoader().getResourceAsStream("badKeystore"+File.separator+"badKeystore");
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

    private static def socketInfo(SSLSocket socket) {
        StringBuffer info = new StringBuffer()
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
