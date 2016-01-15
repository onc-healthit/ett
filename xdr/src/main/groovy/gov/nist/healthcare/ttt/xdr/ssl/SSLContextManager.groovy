package gov.nist.healthcare.ttt.xdr.ssl
import org.springframework.stereotype.Component

import javax.net.ssl.*
import java.security.KeyStore
/**
 * Created by gerardin on 12/12/14.
 */

@Component
public class SSLContextManager {

    public def badSSLContext
    public def goodSSLContext


    SSLContextManager(){
        setupContext()
    }

    def setupContext(){
        String relativePath = "badKeystore/badKeystore";
        InputStream keystoreInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
        InputStream truststoreInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
        badSSLContext = setSSLFactories(keystoreInput, "changeit", truststoreInput, "changeit");
        keystoreInput.close();
        truststoreInput.close();

        String relativePath2 = "goodKeystore/goodKeystore";
        String relativePath3 = "goodKeystore/goodTruststore";
        InputStream keystoreInput2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath2);
        InputStream truststoreInput2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath3);
        goodSSLContext = setSSLFactories(keystoreInput2, "changeit", truststoreInput2, "changeit");
        keystoreInput2.close();
        truststoreInput2.close();
    }



    private SSLContext setSSLFactories(InputStream keyStream, String keyStorePassword, InputStream trustStream, String trustStorePassword) throws Exception {
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
