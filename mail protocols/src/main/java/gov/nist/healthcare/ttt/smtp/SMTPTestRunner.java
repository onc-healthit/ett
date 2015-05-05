package gov.nist.healthcare.ttt.smtp;

import gov.nist.healthcare.ttt.smtp.server.SocketSMTPSender;
import gov.nist.healthcare.ttt.smtp.testcases.MU2ReceiverTests;
import gov.nist.healthcare.ttt.smtp.testcases.MU2SenderTests;
import gov.nist.healthcare.ttt.smtp.testcases.TTTReceiverTests;
import gov.nist.healthcare.ttt.smtp.testcases.TTTSenderNegativeTests;
import gov.nist.healthcare.ttt.smtp.testcases.TTTSenderTests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author sriniadhi
 *
 *         Runs all the test cases
 * 
 *         Key functions: runTestCase, getTestCaseIds, testXXXX
 * 
 * 
 */
public class SMTPTestRunner implements ISMTPTestRunner {
	static Logger log = Logger.getLogger(SMTPTestRunner.class);
	Properties config = new Properties();
	String configFileName = "config.properties";

	SocketSMTPSender smtpSender = new SocketSMTPSender();

	/**
	 * These implement the testcases; the runTestCase calls them
	 */
	TTTSenderTests sTest = new TTTSenderTests();
	TTTReceiverTests tTest = new TTTReceiverTests();
	TTTSenderNegativeTests nTest = new TTTSenderNegativeTests(smtpSender,
			config);
	MU2SenderTests mu2senderTests = new MU2SenderTests();
	MU2ReceiverTests mu2receiverTests = new MU2ReceiverTests();
	

