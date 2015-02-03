package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.xdr.ssl.SSLContextManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

@Component
public class TLSClientImpl implements TLSClient {

    SSLContextManager sslContextManager

    Logger log = LoggerFactory.getLogger(TLSClientImpl.class)


    @Autowired
    public TLSClientImpl(SSLContextManager manager) {
        sslContextManager = manager
    }

    @Override
    public void connectOverBadTLS(Map config) {
        SSLContext sc = sslContextManager.badSSLContext
        SSLSocketFactory f = (SSLSocketFactory) sc.getSocketFactory();
        startConnection(f,config)
    }

    @Override
    public void connectOverGoodTLS(Map config) {
        SSLContext sc = sslContextManager.goodSSLContext
        SSLSocketFactory f = (SSLSocketFactory) sc.getSocketFactory();
        startConnection(f,config)
    }

    public void startConnection(SSLSocketFactory f,Map config) throws IOException{
            String ipAddress = config.ip_address
            def port = config.port

            SSLSocket c = (SSLSocket) f.createSocket(ipAddress, port);
            log.info("Sending a request to the server using socket: \n" + socketInfo(c))
            c.startHandshake();
            c.close();
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

    private void infoExchange(SSLSocket c) {

        BufferedReader inb = new BufferedReader(
                new InputStreamReader(System.in));
        PrintStream outb = System.out;

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
    }
}
