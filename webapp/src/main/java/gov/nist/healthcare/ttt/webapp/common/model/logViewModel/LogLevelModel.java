package gov.nist.healthcare.ttt.webapp.common.model.logViewModel;

import java.util.ArrayList;
import java.util.List;

public class LogLevelModel {
	
	public enum LogLevel {
		ALL, INFO, WARNING, ERROR, DEBUG
	}
	
	private List<LogLevel> levels;
	private String logs;
	
	public LogLevelModel() {
		this.levels = new ArrayList<LogLevelModel.LogLevel>();
		this.logs = "";
	}
	
	public LogLevelModel(List<LogLevel> levels, String logs) {
		super();
		this.levels = levels;
		this.logs = logs;
	}

	public List<LogLevel> getLevels() {
		return levels;
	}
	

	public void setLevels(List<LogLevel> levels) {
		this.levels = levels;
	}
	

	public String getLogs() {
		return logs;
	}
	

	public void setLogs(String logs) {
		this.logs = logs;
	}
}
