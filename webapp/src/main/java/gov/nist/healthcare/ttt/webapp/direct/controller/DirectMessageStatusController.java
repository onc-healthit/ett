package gov.nist.healthcare.ttt.webapp.direct.controller;

import gov.nist.healthcare.ttt.database.jdbc.DatabaseException;
import gov.nist.healthcare.ttt.database.log.LogInterface;
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException;
import gov.nist.healthcare.ttt.webapp.direct.model.messageStatus.MessageStatusDetail;
import gov.nist.healthcare.ttt.webapp.direct.model.messageStatus.MessageStatusList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@Controller
@RequestMapping("/api/directMessageStatus")
public class DirectMessageStatusController {
	
	private static Logger logger = Logger.getLogger(DirectMessageStatusController.class.getName());
	
	@Autowired
	private DatabaseInstance db;
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<MessageStatusList> getAllLogsForUser(HttpServletRequest request) throws IOException, DatabaseException, TTTCustomException {

		String username = "";
		Collection<MessageStatusList> logList = new ArrayList<MessageStatusList>();
		
		// Check if user is connected
		Principal principal = request.getUserPrincipal();
		if (principal != null) {
			username = principal.getName();
		} else {
			logger.info("You must be logged to acces this feature");
			throw new TTTCustomException("0x020", "You must be logged to access this feature");
		}
		
		// Iterate for each direct for the user
		Collection<String> directList = db.getDf().getDirectEmailsForUser(username);
		Iterator<String> it = directList.iterator();
		while(it.hasNext()) {
			String direct = it.next();
			
			
			logList.add(convertLog(false, direct, db.getLogFacade().getOutgoingByToLine(direct)));
			logList.add(convertLog(true, direct, db.getLogFacade().getIncomingByFromLine(direct)));
		}
			
		return logList;
	}
	
	public MessageStatusList convertLog(boolean incoming, String direct, Collection<LogInterface> logList) {
		Iterator<LogInterface> logIt = logList.iterator();
		
		Collection<MessageStatusDetail> tmpDetail = new ArrayList<MessageStatusDetail>();
		
		while(logIt.hasNext()) {
			LogInterface tmpLog = logIt.next();
			
			tmpDetail.add(new MessageStatusDetail(getGoodAddressType(incoming, tmpLog), tmpLog.getMessageId(), tmpLog.getOrigDate(), "Success"));
		}
		MessageStatusList tmpList = new MessageStatusList(convertAddressType(incoming), direct, tmpDetail);
		
		return tmpList;
	}
	
	public String convertAddressType(boolean incoming) {
		if(incoming)
			return "From";
		else
			return "To";
	}
	
	public String getGoodAddressType(boolean incoming, LogInterface logEntry) {
		if(incoming)
			return logEntry.getToLine().iterator().next();
		else
			return logEntry.getFromLine().iterator().next();
	}
	
}
