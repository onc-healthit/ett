/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

/**
 *
 * @author mccaffrey
 */
public class ErrorItem {
    
    private String codeContext = null;
    private String errorCode = null;
    private String location = null;
    private String severity = null;

    /**
     * @return the codeContext
     */
    public String getCodeContext() {
        return codeContext;
    }

    /**
     * @param codeContext the codeContext to set
     */
    public void setCodeContext(String codeContext) {
        this.codeContext = codeContext;
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @param severity the severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    
}
                
