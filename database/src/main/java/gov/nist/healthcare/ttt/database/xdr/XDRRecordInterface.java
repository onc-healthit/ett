
package gov.nist.healthcare.ttt.database.xdr;


import java.util.List;

/**
 * Created Oct 17, 2014 1:47:19 PM
 * @author mccaffrey
 */
public interface XDRRecordInterface {

    public enum CriteriaMet {        
        PASSED,
        FAILED,
        PENDING,
        CANCELLED,
        MANUAL,
    }
    
    /**
     * @return the testCaseNumber
     */
    String getTestCaseNumber();

    /**
     * @return the testSteps
     */
    List<XDRTestStepInterface> getTestSteps();

    /**
     * @return the timestamp
     */
    String getTimestamp();

    /**
     * @return the username
     */
    String getUsername();

    /**
     * @return the criteriaMet
     */
    CriteriaMet getCriteriaMet();

    /**
     * @param criteriaMet the criteriaMet to set
     */
    void setCriteriaMet(CriteriaMet criteriaMet);

    /**
     * @param testCaseNumber the testCaseNumber to set
     */
    void setTestCaseNumber(String testCaseNumber);

    /**
     * @param testSteps the testSteps to set
     */
    void setTestSteps(List<XDRTestStepInterface> testSteps);

    /**
     * @param timestamp the timestamp to set
     */
    void setTimestamp(String timestamp);

    /**
     * @param username the username to set
     */
    void setUsername(String username);

}
