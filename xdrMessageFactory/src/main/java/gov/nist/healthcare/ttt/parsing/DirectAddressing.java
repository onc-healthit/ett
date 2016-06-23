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
public class DirectAddressing {

    private String messageID = null;
    private String directFrom = null;
    private String directTo = null;

    /**
     * @return the messageID
     */
    public String getMessageID() {
        if (messageID == null) {
            messageID = new String();
        }
        return messageID;
    }

    /**
     * @param messageID the messageID to set
     */
    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    /**
     * @return the directFrom
     */
    public String getDirectFrom() {
        if (directFrom == null) {
            directFrom = new String();
        }
        return directFrom;
    }

    /**
     * @param directFrom the directFrom to set
     */
    public void setDirectFrom(String directFrom) {
        this.directFrom = directFrom;
    }

    /**
     * @return the directTo
     */
    public String getDirectTo() {
        if (directTo == null) {
            directTo = new String();
        }
        return directTo;
    }

    /**
     * @param directTo the directTo to set
     */
    public void setDirectTo(String directTo) {
        this.directTo = directTo;
    }

}
