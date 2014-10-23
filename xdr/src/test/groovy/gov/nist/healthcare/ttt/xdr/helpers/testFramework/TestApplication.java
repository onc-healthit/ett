package gov.nist.healthcare.ttt.xdr.helpers.testFramework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"gov.nist.healthcare.ttt.xdr.helpers.testFramework","gov.nist.healthcare.ttt.xdr"})
@EnableAutoConfiguration
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}