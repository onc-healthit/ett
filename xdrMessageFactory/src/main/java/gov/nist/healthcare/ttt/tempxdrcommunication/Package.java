
package gov.nist.healthcare.ttt.tempxdrcommunication;

import java.util.Collection;

/**
 *
 * @author mccaffrey
 */
public class Package {
    
    private String metadata = null;
    private String attachment = null;
    private Collection<String> attachments = null;

    /**
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the attachments
     */
    public Collection<String> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(Collection<String> attachments) {
        this.attachments = attachments;
    }

    /**
     * @return the attachment
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     * @param attachment the attachment to set
     */
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
    
}
