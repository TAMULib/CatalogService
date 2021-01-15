package edu.tamu.app.service;

import java.util.List;
import java.util.Map;

import edu.tamu.app.model.CatalogHolding;

/**
 * An interface describing Catalog Service API connectors
 *
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */
public interface CatalogService {

    List<CatalogHolding> getHoldingsByBibId(String bibId);

    CatalogHolding getHolding(String id, String holdingId);

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);

    String getHost();

    void setHost(String host);

    String getPort();

    void setPort(String port);

    String getApp();

    void setApp(String app);

    String getProtocol();

    void setProtocol(String protocol);

    String getSidPrefix();

    void setSidPrefix(String sidPrefix);

    Map<String, String> getAuthentication();

    void setAuthentication(Map<String, String> authentication);

}
