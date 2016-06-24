package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.Status
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
        Status status = Status.PENDING

        log.info("Request coming from a socket: \n" + socketInfo(connection))

        try {                                                           
            if(connection.isClosed()) {
                status = Status.PASSED
            }
            if(connection.isInputShutdown() && connection.isOutputShutdown()) {
                status = Status.PASSED
            }    
                        
            log.info("trying to send response to client...")
            w = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));            
            String m = "TLS test 1: try to send stuff across."
            w.write(m, 0, m.length());
            log.info("sent " + m.length() +  " bytes to client...")
            w.flush();
            log.info("socket flushed")
            String n = "TLS test 2: try to send stuff across.."
            w.write(n, 0, n.length());
            log.info("sent " + n.length() +  " bytes to client...")
            w.flush();
            log.info("socket flushed")
            String p = "TLS test 3: try to send stuff across..."
            w.write(p, 0, p.length());
            log.info("sent " + p.length() +  " bytes to client...")
            w.flush();
            log.info("socket flushed")
            BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
            int incoming = is.read();                        
            log.info("success opening inputstream from socket (potential issue?)")
            Thread.sleep(10000)
            if(incoming == -1) {                
                log.info("nothing is being sent but socket is available for read -- considered pass")
                status = Status.PASSED
            } else {                
                log.info("there is data available to read")
                int i = is.read();
                StringBuilder sb = new StringBuilder();
                sb.append(i + " ");
                while(i != -1) {
                    i = is.read();
                    sb.append(i + " ");
                }
                log.info("socket should have been closed but client has sent int/bytes: " + sb.toString())
                status = Status.FAILED
            }                      
        } catch (Exception e) {
            //e.printStackTrace()
            log.info(e.toString())
            log.info("client has dropped the connection.")
            status = Status.PASSED
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
