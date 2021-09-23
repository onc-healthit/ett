package gov.nist.healthcare.ttt.webapp.direct.listener;

import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


@Component
public class DirectListenerManager {
	
	private static Logger logger = LogManager.getLogger(DirectListenerManager.class.getName());

    Thread t;

    @Autowired
    public DirectListenerManager(DirectListener listener) {
        t = new Thread(listener);
        t.start();
    }

    @PreDestroy
    public void destroy(){
        t.interrupt();
    }

}