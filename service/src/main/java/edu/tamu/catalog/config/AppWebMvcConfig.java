package edu.tamu.catalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
public class AppWebMvcConfig extends WebMvcConfigurerAdapter {

    @Value("${app.security.allow-access}")
    private String[] hosts;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // @formatter:off
        registry.addMapping("/**")
                .allowedOrigins(hosts)
                .allowCredentials(false)
                .allowedMethods("GET", "DELETE", "PUT", "POST")
                .allowedHeaders("Origin", "Content-Type", "Access-Control-Allow-Origin", "x-requested-with", "data", "x-forwarded-for");
        // @formatter:on
    }

}
