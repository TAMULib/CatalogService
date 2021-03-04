package edu.tamu.catalog.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FolioProperties extends AbstractCatalogServiceProperties {

    private String baseOkapiUrl;

    private String baseEdgeUrl;

    private String tenant;

    private String username;

    private String password;

    private String edgeApiKey;

    // for constructing OAI identifier
    // `oai:<repositoryBaseUrl>:<tenantId>/<uuid of record>`
    private String repositoryBaseUrl;

    @JsonCreator
    public FolioProperties(
        @JsonProperty(value = "name", required = true) String name,
        @JsonProperty(value = "type", required = true) String type,
        @JsonProperty(value = "baseOkapiUrl", required = true) String baseOkapiUrl,
        @JsonProperty(value = "baseEdgeUrl", required = true) String baseEdgeUrl,
        @JsonProperty(value = "tenant", required = true) String tenant,
        @JsonProperty(value = "username", required = true) String username,
        @JsonProperty(value = "password", required = true) String password,
        @JsonProperty(value = "edgeApiKey", required = true) String edgeApiKey,
        @JsonProperty(value = "repositoryBaseUrl", required = true) String repositoryBaseUrl
    ) {
        setName(name);
        setType(type);
        setBaseOkapiUrl(baseOkapiUrl);
        setBaseOkapiUrl(baseOkapiUrl);
        setBaseEdgeUrl(baseEdgeUrl);
        setTenant(tenant);
        setUsername(username);
        setPassword(password);
        setEdgeApiKey(edgeApiKey);
        setRepositoryBaseUrl(repositoryBaseUrl);
    }
    
    

}
