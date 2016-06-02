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
    private String[] additionalDirectTo = null;
    private String directFrom = null;
    private String directRelatesTo = null;
    private String directRecipient = null;
    private String wsaTo = null;
    private String messageId = null;
    private String finalDestinationDelivery = null;
    private String payload = null;
    private String patientId = null;

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

    /**
     * @return the finalDestinationDelivery
     */
    public String getFinalDestinationDelivery() {
        return finalDestinationDelivery;
    }

    /**
     * @param finalDestinationDelivery the finalDestinationDelivery to set
     */
    public void setFinalDestinationDelivery(String finalDestinationDelivery) {
        this.finalDestinationDelivery = finalDestinationDelivery;
    }

    /**
     * @return the additionalDirectTo
     */
    public String[] getAdditionalDirectTo() {
        return additionalDirectTo;
    }

    /**
     * @param additionalDirectTo the additionalDirectTo to set
     */
    public void setAdditionalDirectTo(String[] additionalDirectTo) {
        this.additionalDirectTo = additionalDirectTo;
    }

    /**
     * @return the payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * @return the patientId
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * @param patientId the patientId to set
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }


}
