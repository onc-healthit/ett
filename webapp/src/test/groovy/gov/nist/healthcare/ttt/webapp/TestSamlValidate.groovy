package gov.nist.healthcare.ttt.webapp;

import gov.nist.hit.xdrsamlhelper.SamlHeaderApi.SamlHeaderException;
import gov.nist.hit.xdrsamlhelper.SamlHeaderApiImpl;

public class TestSamlValidate {

	public static void main(String[] args) {
		try {
			SamlHeaderApiImpl.main(null);
			// test1();
		} catch (SamlHeaderException e) {
			// TODO Auto-generated catch block;
			e.printStackTrace();
		}

	}

}
