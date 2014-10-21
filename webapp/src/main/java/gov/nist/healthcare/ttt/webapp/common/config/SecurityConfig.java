package gov.nist.healthcare.ttt.webapp.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by gerardin on 9/26/14.
 */
@ImportResource("classpath:/spring/context-security.xml")
@Configuration
public class SecurityConfig {
}
