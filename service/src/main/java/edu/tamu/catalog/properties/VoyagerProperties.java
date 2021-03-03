package edu.tamu.catalog.properties;

import lombok.Data;

@Data
public class VoyagerProperties implements CatalogServiceProperties {

  private String name;

  private String type;

  private String baseUrl;

  private String sidPrefix;

}
