package gov.nist.healthcare.ttt.webapp.smtp.model;

import gov.nist.healthcare.ttt.smtp.TestInput;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class SmtpTestInput {

	String testCaseNumber

	String sutSmtpAddress

	String sutSmtpPort

	String tttSmtpPort

	String sutEmailAddress

	String tttEmailAddress

	String useTLS

	String sutCommandTimeoutInSeconds

	String sutUserName

	String sutPassword

	String tttUserName

	String tttPassword

	String tttSmtpAddress

	String startTlsPort

	public SmtpTestInput() {

	}

	public SmtpTestInput(String testCaseNumber, String sutSmtpAddress,
			String sutSmtpPort, String tttSmtpPort, String sutEmailAddress,
			String tttEmailAddress, String useTLS,
			String sutCommandTimeoutInSeconds, String sutUserName,
			String sutPassword, String tttUserName, String tttPassword,
			String tttSmtpAddress, String startTlsPort) {
		super()
		this.testCaseNumber = testCaseNumber
		this.sutSmtpAddress = sutSmtpAddress
		this.sutSmtpPort = sutSmtpPort
		this.tttSmtpPort = tttSmtpPort
		this.sutEmailAddress = sutEmailAddress
		this.tttEmailAddress = tttEmailAddress
		this.useTLS = useTLS
		this.sutCommandTimeoutInSeconds = sutCommandTimeoutInSeconds
		this.sutUserName = sutUserName
		this.sutPassword = sutPassword
		this.tttUserName = tttUserName
		this.tttPassword = tttPassword
		this.tttSmtpAddress = tttSmtpAddress
		this.startTlsPort = startTlsPort
	}

	public SmtpTestInput(String testCaseNumber, String sutSmtpAddress,
			String sutSmtpPort, String sutEmailAddress, String tttEmailAddress,
			String useTLS) {
		super()
		this.testCaseNumber = testCaseNumber
		this.sutSmtpAddress = sutSmtpAddress
		this.sutSmtpPort = sutSmtpPort
		this.sutEmailAddress = sutEmailAddress
		this.tttEmailAddress = tttEmailAddress
		this.useTLS = useTLS
	}

	public TestInput convert() throws FileNotFoundException {
		Properties prop = new Properties()
		String propFileName = "application.properties"
 
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath")
		}
		
		// Default value
		if (sutSmtpAddress==null || sutSmtpAddress.equals("")) {
			this.sutSmtpAddress = "localhost"
		}
		if (sutEmailAddress==null || sutEmailAddress.equals("")) {
			this.sutEmailAddress = "blue@localhost"
		}
		if (tttEmailAddress==null || tttEmailAddress.equals("")) {
			this.tttEmailAddress = "wellformed1@" + prop.getProperty("direct.listener.domainName")
		}

		if (tttSmtpAddress==null || tttSmtpAddress.equals("")) {
			this.tttSmtpAddress = prop.getProperty("direct.listener.domainName")
		}

		if (sutCommandTimeoutInSeconds==null || sutCommandTimeoutInSeconds.equals("0")) {
			this.sutCommandTimeoutInSeconds = "600"
		}

		this.sutUserName = setDefaultAuthValue(this.sutUserName)
		this.sutPassword = setDefaultAuthValue(this.sutPassword)
		this.tttUserName = setDefaultAuthValue(this.tttUserName)
		this.tttPassword = setDefaultAuthValue(this.tttPassword)

		this.sutSmtpPort = setDefautlPort(this.sutSmtpPort)
		this.tttSmtpPort = setDefautlPort(this.tttSmtpPort)
		this.startTlsPort = setDefautlPort(this.startTlsPort)

		// Generate attachment
		LinkedHashMap<String, byte[]> attachment = new LinkedHashMap<String, byte[]>()
		InputStream ccdaAttachment = getClass().getResourceAsStream("/cda-samples/CCDA_Ambulatory.xml")
		attachment.put("CCDA_Ambulatory.xml", IOUtils.toByteArray(ccdaAttachment))

		TestInput res = new TestInput(this.sutSmtpAddress, this.tttSmtpAddress,
				Integer.parseInt(this.sutSmtpPort),
				Integer.parseInt(this.tttSmtpPort), this.sutEmailAddress,
				this.tttEmailAddress, getBool(this.useTLS), this.sutUserName,
				this.sutPassword, this.tttUserName, this.tttPassword,
				Integer.parseInt(this.startTlsPort),
				Integer.parseInt(this.sutCommandTimeoutInSeconds), attachment)

		return res
	}

	public boolean getBool(String field) {
		if(field == null) {
			return false
		}
		if (field.equals("true")) {
			return true
		}
		return false
	}

	public String setDefaultAuthValue(String param) {
		if (param==null || param.equals("")) {
			return "red"
		}
		return param
	}

	public String setDefautlPort(String param) {
		if (param==null || param.equals("0")) {
			return "25"
		} else {
			return param
		}
	}

}
