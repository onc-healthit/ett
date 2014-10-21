package gov.nist.healthcare.ttt.webapp.sampleTests;

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/resources.xml")
public class DbIntegrationTest {

    @Autowired
    private DatabaseInstance db;

    @Test
    public void createUserInDB() throws Exception{
        db.getDf().addUsernamePassword("antoine.gerardin@gmail.com","antoine");
        assertTrue(db.getDf().doesUsernameExist("antoine.gerardin@gmail.com"));
    }
}
