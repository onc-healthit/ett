package gov.nist.healthcare.ttt.smtp.util;

public class TestData {
	public static String LONG_LOCAL_PART = "local" + getLongString(70);
	public static String LONG_DOMAIN = "domain" + getLongString(255);
	public static String LONG_DATA = "data" + getLongString(1000);
	public static String LONG_TO = LONG_LOCAL_PART + "@" + LONG_DOMAIN + ".com";
	public static String LONG_FROM = LONG_LOCAL_PART + "@" + LONG_DOMAIN
			+ ".com";

	public static String VALID_DOMAIN = "ttt.nist.gov";
	public static String VALID_TO = "daemon@ttt.nist.gov";
	public static String VALID_FROM = "daemon@ttt.nist.gov";
	public static String VALID_DATA = "This is sample DATA";

	public static String TTT_TIMEOUT_MSG = "-02 Custom Message: Socket Timeout occured";
	public static String SUT_TIMEOUT_MSG = "-03 Custom Message: Null result [Possibly Server Ended the Connection]";

	/**
	 * @param n
	 * @return
	 */
	public static String getLongString(int n) {
		return getLongString(n, 'a');
	}

	public static String getLongString(int n, char a) {
		String s = "";
		for (; n > 0; n--)
			s += a;
		return s;
	}
}
