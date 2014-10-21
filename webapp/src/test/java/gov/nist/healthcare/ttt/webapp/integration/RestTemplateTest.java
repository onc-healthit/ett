package gov.nist.healthcare.ttt.webapp.integration;

import gov.nist.healthcare.ttt.webapp.Application;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.*;

/**
 * Created by gerardin on 9/30/14.
 */
@WebAppConfiguration
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = Application.class)
public class RestTemplateTest {

    @Value("${local.server.port}")
     private int port;




    @Test
    public void hello2() throws Exception {
        //same as RestTemplate, but can have user/pass headers
        RestTemplate template = new TestRestTemplate();
    }



    //There for documentation of rest template
    @Ignore
    @Test
    public void hello() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        System.out.print(port);

        RestTemplate template = new RestTemplate();

        HttpEntity<String> requestEntity = new HttpEntity<String>(null,headers);
        ResponseEntity<String> entity = template.getForEntity( "http://localhost:8080/ttt/hello",String.class);

        assertThat( entity.getBody() , containsString("World") );
    }
}