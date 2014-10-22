package gov.nist.healthcare.ttt.direct.sender;

import gov.nist.healthcare.ttt.direct.messageGenerator.SMTPAddress;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import javax.mail.internet.MimeMessage;

public class DirectMessageSender {
	
	private static Logger logger = Logger.getLogger(DirectMessageSender.class.getName());

	public static final String CRLF = "\r\n";
	public BufferedReader in = null;
	public BufferedOutputStream out = null;
	
	public boolean send(int port, String mailerHostname, MimeMessage msg, String fromAddress, String toAddress) throws Exception {
		DnsLookup lookup = new DnsLookup();
		mailerHostname = lookup.getMxRecord(mailerHostname);
		return sendMessage(port, mailerHostname, msg, fromAddress, toAddress);
	}
	
	public boolean sendMessage(int mailerPort, String mailerHostname, MimeMessage msg, String fromAddress, String toAddress) throws Exception {
		logger.info("Opening socket to Direct system on " + mailerHostname + ":" + mailerPort + "...");
		Socket socket;
		try {
			socket = new Socket(mailerHostname, mailerPort);
		} catch (UnknownHostException e) {
			logger.info(e.getMessage());
			throw new Exception(e.getMessage());
		} catch (IOException e) {
			logger.info(e.getMessage());
			throw new Exception(e.getMessage());
		}
		logger.info("\t...Success");
		
		try {
			smtpProtocol(socket, msg, mailerHostname, fromAddress, toAddress);
		} catch (Exception ex) {
			logger.info("Exception: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		} finally {
			socket.close();
		}
		
		return true;
	}
	

	public void smtpProtocol(Socket socket, MimeMessage mmsg, String domainname, String from, String to) throws Exception {
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new BufferedOutputStream(socket.getOutputStream());

		try {
			from = new SMTPAddress().properEmailAddr(from);
			to = new SMTPAddress().properEmailAddr(to);

			rcv("220");

			send("HELO " + domainname);

			rcv("250"); 

			send("MAIL FROM:" + from);

			rcv("250"); 

			send("RCPT TO:" + to);

			rcv("250");

			send("DATA");

			rcv("354"); 

			send("Subject: " + mmsg.getSubject());

			mmsg.writeTo(out);

			send(CRLF + ".");

			rcv("250");

			send("QUIT");

			rcv("221"); 
		} catch (Exception e) {
			logger.info("Protocol error: " + e.getMessage());
			throw new Exception("Protocol error: " + e.getMessage());
		} finally {
			in.close();
			out.close();
			in = null;
			out = null;
		}
	}

	public void send(String cmd) throws IOException {
		logger.info("SMTP SEND: " + cmd);
		cmd = cmd + CRLF;
		out.write(cmd.getBytes());
		out.flush();
	}

	public String rcv(String expect) throws Exception {
		String msg;
		msg = in.readLine();
		logger.info("SMTP RCV: " + msg);
		if (expect != null && !msg.startsWith(expect))
			throw new Exception("Error: expecting " + expect + ", got <" + msg + "> instead");
		return msg;
	}

	public String getTargetDomain(String targetedFrom) {
		// Get the targeted domain
		String targetDomain = "";
		if(targetedFrom.contains("@")) {
			targetDomain = targetedFrom.split("@", 2)[1];
		}
		return targetDomain;
	}
}
