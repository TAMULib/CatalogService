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

    private Credentials credentials;

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
        @JsonProperty(value = "credentials", required = true) Credentials credentials,
        @JsonProperty(value = "edgeApiKey", required = true) String edgeApiKey,
        @JsonProperty(value = "repositoryBaseUrl", required = true) String repositoryBaseUrl
    ) {
        setName(name);
        setType(type);
        setBaseOkapiUrl(baseOkapiUrl);
        setBaseEdgeUrl(baseEdgeUrl);
        setTenant(tenant);
        setCredentials(credentials);
        setEdgeApiKey(edgeApiKey);
        setRepositoryBaseUrl(repositoryBaseUrl);
    }

}