	public SMTPTestRunner() throws FileNotFoundException, IOException {
		super();
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			InputStream resourceStream = loader
					.getResourceAsStream(configFileName);
			config.load(resourceStream);
		} catch (Exception e) {
			log.error("Unable to load config.properties!");
		}
	}

	/**
	 * Loops through getTestCaseIds and calls runTest for each on the given
	 * TestInput ti
	 */
	@Override
	public ITestResult[] runAllTests(TestInput ti) {
		int[] tids = getTestCaseIds();
		ITestResult[] all = new ITestResult[0];
		for (int t : tids) {
			ITestResult[] tr = runTestCase(t, ti);
			if (tr != null)
				all = concat(all, tr);
		}

		return all;
	}

	/**
	 * @return array of integers containing the implemented testcase ids
	 */
	@Override
	public int[] getTestCaseIds() {
		final int[] ntestCaseIds = { 10, 11, 12, 13, 17 };
		final int[] ptestCaseIds = { 9, 16, 18, 20, 21, 22, 23 };
		final int[] alltestCaseIds = concati(ntestCaseIds, ptestCaseIds);
		final int[] testTest = { 9,16,20,22 };
		return testTest;
	}

	/**
	 *  
	 */
	@Override
	public ITestResult[] runTestCase(int i, TestInput ti) {

		System.setProperty("java.net.preferIPv4Stack", "true");

		ArrayList<ITestResult> res = new ArrayList<ITestResult>();
		log.info("------------------------->  runTestCase " + i);
		ti.useTLS = true; // Enforcing STARTTLS for all cases.
		
		switch (i) {
		case 0:
			// stub to test; not a real test case	
			res.add(nTest.testValid(ti));
			break;

		case 1:
			log.info("*****************   BEGIN  Testcase 1 *******************************");

			TestResult tr;
			try {
				ti.tttUserName = "wellformed1";
				ti.tttPassword = "smtptesting123";
				tr = tTest.fetchMail(ti);
				tr.id = 1;
				res.add(tr);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 1 *******************************");
			break;
		case 9:
			
			log.info("*****************   BEGIN  Testcase 9 *******************************");
			ti.useTLS = false;
			TestResult tr9 = sTest.testSendMail(ti);
			tr9.id = 9;
			res.add(tr9);
			log.info("*****************   END  Testcase 9 *******************************");
			break;

		case 10:
			res.add(nTest.testBadData(ti));
			break;

		case 11:
			res.addAll(nTest.testInvalidSMTP(ti));
			break;

		case 12:
			res.add(nTest.testBigData(ti));
			break;

		case 13:
			res.addAll(nTest.testTimeout(ti));
			break;
			
		case 14:
			log.info("*****************   BEGIN  Testcase 14 *******************************");

			TestResult tr14;
			try {
				tr14 = tTest.fetchMail2(ti);
				tr14.id = 14;
				res.add(tr14);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 14 *******************************");
			break;

		case 15:
			log.warn("------------------------->    Test case (bad certificate) "
					+ i + " not implemented!!!!!!!!!!!!!!!!!!!!!!!!!!");
			break;

		case 16:
			log.info("*****************   BEGIN  Testcase 16 *******************************");
			ti.useTLS = true;
			TestResult tr16 = sTest.testStarttls(ti);
			tr16.id = 16;
			res.add(tr16);
			log.info("*****************   END  Testcase 16 *******************************");
			break;

		case 17:
			res.add(nTest.testInvalidStartTLS(ti));
			break;

		case 18:
			log.info("*****************   BEGIN  Testcase 18 *******************************");

			TestResult tr18;
			try {
				tr18 = tTest.fetchMail(ti);
				tr18.id = 18;
				res.add(tr18);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 18 *******************************");
			break;

		case 19:
			log.warn("------------------------->    Test case (MD5)" + i
					+ " not implemented!!!!!!!!!!!!!!!!!!!!!!!!!!");
			break;

		case 20:
			log.info("*****************   BEGIN  Testcase 20 *******************************");
			TestResult tr20 = sTest.testPlainSasl(ti, false);
			tr20.id = 20;
			res.add(tr20);
			log.info("*****************   END  Testcase 20 *******************************");
			break;

		case 21:
			log.warn("------------------------->    Test case (MD5)" + i
					+ " not tested!!!!!!!!!!!!!!!!!!!!!!!!!!");
			sTest.testDigestMd5(ti);
			break;

		case 22:
			log.info("*****************   BEGIN  Testcase 22 *******************************");
			TestResult tr22 = sTest.testPlainSasl(ti, true);
			tr22.id = 22;
			res.add(tr22);
			log.info("*****************   END  Testcase 22 *******************************");
			break;
			
		case 23:
			log.warn("------------------------->    Test case (MD5)" + i
					+ " not tested!!!!!!!!!!!!!!!!!!!!!!!!!!");
			sTest.testDigestMd5(ti); // take another param useBadPass
			return null;

		case 101:
			log.info("*****************   BEGIN  Testcase 101 *******************************");
			ti.useTLS = true;
			TestResult tr101;
			tr101 = mu2senderTests.testBadAddress(ti);
			tr101.id = 101;
			res.add(tr101);
			
			log.info("*****************   END  Testcase 101 *******************************");
			break;
			
		case 1012:
			log.info("*****************   BEGIN  Testcase 1012 *******************************");
			ti.useTLS = true;
			TestResult tr1012;
			try {
				tr1012 = mu2receiverTests.fetchMail(ti);
				tr1012.id = 1012;
				res.add(tr1012);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			log.info("*****************   END  Testcase 1012 *******************************");
			break;
			
		case 102:
			log.info("*****************   BEGIN  Testcase 102 *******************************");
			ti.useTLS = true;
			TestResult tr102;
			tr102 = mu2senderTests.testMu2Two(ti);
			tr102.id = 102;
			res.add(tr102);
			log.info("*****************   END  Testcase 102 *******************************");
			break;
			
		case 103:
			log.info("*****************   BEGIN  Testcase 103 *******************************");
			ti.useTLS = true;
			TestResult tr103;
			tr103 = mu2senderTests.testMu2Three(ti);
			tr103.id = 103;
			res.add(tr103);
			
			log.info("*****************   END  Testcase 103 *******************************");
			break;
			
		case 104:
			log.info("*****************   BEGIN  Testcase 104 *******************************");
			ti.useTLS = true;
			TestResult tr104;
			tr104 = mu2senderTests.testMu2Four(ti);
			tr104.id = 104;
			res.add(tr104);
			
			log.info("*****************   END  Testcase 104 *******************************");
			break;
			
		case 105:
			log.info("*****************   BEGIN  Testcase 101 *******************************");
			log.info("*****************   END  Testcase 101 *******************************");
			break;
			
		case 106:
			log.info("*****************   BEGIN  Testcase 101 *******************************");
			log.info("*****************   END  Testcase 101 *******************************");
			break;
			
		case 107:
			log.info("*****************   BEGIN  Testcase 101 *******************************");
			log.info("*****************   END  Testcase 101 *******************************");
			break;
			
		case 108:
			log.info("*****************   BEGIN  Testcase 101 *******************************");
			log.info("*****************   END  Testcase 101 *******************************");
			break;
			
		case 109:
			log.info("*****************   BEGIN  Testcase 101 *******************************");
			log.info("*****************   END  Testcase 101 *******************************");
			break;
			
		case 117:
			log.info("*****************   BEGIN  Testcase 117 *******************************");

			TestResult tr117;
			try {
				tr117 = tTest.fetchUniqueId(ti);
				tr117.id = 117;
				res.add(tr117);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 117 *******************************");
			break;
			
		case 121:
			log.info("*****************   BEGIN  Testcase 121 *******************************");

			TestResult tr121;
				tr121 = mu2senderTests.testDispositionNotification(ti);
				tr121.id = 121;
				res.add(tr121);
			log.info("*****************   END  Testcase 121 *******************************");
			break;
			
		case 122:
			log.info("*****************   BEGIN  Testcase 122 *******************************");

			TestResult tr122;
				tr122 = mu2senderTests.testBadDispositionNotification(ti);
				tr122.id = 122;
				res.add(tr122);
			log.info("*****************   END  Testcase 122 *******************************");
			break;
			
		case 123:
			log.info("*****************   BEGIN  Testcase 123 *******************************");
			ti.useTLS = true;
			TestResult tr123;
			tr123 = mu2senderTests.testBadAddress(ti);
			tr123.id = 123;
			res.add(tr123);
			log.info("*****************   END  Testcase 123 *******************************");
			break;
			
		case 124:
			log.info("*****************   BEGIN  Testcase 124 *******************************");
			ti.useTLS = true;
			TestResult tr124;
			tr124 = mu2senderTests.testMu2Two(ti);
			tr124.id = 124;
			res.add(tr124);
			
			log.info("*****************   END  Testcase 124 *******************************");
			break;
			
		case 125:
			log.info("*****************   BEGIN  Testcase 125 *******************************");
			ti.useTLS = true;
			TestResult tr125;
			tr125 = mu2senderTests.testMu2Three(ti);
			tr125.id = 125;
			res.add(tr125);
			
			log.info("*****************   END  Testcase 125 *******************************");
			break;
			
		case 126:
			log.info("*****************   BEGIN  Testcase 126 *******************************");
			ti.useTLS = true;
			TestResult tr126;
			tr126 = mu2senderTests.testMu2Four(ti);
			tr126.id = 126;
			res.add(tr126);
			
			log.info("*****************   END  Testcase 126 *******************************");
			break;
			
		case 127:
			log.info("*****************   BEGIN  Testcase 127 *******************************");
			ti.useTLS = true;
			TestResult tr127;
			tr127 = mu2senderTests.testMu2TwoEight(ti,"processedonly5@hit-dev.nist.gov");
			tr127.id = 127;
			res.add(tr127);
			
			log.info("*****************   END  Testcase 127 *******************************");
			break;
			
		case 128:
			log.info("*****************   BEGIN  Testcase 128 *******************************");
			ti.useTLS = true;
			TestResult tr128;
			tr128 = mu2senderTests.testMu2TwoEight(ti,"processdelayeddispatch7@hit-dev.nist.gov");
			tr128.id = 127;
			res.add(tr128);
			
			log.info("*****************   END  Testcase 128 *******************************");
			break;
			
			
			
		case 129:
			log.info("*****************   BEGIN  Testcase 129 *******************************");

			TestResult tr129;
				tr129 = mu2senderTests.testPositiveDeliveryNotification(ti);
				tr129.id = 129;
				res.add(tr129);
			log.info("*****************   END  Testcase 129 *******************************");
			break;
			
		case 139:
			log.info("*****************   BEGIN  Testcase 139 *******************************");

			TestResult tr139;
				tr139 = mu2senderTests.testBadDispositionNotification(ti);
				tr139.id = 139;
				res.add(tr139);
			log.info("*****************   END  Testcase 139 *******************************");
			break;
			
		case 140:
			log.info("*****************   BEGIN  Testcase 140 *******************************");

			TestResult tr140;
				tr140 = mu2senderTests.testBadDispositionNotification(ti);
				tr140.id = 139;
				res.add(tr140);
			log.info("*****************   END  Testcase 140 *******************************");
			break;
			
		case 145:
			log.info("*****************   BEGIN  Testcase 145 *******************************");

			TestResult tr145;
			try {
				tr145 = tTest.fetchUniqueId(ti);
				tr145.id = 145;
				res.add(tr145);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 145 *******************************");
			break;
			
		case 146:
			log.info("*****************   BEGIN  Testcase 146 *******************************");

			TestResult tr146;
			try {
				tr146 = tTest.fetchDispositionNotificaton(ti);
				tr146.id = 146;
				res.add(tr146);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 146 *******************************");
			break;
			
		case 147:
			log.info("*****************   BEGIN  Testcase 147 *******************************");

			TestResult tr147;
			try {
				tr147 = tTest.fetchManualTest(ti);
				tr147.id = 147;
				res.add(tr147);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 147 *******************************");
			break;
			
		case 201:
			log.info("*****************   BEGIN  Testcase 201 *******************************");

			TestResult tr201;
			
			try {
				tr201 = tTest.SocketImap(ti);
				tr201.id = 201;
				res.add(tr201);
			} catch (KeyManagementException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				
			
			log.info("*****************   END  Testcase 201 *******************************");
			break;
			
		case 204:
			log.info("*****************   BEGIN  Testcase 204 *******************************");

			TestResult tr204;

				tr204 = tTest.SocketStarttls(ti);
				tr204.id = 204;
				res.add(tr204);

			log.info("*****************   END  Testcase 204 *******************************");
			break;
			
		case 209:
			log.info("*****************   BEGIN  Testcase 209 *******************************");

			TestResult tr209;
			
				tr209 = tTest.SocketImapBadSyntax(ti);
				tr209.id = 209;
				res.add(tr209);
			
			log.info("*****************   END  Testcase 209 *******************************");
			break;
			
		case 210:
			log.info("*****************   BEGIN  Testcase 210 *******************************");

			TestResult tr210;
			
				tr210 = tTest.SocketImapBadState(ti);
				tr210.id = 210;
				res.add(tr210);
			
			log.info("*****************   END  Testcase 210 *******************************");
			break;

			
		case 217:
			log.info("*****************   BEGIN  Testcase 217 *******************************");

			TestResult tr217;
			try {
				tr217 = tTest.imapFetchWrongPass(ti);
				tr217.id = 217;
				res.add(tr217);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 217 *******************************");
			break;
			
		case 219:
			log.info("*****************   BEGIN  Testcase 219 *******************************");

			TestResult tr219;
			try {
				tr219 = tTest.fetchManualTestEdge(ti);
				tr219.id = 219;
				res.add(tr219);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 219 *******************************");
			break;
		
		case 221:
			log.info("*****************   BEGIN  Testcase 221 *******************************");

			TestResult tr221;
			try {
				tr221 = tTest.fetchTestMailboxNames(ti);
				tr221.id = 221;
				res.add(tr221);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 221 *******************************");
			break;
			
		case 205:
			log.info("*****************   BEGIN  Testcase 205 *******************************");

			TestResult tr205;
			try {
				tr205 = tTest.imapFetch(ti);
				tr205.id = 205;
				res.add(tr205);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 205 *******************************");
			break;
			
		case 212:
			log.info("*****************   BEGIN  Testcase 212 *******************************");

			TestResult tr212;
			try {
				tr212 = tTest.imapFetchUid(ti);
				tr212.id = 212;
				res.add(tr212);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 212 *******************************");
			break;
		
		case 213:
			log.info("*****************   BEGIN  Testcase 213 *******************************");

			TestResult tr213;
			try {
				tr213 = tTest.imapFetch13(ti);
				tr213.id = 213;
				res.add(tr213);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 213 *******************************");
			break;
			
		case 214:
			log.info("*****************   BEGIN  Testcase 214 *******************************");

			TestResult tr214;
			try {
				tr214 = tTest.imapFetch14(ti);
				tr214.id = 214;
				res.add(tr214);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("*****************   END  Testcase 214 *******************************");
			break;
			
		case 225:
			log.info("*****************   BEGIN  Testcase 225 *******************************");

			TestResult tr225;
			
				tr225 = tTest.testStatusUpdate(ti);
				tr225.id = 225;
				res.add(tr225);
			
			log.info("*****************   END  Testcase 225 *******************************");
			break;

		default:
			log.warn("------------------------->    Test case " + i
					+ " not implemented!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

		log.info("-------------------------> END runTestCase " + i);
		return res.toArray(new ITestResult[0]);
	}

	/**
	 * Concatenates two arrays and returns the concatenated array
	 * 
	 * @param first
	 *            array
	 * @param second
	 *            array
	 * @return concatenated array
	 */
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static int[] concati(int[] first, int[] second) {
		int[] third = new int[first.length + second.length];

		System.arraycopy(first, 0, third, 0, first.length);
		System.arraycopy(second, 0, third, first.length, second.length);
		return third;
	}
}