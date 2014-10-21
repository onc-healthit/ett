package gov.nist.healthcare.ttt.webapp;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import gov.nist.healthcare.ttt.webapp.common.config.ComponentConfig;
import gov.nist.healthcare.ttt.webapp.common.config.SecurityConfig;
import gov.nist.healthcare.ttt.webapp.common.config.ToolkitClientConfig;
import gov.nist.healthcare.ttt.webapp.direct.listener.DirectListenerServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
    @Bean
    public DirectListenerServlet listenerServlet() {
        return new DirectListenerServlet();
    }


    /*
    Config for swagger, we do not really use it for now
     */
    private SpringSwaggerConfig springSwaggerConfig;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    @Bean //Don't forget the @Bean annotation
    public SwaggerSpringMvcPlugin customImplementation() {
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig);
    }


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


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}