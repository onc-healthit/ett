package gov.nist.healthcare.ttt.webapp.common.db;

import gov.nist.healthcare.ttt.database.jdbc.DatabaseException;
import gov.nist.healthcare.ttt.database.jdbc.DatabaseFacade;
import gov.nist.healthcare.ttt.database.jdbc.LogFacade;
import gov.nist.healthcare.ttt.database.jdbc.SmtpEdgeLogFacade;
import gov.nist.healthcare.ttt.database.jdbc.XDRFacade;

import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

public class DatabaseInstance {
	
	private DatabaseFacade df;
	private LogFacade logFacade;
	private SmtpEdgeLogFacade smtpEdgeLogFacade;

    private XDRFacade xdrFacade;

	@Autowired
	public DatabaseInstance(DatabaseData config) throws SQLException, DatabaseException {
		df = new DatabaseFacade(config);
		logFacade = new LogFacade(config);
		smtpEdgeLogFacade = new SmtpEdgeLogFacade(config);

        //TODO check with Andrew how to use this exception
        try {
            xdrFacade = new XDRFacade(config);
        } catch (gov.nist.healthcare.ttt.database.jdbc.DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

	public DatabaseFacade getDf() {
		return df;
	}

	public void setDf(DatabaseFacade df) {
		this.df = df;
	}

	public LogFacade getLogFacade() {
		return logFacade;
	}

	public void setLogFacade(LogFacade logFacade) {
		this.logFacade = logFacade;
	}

    public XDRFacade getXdrFacade(){
        return xdrFacade;
    }

    public void setXdrFacade(XDRFacade xdrFacade){
        this.xdrFacade = xdrFacade;
    }

	public SmtpEdgeLogFacade getSmtpEdgeLogFacade() {
		return smtpEdgeLogFacade;
	}

	public void setSmtpEdgeLogFacade(SmtpEdgeLogFacade smtpEdgeLogFacade) {
		this.smtpEdgeLogFacade = smtpEdgeLogFacade;
	}
}
