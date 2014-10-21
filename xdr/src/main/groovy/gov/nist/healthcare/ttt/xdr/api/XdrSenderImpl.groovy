package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.xdr.domain.Message
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/21/14.
 */
@Component
class XdrSenderImpl implements XdrSender{

    @Override
    Message<Object> sendXdr(Object config) {
        return null
    }
}
