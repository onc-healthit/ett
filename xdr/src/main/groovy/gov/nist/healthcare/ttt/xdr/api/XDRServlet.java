package gov.nist.healthcare.ttt.xdr.api;


import gov.nist.toolkit.transactionNotificationService.TransactionLog;
import gov.nist.toolkit.transactionNotificationService.TransactionNotification;

public class XDRServlet implements TransactionNotification {
	
	@Override
	public void notify(TransactionLog arg0) {
		System.out.println(arg0.getResponseMessageBody().toString());
		System.out.println("NOTIFY");
		
	}

}
