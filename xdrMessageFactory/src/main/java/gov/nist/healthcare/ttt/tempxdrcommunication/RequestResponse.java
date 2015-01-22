/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.healthcare.ttt.tempxdrcommunication;

/**
 *
 * @author mccaffrey
 */
public class RequestResponse {
    
    private String request = null;
    private String response = null;

    public RequestResponse() {}
    
    public RequestResponse(String request, String response) {
        this.setRequest(request);
        this.setResponse(response);
    }
    
    /**
     * @return the request
     */
    public String getRequest() {
        return request;
    }

    /**
     * @param request the request to set
     */
    public void setRequest(String request) {
        this.request = request;
    }

    /**
     * @return the response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(String response) {
        this.response = response;
    }
    
}
