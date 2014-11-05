package gov.nist.healthcare.ttt.webapp.direct.direcForXdr;

public class DirectMessageSenderForXdrNoLookUp extends DirectMessageSenderForXdr {

	public DirectMessageInfoForXdr sendDirectWithCCDAForXdrNoDNSLookUp(String sutSmtpAddress, int port, String encryptionCertPath) throws Exception {
		setEncryptionCert(encryptionCertPath);
		return sendDirectWithCCDAForXdr(sutSmtpAddress, port);
	}
	
	public DirectMessageInfoForXdr sendDirectWithXDMForXdrNoDNSLookUp(String sutSmtpAddress, int port, String encryptionCertPath) throws Exception {
		setEncryptionCert(encryptionCertPath);
		return sendDirectWithXDMForXdr(sutSmtpAddress, port);
	}
	
	public void setEncryptionCert(String encryptionCertPath) {
		this.setDnsLookup(false);
		this.setEncryptionCert(encryptionCertPath);
	}
}
