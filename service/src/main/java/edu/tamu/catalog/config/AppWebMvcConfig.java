package edu.tamu.catalog.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import edu.tamu.catalog.resolver.CatalogServiceArgumentResolver;
import edu.tamu.catalog.service.CatalogService;

@EnableWebMvc
@Configuration
public class AppWebMvcConfig extends WebMvcConfigurerAdapter {

    @Value("${app.security.allow-access}")
    private String[] hosts;

    @Lazy
    @Autowired
    private List<CatalogService> catalogServices;

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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new CatalogServiceArgumentResolver(catalogServices));
    }

}
