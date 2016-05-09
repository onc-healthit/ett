/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.parsing;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 *
 * @author mccaffrey
 */
public class XDRValidationEventHandler implements ValidationEventHandler {

    private Collection<ValidationEvent> validationEvents;

    public XDRValidationEventHandler() {
        super();    
        this.validationEvents = new ArrayList<ValidationEvent>();
    }
    
    
    @Override
    public boolean handleEvent(ValidationEvent event) {
        this.getValidationEvents().add(event);
        System.out.println("\nEVENT");
        System.out.println("SEVERITY:  " + event.getSeverity());
        System.out.println("MESSAGE:  " + event.getMessage());
        System.out.println("LINKED EXCEPTION:  " + event.getLinkedException());
        System.out.println("LOCATOR");
        System.out.println("    LINE NUMBER:  " + event.getLocator().getLineNumber());
        System.out.println("    COLUMN NUMBER:  " + event.getLocator().getColumnNumber());
        System.out.println("    OFFSET:  " + event.getLocator().getOffset());
        System.out.println("    OBJECT:  " + event.getLocator().getObject());
        System.out.println("    NODE:  " + event.getLocator().getNode());
        System.out.println("    URL:  " + event.getLocator().getURL());
        return true;
    }

    /**
     * @return the validationEvents
     */
    public Collection<ValidationEvent> getValidationEvents() {
        if(validationEvents == null) 
            validationEvents = new ArrayList<ValidationEvent>();
        return validationEvents;
    }

    /**
     * @param validationEvents the validationEvents to set
     */
    public void setValidationEvents(Collection<ValidationEvent> validationEvents) {
        this.validationEvents = validationEvents;
    }

    


}

