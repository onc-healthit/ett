package gov.nist.healthcare.ttt.webapp.xdr.controller
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.component.ResponseHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@Api(value = "receive", description = "control xdr receiver") // Swagger annotation
@RestController
@RequestMapping("/xdr/receive")
public class XdrReceiveController {

    private final DatabaseInstance db
    private final XdrReceiver receiver
    private final ResponseHandler handler

    @Autowired
    public XdrReceiveController(XdrReceiver xdrReceiver, ResponseHandler xdrResponseHandler, DatabaseInstance database){
        receiver = xdrReceiver
        handler = xdrResponseHandler
        receiver.registerObserver(handler)
        db = database
    }

    @ApiOperation(value = "Create an endpoint on the toolkit")
    @RequestMapping(value = "/{id}/endpoint", method = RequestMethod.POST)
    @ResponseBody
    UserMessage createEndpoint(@PathVariable("id") String id, @RequestBody Object body) {

        //Endpoint config is serializable info for Bill

        EndpointConfig config = new EndpointConfig()
        config.name = "endpoint1"

        try {
            Message<String> r = receiver.createEndpoints(config)
            if(r.success()) {

                db.getDf()
                //TODO create endpoint as well
                //TODO save step success with endpoint info
                return new UserMessage(UserMessage.Status.SUCCESS, "create new endpoint for test case ${id} with config : ${body}")
            }
        }
        catch(Exception e){
            return new UserMessage(UserMessage.Status.ERROR, "unable to create endpoints")
        }

        //TODO save step failure
        return new UserMessage(UserMessage.Status.ERROR, "unable to create endpoints")
    }



}