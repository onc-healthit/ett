package gov.nist.healthcare.ttt.fakeToolkit
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan("gov.nist.healthcare.ttt.fakeToolkit")
@EnableAutoConfiguration
public class FakeToolkitApplication {

    public static void main(String[] args) {
        SpringApplication.run(FakeToolkitApplication.class, args)

    }

}