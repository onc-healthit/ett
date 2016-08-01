package gov.nist.healthcare.ttt.smtp.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.util.Date;
import java.util.Properties;

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
	
	
	public static Properties getProp() {
		Properties prop = new Properties();
		try {
			String path = "./application.properties";
			FileInputStream file = new FileInputStream(path);
			prop.load(file);
			file.close();
		} catch (Exception e) {
		e.printStackTrace();
		}
		return prop;
		}
}
