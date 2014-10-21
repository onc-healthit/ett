package gov.nist.healthcare.ttt.webapp.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan({
        "gov.nist.healthcare.ttt.webapp.xdr.controller",
        "gov.nist.healthcare.ttt.webapp.direct.controller",
        "gov.nist.healthcare.ttt.webapp.smtp.controller",
        "gov.nist.healthcare.ttt.webapp.xdr.component",
})
@Configuration
public class ComponentConfig {

}