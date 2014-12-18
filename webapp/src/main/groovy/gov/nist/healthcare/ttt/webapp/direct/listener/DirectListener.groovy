package gov.nist.healthcare.ttt.webapp.direct.listener;

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;


@Component
public class DirectListener implements Runnable {

    @Autowired
    DatabaseInstance db;

	@Value('${direct.listener.port}')
	int listenerPort
	
	@Value('${direct.listener.domainName}')
	String domainName = ""
	
	@Value('${server.contextPath}')
	String servletName = ""
	
	@Value('${server.port}')
	int port = 0
	
	@Value('${direct.certificates.repository.path}')
	String certificatesPath
	
	@Value('${direct.certificates.password}')
	String certPassword = ""
	
	@Value('${server.tomcat.basedir}')
	String tomcatDir = ""
	
	// Emailer settings
	@Value('${direct.listener.email.from}')
	String emailerFom
	@Value('${direct.listener.email.host}')
	String emailerHost
	@Value('${direct.listener.email.port}')
	String emailerPort
	@Value('${direct.listener.email.auth}')
	String emailerAuth
	@Value('${direct.listener.email.username}')
	String emailerUsername
	@Value('${direct.listener.email.password}')
	String emailerPassword
	@Value('${direct.listener.email.starttls}')
	String emailerStarttls
	@Value('${direct.listener.email.gmailStyle}')
	String emailerGmailStyle
	
	Emailer emailer
	
	private int maxConnections = 0;

	private ArrayList<Thread> threadsList = new ArrayList<Thread>();

	private static Logger logger = Logger.getLogger(DirectListener.class.getName());

	public DirectListener() {
		EmailerModel emailerModel = new EmailerModel();
		emailerModel.setFrom(this.emailerFom);
		emailerModel.setHost(this.emailerHost);
		emailerModel.setSmtpAuth(this.emailerAuth);
		emailerModel.setSmtpUser(this.emailerUsername);
		emailerModel.setSmtpPassword(this.emailerPassword);
		emailerModel.setSmtpPort(this.emailerPort);
		emailerModel.setStarttls(this.emailerStarttls);
		emailerModel.setGmailStyle(this.emailerGmailStyle);
		this.emailer = new Emailer(emailerModel);
	}

	// Listen for incoming connections and handle them
	public void run() {
		if(this.listenerPort == 0) {
			logger.info("Listener port is configured to 0 so the listener is not starting");
			return;
		}
		logger.info("Starting listener on port: " + this.listenerPort);
		int i = 0;

		try{
			ServerSocket listener = new ServerSocket(this.listenerPort);
			Socket server;

			while((i++ < maxConnections) || (maxConnections == 0)) {
				server = listener.accept();
				logger.debug("Running listener");
				
				String logFilePath = this.tomcatDir + File.separator + "logs" + File.separator + "listener.log"
				
				// Set the processor
				ListenerProcessor processor = new ListenerProcessor(server, db);
				processor.setEmailer(this.emailer)
				processor.setDomainName(this.domainName)
				processor.setServletName(this.servletName)
				processor.setPort(this.port)
				processor.setListenerPort(this.listenerPort)
				processor.setCertificatesPath(this.certificatesPath)
				processor.setCertPassword(this.certPassword)
				processor.setLogFilePath(logFilePath)
				
				Thread t = new Thread(processor);
				threadsList.add(t);
				t.start();
			}
			listener.close();
		} 
		catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
		catch (Exception ioe) {
			System.out.println("Exception on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}

    @PreDestroy
	public void stopThreads() {
		for(Thread t : threadsList) {
			t.interrupt();
		}
	}

}