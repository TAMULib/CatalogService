package edu.tamu.catalog.properties;

import lombok.Data;

@Data
public abstract class AbstractCatalogServiceProperties implements CatalogServiceProperties {

    private String type;

    private String name;

}
