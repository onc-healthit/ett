package gov.nist.healthcare.ttt.webapp.xdr.core

import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import gov.nist.healthcare.ttt.xdr.api.TLSReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.TLSValidationReport
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 *
 * This component receives notification from the XDR layer
 * and try to find a correlated record.
 * If found, it called the appropriate test case to resume the test workflow.
 *
 * Created by gerardin on 10/14/14.
 */
@Component
class ResponseHandler implements IObserver {

    private static Logger log = LoggerFactory.getLogger(ResponseHandler.class)

    private final TestCaseManager manager
    private final XdrReceiver xdrReceiver
    private final TLSReceiver tlsReceiver
    private final DatabaseProxy db

    @Autowired
    public ResponseHandler(TestCaseManager manager, XdrReceiver xdrReceiver, TLSReceiver tlsReceiver, DatabaseProxy db) {
        this.manager = manager
        this.xdrReceiver = xdrReceiver
        this.tlsReceiver = tlsReceiver
        this.db = db
        xdrReceiver.registerObserver(this)
        tlsReceiver.registerObserver(this)
    }

    @Override
    public getNotification(Message msg) {

        log.info "notification received"

        if (msg.status == Message.Status.ERROR) {
            handleBadNotification(msg)
        } else {

            try {
                handle(msg.content)
            }
            catch (Exception e) {
                e.printStackTrace()
                log.error "notification content not understood"
            }
        }
    }

    //TODO check : do we ever generate this kind of message?
    private def handleBadNotification(Message message) {
        log.error("$message.status : $message.message")
        log.error("recovery method : Logging error and silent failure")
    }

    /*
    Handles test case report coming from the TLS socket
     */
    private handle(TLSValidationReport report) {
        log.debug "handle tls report."

        XDRRecordInterface rec = db.instance.xdrFacade.getLatestXDRRecordByHostname(report.hostname)
        if (rec == null) {
            log.info "could not correlate TLS connection with an existing record."
        } else {
            TestCase testcase = manager.findTestCase(rec.testCaseNumber)
            testcase.notifyTLSReceive(rec, report)
        }
    }

    /*
    Handles test case reports coming from the toolkit
     */
    private handle(TkValidationReport report) {

        log.debug "handle toolkit report."

        XDRRecordInterface rec
        String directFrom = report.directFrom
        String directTo = report.directTo
        String simId = report.simId

        //we need both simid and directFrom : without simid, we would not know which testcaseid we are talking about.
        //Without the directFrom address, we would not be able to know who sent us a message.
        rec = db.instance.xdrFacade.getLatestXDRRecordBySimulatorAndDirectAddress(simId, directFrom)

        if (rec != null) {
            log.info("found correlation with existing record using direct from address and simId : $directFrom , $simId")
        } else{
            rec = db.instance.xdrFacade.getLatestXDRRecordBySimulatorAndDirectAddress(simId, directTo)
            if(rec != null) {
                log.info("found correlation with existing record using direct to address and simId : $directTo , $simId")
            }
            else{
                log.warn("could not find report correlated with the following direct address and simId : $directFrom , $simId")
                throw new Exception("error : could not correlate report with simId ($simId) and directAddress ($directFrom) with any existing record")
            }
        }

        TestCase testcase = manager.findTestCase(rec.testCaseNumber);

        testcase.notifyXdrReceive(rec, report)
    }


}
