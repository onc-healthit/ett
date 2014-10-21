package gov.nist.healthcare.ttt.webapp.direct.listener;

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class DirectListener implements Runnable {

	private int port = 25;
	private int maxConnections = 0;
	
	private DatabaseInstance db;
	private ServletContext context;
	private ArrayList<Thread> threadsList = new ArrayList<Thread>();

	// Config Json

	JSONObject configJson;
	
	private static Logger logger = Logger.getLogger(DirectListener.class.getName());

	public DirectListener(DatabaseInstance db, ServletContext context) {
		this.db = db;
		this.context = context;
		this.configJson = getConfig(context);
		this.port = Integer.parseInt(this.configJson.getJSONObject("config").getString("listenerPort"));
	}

	// Listen for incoming connections and handle them
	public void run() {
		logger.info("Starting listener on port: " + this.port);
		int i = 0;

		try{
			ServerSocket listener = new ServerSocket(port);
			Socket server;

			while((i++ < maxConnections) || (maxConnections == 0)) {
				server = listener.accept();
				logger.debug("Running listener");
				ListenerProcessor processor = new ListenerProcessor(server, configJson, db, context.getContextPath());
				Thread t = new Thread(processor);
				threadsList.add(t);
				t.start();
			}
			listener.close();
		} 
		catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
		catch (Exception ioe) {
			System.out.println("Exception on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}

    public JSONObject getConfig(ServletContext context) {

        try {

            URL path = this.getClass().getClassLoader().getResource("config/settings.json");
            final InputStream is = path.openStream();
            String jsonTxt = IOUtils.toString(is);
            return new JSONObject(jsonTxt);

        } catch (IOException e) {
            logger.error("Could not find the configuration file settings.json");
            e.printStackTrace();
        }

        return new JSONObject();

    }
	
	public void stopThreads() {
		for(Thread t : threadsList) {
			t.interrupt();
		}
	}

}