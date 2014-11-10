package gov.nist.healthcare.ttt.webapp.direct.listener;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;

public class DirectListenerServlet implements ServletContextListener {
	
	private static Logger logger = Logger.getLogger(DirectListenerServlet.class.getName());
	
	DirectListener listener;
	Thread t;
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		logger.info("Starting listener: " + new Date());
		// Get database
		WebApplicationContext servletContext =  WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        DatabaseInstance db = (DatabaseInstance) servletContext.getBean("DatabaseInstance");
		listener = new DirectListener(db, event.getServletContext());
		t = new Thread(listener);
		t.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("Stoping listener: " + new Date());
		
		Enumeration<Driver> drivers = DriverManager.getDrivers();     

        Driver driver = null;

        // clear drivers
        while(drivers.hasMoreElements()) {
            try {
                driver = drivers.nextElement();
                DriverManager.deregisterDriver(driver);

            } catch (SQLException ex) {
                // deregistration failed, might want to do something, log at the very least
            	ex.printStackTrace();
            }
        }
		// MySQL driver leaves around a thread. This static method cleans it up.
        try {
            AbandonedConnectionCleanupThread.shutdown();
        } catch (InterruptedException e) {
            // again failure, not much you can do
        	e.printStackTrace();
        }
        listener.stopThreads();
		t.interrupt();
	}

}
