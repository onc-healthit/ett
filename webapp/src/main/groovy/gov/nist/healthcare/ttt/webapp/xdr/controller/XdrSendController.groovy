package gov.nist.healthcare.ttt.webapp.xdr.controller

import com.wordnik.swagger.annotations.ApiOperation
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

/**
 * Created by gerardin on 10/17/14.
 */

@RestController
@RequestMapping("/xdr/send")
class XdrSendController {

    private final DatabaseInstance db
    private final XdrSender sender

    @Autowired
    public XdrSendController(XdrSender xdrSender, DatabaseInstance database) {
    }

    @ApiOperation(value = "send an xdr ")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    UserMessage send(@RequestBody Object body) {

    }
}
