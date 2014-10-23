package gov.nist.healthcare.ttt.webapp.direct.listener;

import gov.nist.healthcare.ttt.database.jdbc.DatabaseException;
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import gov.nist.healthcare.ttt.direct.messageGenerator.MDNGenerator;
import gov.nist.healthcare.ttt.direct.messageProcessor.DirectMessageProcessor;
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender;
import gov.nist.healthcare.ttt.direct.sender.DnsLookup;
import org.apache.log4j.Logger;
import org.xbill.DNS.TextParseException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListenerProcessor implements Runnable {
	Socket server;
	String directFrom = null; // Direct from address reported in SMTP protocol
	List<String> directTo = new ArrayList<String>();
	boolean logInputs = true;

	// Message and cert streams
	StringBuffer message = new StringBuffer();
	InputStream messageStream = null;
	InputStream certStream = null;
	String certPassword = "";

	Collection<String> contactAddr = null;
	String logHostname = "";
	BufferedReader in = null;
	BufferedOutputStream out = null;
	static final String CRLF = "\r\n";
	
	// Settings
	Properties settings;

	String domainName = "";
	String servletName = "";
	int port = 0;
	int listenerPort = 0;

	DirectMessageProcessor processor = new DirectMessageProcessor();

	private DatabaseInstance db;

	private static Logger logger = Logger.getLogger(ListenerProcessor.class
			.getName());

	ListenerProcessor(Socket server, Properties settings, DatabaseInstance db, String contextPath, int listenerPort)
			throws DatabaseException, SQLException {
		this.server = server;

		this.db = db;

		this.settings = settings;
		this.domainName = settings.getProperty("direct.listener.domainName");
		this.servletName = contextPath;
		this.port = Integer.parseInt(settings.getProperty("server.port"));
		this.listenerPort = listenerPort;
	}

	/**
	 * For unit testing only
	 */
	public ListenerProcessor() {

	}

	public void run() {

		if (readSMTPMessage() == false)
			return;
		logger.info("Processing message from " + directFrom);

		// Need to know if it is a MDN or not so we know if we have to send MDN
		// back
		// boolean isMDN = false;
		// boolean isDirect = false;

		logger.info("Mime Message parsing successful");

		// Valid Direct (From) addr?
		try {
			if (!db.getDf().isDirectMappedToAUsername(directFrom)) {
				// looks like spam - unregistered direct (from) Addr
				logger.error("Throw away message from " + directFrom
						+ " not a registered Direct (From) email account name");
				return;
			}
		} catch (DatabaseException e1) {
			logger.error("Cannot connect to database: " + e1.getMessage());
		}

		try {
			contactAddr = getContactAddr(directFrom);
		} catch (DatabaseException e1) {
			logger.error("Cannot connect to database: " + e1.getMessage());
		}

		if (contactAddr == null || contactAddr.isEmpty()) {
			logger.error("No contact address listed for Direct (From) address "
					+ directFrom);
		} else {
			logger.info("Direct addr (From) " + directFrom
					+ " is registered and has contact email of " + contactAddr);
		}

		/****************************************************************************************
		 * 
		 * All errors detected after this can be reported back to the user via
		 * their Contact Addr.
		 * 
		 ****************************************************************************************/

		// Reporting enclosure - throw ReportException to flush messages and
		// continue
		try {	
			
			this.certStream = getPrivateCert("/signing-certificates/good/", ".p12");

			if (this.certStream == null) {
				logger.error("Cannot load private decryption key");
			}

			// TkPropsServer ccdaProps =
			// reportingProps.withPrefixRemoved("ccdatype");
			if (directTo.size() > 1) {
				String msg = "Multiple TO addresses pulled from SMTP protocol headers - cannot determine which CCDA validator to run - CCDA validation will be skipped";
				logger.warn(msg);
			} else if (directTo.size() == 0) {
				String msg = "No TO addresses pulled from SMTP protocol headers - cannot determine which CCDA validator to run - CCDA validation will be skipped";
				logger.warn(msg);
				// else {
				// String to = directTo.get(0);
				// for (int i=1; i<500; i++) {
				// String en = Integer.toString(i);
				// String type = ccdaProps.get("type" + en, null);
				// String ccdaTo = ccdaProps.get("directTo" + en, null);
				// if (type == null || ccdaTo == null)
				// break;
				// if (ccdaTo.equals(to)) {
				// ccdaType = type;
				// break;
				// }
				// }
				// }
			}

			// ccdaType tells us the document type to validate against

			// Validate
			// yadda, yadda, yadda
			byte[] messageBytes = message.toString().getBytes();
			this.messageStream = new ByteArrayInputStream(messageBytes);

			logger.info("Message Validation Begins");

			processor.setDirectMessage(messageStream);
			processor.setCertificate(certStream);
			processor.setCertificatePassword(certPassword);
			processor.processDirectMessage();

			// Log in the database
			try {
				logger.info("Logging message validation in database: "
						+ processor.getLogModel().getMessageId());
				db.getLogFacade().addNewLog(processor.getLogModel());
				db.getLogFacade().addNewPart(
						processor.getLogModel().getMessageId(),
						processor.getMainPart());
			} catch (DatabaseException e) {
				logger.error("Error trying to log in the database "
						+ e.getMessage());
				e.printStackTrace();
			}

			logger.info("Message Validation Complete");

			// Log line for the stats
			if (directTo != null) {
				String docType;
				// if(isMDN) {
				// docType = "mdn";
				// } else {
				docType = getCcdaType(directTo.get(0));
				// }
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"dd/MMM/yyyy:hh:mm:ss Z"); // Date format
				String statLog = "|" + logHostname + " - - ["
						+ dateFormat.format(new Date()) + "] "
						+ "\"POST /ttt/xdstools2/toolkit/" + docType
						+ " HTTP/1.1\" 200 75";
				logger.info(statLog);
			}

		} catch (Exception e) {
			logger.error("Message Validation Error: " + e.getMessage());
		}

		logger.info("Starting report generation");

		// Generate validation report URL
		// String reportId = new DirectActorFactory().getNewId();
		String url = "http://" + domainName + ":" + port + servletName
				+ "/direct/#/validationReport/"
				+ this.processor.getLogModel().getMessageId();

		// Generate report template
		String announcement = "<h2>Direct Validation Report"
//				+ processor.getLogModel().getMessageId()
				+ "</h2>Validation from " + new Date()
				+ "<p>Report link: <a href=\"" + url + "\">" + url + "</p>";

		logger.debug("Announcement is:\n" + announcement);

		logger.info("Send report");
		
		try {

			logger.info("Sending report to " + contactAddr);
			if (contactAddr == null) {
				throw new Exception(
						"Internal Error: no Contact email Address found");
			}

			// Send report
			EmailerModel emailerModel = new EmailerModel();
			emailerModel.setFrom(settings.getProperty("direct.listener.email.from"));
			emailerModel.setHost(settings.getProperty("direct.listener.email.host"));
			emailerModel.setSmtpAuth(settings.getProperty("direct.listener.email.auth"));
			emailerModel.setSmtpUser(settings.getProperty("direct.listener.email.username"));
			emailerModel.setSmtpPassword(settings.getProperty("direct.listener.email.password"));
			emailerModel.setSmtpPort(settings.getProperty("direct.listener.email.port"));
			emailerModel.setStarttls(settings.getProperty("direct.listener.email.starttls"));
			emailerModel.setGmailStyle(settings.getProperty("direct.listener.email.gmailStyle"));
			Emailer emailer = new Emailer(emailerModel);

			logger.debug("Sending report from " + emailerModel.getFrom()
					+ "   to " + contactAddr);
			Iterator<String> it = contactAddr.iterator();
			while (it.hasNext()) {
				emailer.sendEmail2(it.next(),
						"Direct Message Validation Report", announcement);
			}

		} catch (Exception e) {
			logger.error("Cannot send email (" + e.getClass().getName() + ". "
					+ e.getMessage());
		} catch (Throwable e) {
			logger.error("Cannot send email (" + e.getClass().getName() + ". "
					+ e.getMessage());
		}

		logger.info("Done");

		// Send MDN if it is Direct Message
		if (!processor.isMdn()) {
			String toMDN = "";
			if(processor.getLogModel().getReplyTo() != null) {
				toMDN = processor.getLogModel().getReplyTo().iterator().next();
			} else {
				toMDN = this.processor.getLogModel().getFromLine().iterator().next();
			}
			String fromMDN = this.processor.getLogModel().getToLine().iterator().next();
			
			try {
				logger.info("Generating MDN for message " + processor.getLogModel().getMessageId());
				MimeMessage mdn = generateMDN(fromMDN, toMDN, this.processor.getLogModel().getMessageId(), getPrivateCert("/signing-certificates/good/", ".p12"),
						this.certPassword);
				DirectMessageSender sender = new DirectMessageSender();
				logger.info("Sending MDN to " + toMDN);
				sender.send(listenerPort, sender.getTargetDomain(toMDN), mdn, fromMDN, toMDN);
			} catch (Exception e) {
				logger.info("Cannot send MDN to " + toMDN + ": " + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	public String getDirectTo() {
		if (directTo.size() == 0 || directTo.size() > 1)
			return null;
		return directTo.get(0);
	}

	public Collection<String> getContactAddr(String directFrom)
			throws DatabaseException {
		directFrom = stripBrackets(directFrom);

		return db.getDf().getContactAddresses(directFrom);
	}

	public boolean readSMTPMessage() {
		InetAddress ia = server.getInetAddress();
		String stars = "**********************************************************";
		logger.info(stars + "\n" + "Connection from " + ia.getHostName() + " ("
				+ ia.getHostAddress() + ")");

		// Get the hostname for the stats log
		logHostname = ia.getHostName();

		try {
			// smtpFrom set as a side-effect. This is the from address from the
			// SMTP protocol elements
			// replies should go here as well as this addr should be used to
			// lookup contact addr.
			String m = readIncomingSMTPMessage(server, this.domainName);
			message.append(m);
		} catch (EOFException e) {
			logger.warn("IOException on socket listen: " + e);
			return true;
		} catch (IOException ioe) {
			logger.error("IOException on socket listen: " + ioe);
			return false;
		} catch (Exception e) {
			logger.error("Protocol error on socket listen: " + e);
			return false;
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
				return true;
			}
		}
		return true;
	}

	class RSETException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RSETException() {

		}

	}

	public String readIncomingSMTPMessage(Socket socket, String domainname)
			throws IOException, Exception {
		this.domainName = domainname;
		String buf = null;

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new BufferedOutputStream(socket.getOutputStream());

		try {
			send("220 " + domainname + " SMTP Exim");

			buf = rcvStateMachine();

			logger.trace("MESSAGE: \n" + buf);

		} catch (RSETException e) {
			send("250 OK");
			return "";
		} catch (IOException e) {
			return "";
		} finally {
			in.close();
			out.close();
			in = null;
			out = null;
		}

		return buf;
	}

	void send(String cmd) throws IOException {
		logger.debug("SMTP SEND: " + cmd);
		cmd = cmd + CRLF;
		out.write(cmd.getBytes());
		out.flush();
	}

	String rcvStateMachine() throws IOException, RSETException, Exception {
		return rcvStateMachine(false);
	}

	String rcvStateMachine(boolean reportError) throws IOException,
			RSETException, Exception {
		StringBuffer buf = new StringBuffer();
		String msg;
		while (true) {
			msg = rcv().trim();
			msg = msg.toLowerCase();
			if (msg.startsWith("rcpt to:")) {
				String to = msg.substring(msg.indexOf(':') + 1);
				if (to != null && !to.equals(""))
					directTo.add(stripBrackets(to));
				send("250 OK");
				continue;
			}
			if (msg.startsWith("data")) {
				send("354 Enter message, ending with '.' on a line by itself");
				msg = "";
				logInputs = false;
				while (true) {
					msg = rcv();
					if (".".equals(msg.trim()))
						break;
					buf.append(msg).append(CRLF);
				}
				logInputs = true;

				send("250 OK");
				continue;
			}
			if (msg.startsWith("helo")) {
				send("250 OK");
				continue;
			}
			if (msg.startsWith("ehlo")) {
				send("502 ehlo not supported - use helo");
				continue;
			}
			if (msg.startsWith("mail from:")) {
				directFrom = stripBrackets(msg.substring(msg.indexOf(':') + 1));
				send("250 OK");
				continue;
			}
			if (msg.startsWith("rset")) {
				send("250 OK");
				throw new RSETException();
			}
			if (msg.startsWith("vrfy")) {
				send("250 OK");
				continue;
			}
			if (msg.startsWith("noop")) {
				send("250 OK");
				continue;
			}
			if (msg.startsWith("quit")) {
				send("221 " + domainName + " closing connection");
				// if (error)
				// return "";
				return buf.toString();
			}
			send("503 bad sequence of commands - received " + msg);
			throw new Exception("503 bad sequence of commands - received "
					+ msg);
		}
	}

	String rcv(String expect) throws IOException, RSETException, Exception {
		String msg = rcv().trim().toLowerCase();
		expect = expect.toLowerCase();
		if (expect != null && !msg.startsWith(expect)) {
			send("503 bad sequence of commands");
			return rcvStateMachine(true);
		}
		return msg;
	}

	String rcv() throws IOException, RSETException {
		String msg = in.readLine();
		if (logInputs)
			logger.debug("SMTP RCV: " + msg);
		return (msg == null) ? "" : msg;
	}

	String ccdaValidationType(String toAddr) {
		if (toAddr == null)
			return null;
		// TkPropsServer props = tps.withPrefixRemoved("ccdatype");
		// for (int i=1; i<50; i++) {
		// String key = "directTo" + String.valueOf(i);
		// String to = props.get(key, null);
		// if (to == null)
		// return null;
		// if (toAddr.equals(to)) {
		// key = "type" + String.valueOf(i);
		// return props.get(key, null);
		// }
		// }
		return null;
	}

	String simpleEmailAddr(String addr) throws Exception {
		addr = addr.trim();
		int fromi = addr.indexOf('@');
		if (fromi == -1)
			throw new Exception("Cannot parse Direct Address " + addr);
		int toi = fromi;
		for (int i = fromi; i >= 0; i--) {
			if (i == 0) {
				fromi = 0;
				break;
			}
			char c = addr.charAt(i);
			if (c == '"' || c == '<' || c == ' ' || c == '\t' || c == ':') {
				fromi = i + 1;
				break;
			}
		}
		for (int i = toi; i < addr.length(); i++) {
			char c = addr.charAt(i);
			if (c == '"' || c == '>') {
				String sim = addr.substring(fromi, i);
				return sim.trim();
			}
			if (i == addr.length() - 1) {
				return addr.substring(fromi).trim();
			}
		}
		throw new Exception("Cannot parse Direct Address " + addr);
	}

	String header(List<String> headers, String name) {
		String prefix = (name + ":").toLowerCase();
		for (String h : headers) {
			String hl = h.toLowerCase();
			if (hl.startsWith(prefix))
				return h;
		}
		return "";
	}

	List<String> headers(StringBuffer b) {
		List<String> headers = new ArrayList<String>();
		String CR = "\n";

		int start = 0;
		int end = b.indexOf(CR);
		int length = end - start;

		while (end != -1) {
			if (length > 0) {
				String header = b.substring(start, end).trim();
				if (header.length() == 0)
					break;
				logger.info("Header (" + header.length() + ") : " + header);
				headers.add(header);
				start = end;
				end = b.indexOf(CR, start + 1);
			}
		}

		logger.info("Found " + headers.size() + " headers");

		return headers;
	}

	/**
	 * Strip surrounding < > brackets if present
	 * 
	 * @param in
	 * @return
	 */
	public String stripBrackets(String in) {
		if (in == null || in.length() == 0)
			return in;
		in = in.trim();
		int openI = in.indexOf('<');
		while (openI > -1) {
			in = in.substring(1);
			openI = in.indexOf('<');
		}

		if (in.length() == 0)
			return in;

		int closeI = in.indexOf('>');
		if (closeI > 0)
			in = in.substring(0, closeI);

		return in;
	}

	public MimeMessage generateMDN(String from, String to, String messageId, InputStream signingCert, String signingCertPassword) throws MessagingException, Exception {
		
		// Generate the MDN
		MDNGenerator generator = new MDNGenerator();
		generator.setDisposition("automatic-action/MDN-sent-automatically;processed");
		generator.setFinal_recipient(to);
		generator.setFromAddress(from);
		generator.setOriginal_message_id(messageId);
		generator.setOriginal_recipient(from);
		generator.setReporting_UA_name("direct.nist.gov");
		generator.setReporting_UA_product("Security Agent");
		generator.setSigningCert(signingCert);
		generator.setSigningCertPassword(signingCertPassword);
		generator.setSubject("Automatic MDN");
		generator.setText("Your message was successfully processed.");;
		generator.setToAddress(to);
		generator.setEncryptionCert(generator.getEncryptionCertByDnsLookup(to));
		
		return generator.generateMDN();
		
	}

	// this only applies to certificates in byte[] format
	boolean isEmpty(byte[] b) {
		if (b == null)
			return true;
		if (b.length < 10)
			return true;
		return false;
	}

	String getDirectServerName(String domainName) {
		String directServerName = null;
		try {
			directServerName = new DnsLookup().getMxRecord(domainName);
		} catch (TextParseException e) {
			logger.error("    Error parsing MX record from DNS - for domain "
					+ domainName);
		}

		if (directServerName != null && !directServerName.equals(""))
			return directServerName;

		logger.error("    MX record lookup in DNS did not provide a mail handler hostname for domain "
				+ domainName);
		directServerName = "smtp." + domainName;
		logger.error("    Guessing at mail server name - " + directServerName);
		return directServerName;
	}

	public static String getCcdaType(String directAddress) {
		String res = null;
		if (directAddress != null) {
			if (directAddress.contains("@")) {
				res = directAddress.split("@")[0];
				res = res.toLowerCase();
				if (!res.equals("direct-clinical-summary")
						&& !res.equals("direct-ambulatory")
						&& !res.equals("direct-inpatient")
						&& !res.equals("direct-vdt-ambulatory")) {
					res = "non-specific";
				}
			}
		}
		return res;
	}
	
	public InputStream getPrivateCert(String path, String extension) throws IOException {
		InputStream in = getClass().getResourceAsStream(path);
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = rdr.readLine()) != null) {
            if(line.endsWith(extension)) {
            	logger.info("Loading private key (decryption) from " + path + line);
            	return getClass().getResourceAsStream(path + line);
            }
        }
        rdr.close();
        return null;
	}

}
