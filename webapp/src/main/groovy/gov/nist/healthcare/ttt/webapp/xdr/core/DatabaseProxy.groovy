package gov.nist.healthcare.ttt.webapp.xdr.core

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Avoid handling checked exceptions coming from the database in the rest of the application.
 *
 * Created by gerardin on 10/29/14.
 */
@Component
class DatabaseProxy {

    public final DatabaseInstance instance

    @Autowired
    DatabaseProxy(DatabaseInstance db){
        this.instance = db
    }

    public addNewXdrRecord(XDRRecordInterface record){
        try {
            String recordId = instance.getXdrFacade().addNewXdrRecord(record)
            return recordId
        }
        catch(e){
            e.printStackTrace()
            throw new RuntimeException(MsgLabel.CREATE_NEW_RECORD_FAILED,e)
        }
    }

    public updateXDRRecord(XDRRecordInterface record){
        try {
            instance.getXdrFacade().updateXDRRecord(record)
        }
        catch(e){
            e.printStackTrace()
            throw new RuntimeException(MsgLabel.UPDATE_RECORD_FAILED.msg,e)
        }
    }

    /*
    A user will always poll the latest record in the database.
     */
    XDRRecordInterface getLatestXDRRecordByUsernameTestCase(String username, String tcid){
        instance.xdrFacade.getLatestXDRRecordByUsernameTestCase(username,tcid)
    }




}
