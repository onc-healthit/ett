package gov.nist.healthcare.ttt.webapp;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import gov.nist.healthcare.ttt.webapp.common.config.ComponentConfig;
import gov.nist.healthcare.ttt.webapp.common.config.SecurityConfig;
import gov.nist.healthcare.ttt.webapp.common.config.ToolkitClientConfig;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.io.IOException;

@EnableWebMvcSecurity
@EnableAutoConfiguration
@EnableSwagger
@Import({ComponentConfig.class,
        SecurityConfig.class,
        ToolkitClientConfig.class
})
@ImportResource("classpath:/spring/resources.xml")
public class Application {

    /*
    Not really necessary since it is equivalent to springboot default
     */
    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
        registration.addUrlMappings("/");
        registration.setLoadOnStartup(1);
        return registration;
    }

    /*
    We register the direct listener Bill's way. We should probably clean that up later
     */
//    @Bean
//    public DirectListenerServlet listenerServlet() {
//        return new DirectListenerServlet();
//    }

    /*
    Config for swagger, we do not really use it for now
     */
    private SpringSwaggerConfig springSwaggerConfig;

//    @Autowired
//    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
//        this.springSwaggerConfig = springSwaggerConfig;
//    }
//
//    @Bean //Don't forget the @Bean annotation
//    public SwaggerSpringMvcPlugin customImplementation() {
//        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig);
//    }


    /*
    Important to integrate swagger-ui code
     */
    @Bean
    public WebMvcConfigurerAdapter customWebMvcConfigurerAdapter() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                super.addViewControllers(registry);
                // Use forward: prefix so that no view resolution is done
                registry.addViewController("/swagger/").setViewName("forward:/index.html");
                return;
            }
        };
    }

/*
we currently do not use tomcat ssl capabilities.
 */
//    @Bean
//    public EmbeddedServletContainerFactory servletContainer() {
//                URL url = getClass().getClassLoader().getResource("application.properties");
//                System.out.println("properties file:" + url.getPath());
//
//
//        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
//   //     tomcat.addAdditionalTomcatConnectors(createSslConnector());
//   //     tomcat.addContextValves(new AccessLogValve());
//        return tomcat;
//    }


    private Connector createSslConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        try {
            File keystore = new ClassPathResource("keystore/keystore").getFile();
            File truststore = new ClassPathResource("keystore/keystore").getFile();
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(8443);
            protocol.setSSLEnabled(true);
            protocol.setKeystoreFile(keystore.getAbsolutePath());
            protocol.setKeystorePass("changeit");
            protocol.setTruststoreFile(truststore.getAbsolutePath());
            protocol.setTruststorePass("changeit");
            protocol.setKeyAlias("1");
            return connector;
        } catch (IOException ex) {
            throw new IllegalStateException("can't access keystore: [" + "keystore"
                    + "] or truststore: [" + "keystore" + "]", ex);
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(Application.class, args);
    }


}