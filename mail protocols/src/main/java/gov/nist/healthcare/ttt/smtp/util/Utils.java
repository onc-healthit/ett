package gov.nist.healthcare.ttt.smtp.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.util.Date;

public class Utils {
	public static void pause() {
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Long getTime(Class<?> cl) {
	    try {
	        String rn = cl.getName().replace('.', '/') + ".class";
	        JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
	        return j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
	    } catch (Exception e) {
	        return new Date().getTime();
	    }
	}
}
