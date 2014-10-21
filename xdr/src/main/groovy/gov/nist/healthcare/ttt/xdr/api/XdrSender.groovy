package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.xdr.domain.Message
/**
 * Created by gerardin on 10/6/14.
 */

public interface XdrSender  {


    public Message<Object> sendXdr(Object config)




}
