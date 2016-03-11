package edu.tamu.app.service;

import edu.tamu.framework.util.HttpUtility;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	protected String getAPIBase() {
		return getProtocol() +"://"+getHost()+":"+getPort()+"/"+getApp()+"/";
	}
}
