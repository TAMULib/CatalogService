package edu.tamu.catalog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tenant")
public class FolioTenantConfig {

    private String headerName = "X-Okapi-Tenant";

    private String defaultTenant = "public";

}
