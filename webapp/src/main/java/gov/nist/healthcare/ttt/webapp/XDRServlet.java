package gov.nist.healthcare.ttt.webapp;

import gov.nist.toolkit.transactionNotificationService.TransactionLog;
import gov.nist.toolkit.transactionNotificationService.TransactionNotification;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.servlet.ServletContainer;

public class XDRServlet extends ServletContainer implements TransactionNotification {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		System.out.println("IT WORKS!!!");
	}
	
	@Override
	public void notify(TransactionLog arg0) {
		System.out.println(arg0.getResponseMessageBody().toString());
		System.out.println("NOTIFY");
		
	}

}
