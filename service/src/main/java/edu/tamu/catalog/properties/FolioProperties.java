package edu.tamu.catalog.properties;

import lombok.Data;

@Data
public class FolioProperties implements CatalogServiceProperties {

  private String name;

  private String baseOkapiUrl;

  private String baseEdgeUrl;

  private String tenant;

  private String username;

  private String password;

  private String edgeApiKey;

  // for constructing OAI identifier
  // `oai:<repositoryBaseUrl>:<tenantId>/<uuid of record>`
  private String repositoryBaseUrl;

}
