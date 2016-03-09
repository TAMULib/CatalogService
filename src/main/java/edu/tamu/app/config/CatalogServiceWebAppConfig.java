/* 
 * TamuWebserviceSeedWebAppConfig.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import edu.tamu.app.controller.interceptor.AppRestInterceptor;
import edu.tamu.framework.config.CoreWebAppConfig;

/**
 * Catalog Service Web App Configuration.
 * 
 */
@Configuration
@ComponentScan(basePackages = { "edu.tamu.app.config", "edu.tamu.app.controller", "edu.tamu.app.service" })
@ConfigurationProperties(prefix = "app.controller")
@PropertySource("classpath:config/buttons.properties")
@EnableJpaRepositories(basePackages = { "edu.tamu.app.model.repo" })
@EntityScan(basePackages = { "edu.tamu.app.model" })
public class CatalogServiceWebAppConfig extends CoreWebAppConfig {

    /**
     * Rest interceptor bean.
     *
     * @return RestInterceptor
     *
     */
    @Bean
    public AppRestInterceptor restInterceptor() {
        return new AppRestInterceptor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(restInterceptor()).addPathPatterns("/**");
    }
}
