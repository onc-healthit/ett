/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.tempxdrcommunication.artifact;

/**
 *
 * @author mccaffrey
 */
public class Settings {

    private String directTo = null;
    private String directFrom = null;
    private String directRelatesTo = null;
    private String directRecipient = null;
    private String wsaTo = null;
    private String messageId = null;

    /**
     * @return the directTo
     */
    public String getDirectTo() {
        return directTo;
    }

    /**
     * @param directTo the directTo to set
     */
    public void setDirectTo(String directTo) {
        this.directTo = directTo;
    }

    /**
     * @return the directFrom
     */
    public String getDirectFrom() {
        return directFrom;
    }

    /**
     * @param directFrom the directFrom to set
     */
    public void setDirectFrom(String directFrom) {
        this.directFrom = directFrom;
    }

    /**
     * @return the directRelatesTo
     */
    public String getDirectRelatesTo() {
        return directRelatesTo;
    }

    /**
     * @param directRelatesTo the directRelatesTo to set
     */
    public void setDirectRelatesTo(String directRelatesTo) {
        this.directRelatesTo = directRelatesTo;
    }

    /**
     * @return the directRecipient
     */
    public String getDirectRecipient() {
        return directRecipient;
    }

    /**
     * @param directRecipient the directRecipient to set
     */
    public void setDirectRecipient(String directRecipient) {
        this.directRecipient = directRecipient;
    }

    /**
     * @return the wsaTo
     */
    public String getWsaTo() {
        return wsaTo;
    }

    /**
     * @param wsaTo the wsaTo to set
     */
    public void setWsaTo(String wsaTo) {
        this.wsaTo = wsaTo;
    }

    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

}
