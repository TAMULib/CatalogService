package edu.tamu.catalog.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VoyagerProperties extends AbstractCatalogServiceProperties {

    private String baseUrl;

    private String sidPrefix;

    @JsonCreator
    public VoyagerProperties(
        @JsonProperty(value = "name", required = true) String name,
        @JsonProperty(value = "type", required = true) String type,
        @JsonProperty(value = "baseUrl", required = true) String baseUrl,
        @JsonProperty(value = "sidPrefix", required = true) String sidPrefix
    ) {
        setName(name);
        setType(type);
        setBaseUrl(baseUrl);
        setSidPrefix(sidPrefix);
    }

}
