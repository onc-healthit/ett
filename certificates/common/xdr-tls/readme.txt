1) Use the keystore file as is: (we use Tomcat)

  <Connector port="8443"
               SSLEnabled="true"
               maxThreads="150"
               secure="true"
               protocol="HTTP/1.1"
               scheme="https"
               clientAuth="false"
               SSLProtocol="TLSv1"
               keystoreFile="/<your path to file>/keystore"
               keystorePass="changeit"/>

2)      Or you can import our Public cert (cert.pem) into your store.
