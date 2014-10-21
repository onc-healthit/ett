package gov.nist.healthcare.ttt.webapp.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan({
        "gov.nist.healthcare.ttt.xdr.api",
        "gov.nist.healthcare.ttt.xdr.web"
})
@Configuration
public class ToolkitClientConfig {

}