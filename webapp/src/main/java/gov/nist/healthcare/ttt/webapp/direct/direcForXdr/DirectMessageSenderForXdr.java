package gov.nist.healthcare.ttt.webapp.direct.direcForXdr;

import gov.nist.healthcare.ttt.direct.messageGenerator.DirectMessageGenerator;
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender;
import gov.nist.healthcare.ttt.webapp.common.config.ApplicationPropertiesConfig;
import gov.nist.healthcare.ttt.webapp.direct.listener.ListenerProcessor;
import org.apache.log4j.Logger;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;

public class DirectMessageSenderForXdr {
	
	private static Logger logger = Logger.getLogger(DirectMessageSenderForXdr.class.getName());

	// Used to get the ressources
	private ListenerProcessor listener = new ListenerProcessor();
	private DirectMessageSender sender = new DirectMessageSender();

	public DirectMessageInfoForXdr sendDirectWithCCDAForXdr(String sutSmtpAddress, int port) throws Exception {
		InputStream attachmentFile = DirectMessageSenderForXdr.class.getResourceAsStream("/cda-samples/CCDA_Ambulatory.xml");
		return sendDirect(attachmentFile, sutSmtpAddress, port);

	}
	
	public DirectMessageInfoForXdr sendDirectWithXDMForXdr(String sutSmtpAddress, int port) throws Exception {
		InputStream attachmentFile = DirectMessageSenderForXdr.class.getResourceAsStream("/cda-samples/CCDA_Ambulatory_in_XDM.zip");
		return sendDirect(attachmentFile, sutSmtpAddress, port);
	}
	
	public DirectMessageInfoForXdr sendDirect(InputStream attachmentFile, String sutSmtpAddress, int port) throws Exception {
		InputStream signingCert = listener.getPrivateCert("/signing-certificates/good/", ".p12");

		String tttDomain = ApplicationPropertiesConfig.getConfig().getProperty("direct.listener.domainName");
		
		DirectMessageGenerator messageGenerator = new DirectMessageGenerator(
				"This is a Direct Message for XDR testing", "Direct For XDR",
				"directFrom4Xdr@" + tttDomain, "directTo4Xdr@" + tttDomain,
				attachmentFile, "CCDA_Ambulatory.xml", signingCert, "", null,
				true);

		// Get encryption cert
		logger.debug("Trying to fetch encryption cert by DNS Lookup");
		InputStream encryptionCert = messageGenerator.getEncryptionCertByDnsLookup(sutSmtpAddress);

		messageGenerator.setEncryptionCert(encryptionCert);

		MimeMessage msg = messageGenerator.generateMessage();

		sender.send(port, messageGenerator.getTargetDomain(sutSmtpAddress),
				msg, "directFrom4Xdr@" + tttDomain, "directTo4Xdr@" + tttDomain);
		
		return new DirectMessageInfoForXdr(msg.getMessageID(), "directFrom4Xdr@" + tttDomain, "directTo4Xdr@" + tttDomain, msg.getReceivedDate(), "CCDA_Ambulatory.xml");
	}

}
