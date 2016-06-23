/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author mccaffrey
 */
public class SOAPWithAttachment {
    
    private String soap = null;
    private Collection<byte[]> attachments = null;

    /**
     * @return the soap
     */
    public String getSoap() {
        return soap;
    }

    /**
     * @param soap the soap to set
     */
    public void setSoap(String soap) {
        this.soap = soap;
    }

    /**
     * @return the attachments
     */
    public Collection<byte[]> getAttachment() {
        if (attachments == null)
            attachments = new ArrayList<byte[]>();
        return attachments;
    }

    /**
     * @param attachments the attachments to set
     */
    public void setAttachment(Collection<byte[]> attachments) {
        this.attachments = attachments;
    }
    
}
