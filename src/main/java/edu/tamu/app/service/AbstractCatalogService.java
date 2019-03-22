package edu.tamu.app.service;

import edu.tamu.weaver.utility.HttpUtility;

/**
 * A base implementation of the Catalog Service interface
 *
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */

public abstract class AbstractCatalogService implements CatalogService {

    private String name;

    private String type;

    private String host;

    private String port;

    private String app;

    private String protocol;

    private HttpUtility httpUtility;

    public HttpUtility getHttpUtility() {
        return httpUtility;
    }

    public void setHttpUtility(HttpUtility httpUtility) {
        this.httpUtility = httpUtility;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String getApp() {
        return app;
    }

    @Override
    public void setApp(String app) {
        this.app = app;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    protected String getAPIBase() {
        return getProtocol() + "://" + getHost() + ":" + getPort() + "/" + getApp() + "/";
    }
}
