package gov.nist.healthcare.ttt.webapp

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

/**
 * Created by gerardin on 2/5/15.
 */
class XDRSpecification extends Specification{

    Logger log = LoggerFactory.getLogger(this.class)

    @Autowired
    XdrTestCaseController controller

    @Autowired
    DatabaseInstance db

    @Autowired
    TkListener listener

    MockMvc gui
    MockMvc toolkit

    /*
    Default values
     */
    String userId = "user1"
    String userPass = "pass"
    String messageID = "1"
    String toAddress = "from@edge.nist.gov"
    String fromAddress = "to@edge.nist.gov"

    String system = "ett"

    @Before
    public setup() {

        setupDb()
        //Set up mockmvc with the necessary converter (json or xml)

        gui = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()

        toolkit = MockMvcBuilders.standaloneSetup(listener)
                .build()
    }

    def setupDb() {
        createUserInDB()
        db.xdrFacade.removeAllByUsername(userId)
        log.info("db data fixture set up.")
    }

    def createUserInDB() throws Exception {
        if (!db.getDf().doesUsernameExist(userId)) {
            db.getDf().addUsernamePassword(userId, userPass)
        }
        assert db.getDf().doesUsernameExist(userId)
    }



}
