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
    def getNotification(Message msg) {

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

    private def handleBadNotification(Message message) {
        log.error("$message.status : $message.message")
        log.error("recovery method : Logging error and silent failure")
    }

    private handle(TLSValidationReport report) {
        log.info "handle tls report."


        XDRRecordInterface rec = db.instance.xdrFacade.getLatestXDRRecordByHostname(report.hostname)
        if (rec == null) {
            log.info "could not correlate TLS connection with an existing record."
        } else {
            TestCase testcase = manager.findTestCase(rec.testCaseNumber)
            testcase.notifyTLSReceive(rec, report)
        }
    }

    private handle(TkValidationReport report) {

        XDRRecordInterface rec
        String directFrom = report.directFrom
        String msgId = report.messageId

        if (directFrom != null) {
            rec = db.instance.xdrFacade.getLatestXDRRecordByDirectFrom(directFrom)

            if (rec != null) {
                log.info("found correlation with existing record using directFrom address : $directFrom")
            } else {
                log.warn("could not find report correlated with the following directFrom address : $directFrom")
            }
        } else if (msgId != null) {
            String unescapedMsgId = "<" + msgId + ">"
            rec = db.instance.xdrFacade.getXDRRecordByMessageId(unescapedMsgId)

            if (rec != null) {
                log.info("found correlation with existing record using messageID : $msgId")
            } else {
                log.warn("could not find report correlated with the following messageID : $msgId")
            }
        } else {
            String simId = report.simId
            rec = db.getLatestXDRRecordBySimulatorId(simId)

            if (rec != null) {
                log.info("found correlation with existing record using simId : $simId")
            } else {
                log.error("error : could not correlate report with any existing record")
                throw new Exception("error : could not correlate report with any existing record")
            }
        }

        TestCase testcase = manager.findTestCase(rec.testCaseNumber)
        testcase.notifyXdrReceive(rec, report)
    }


}
