package edu.tamu.app.service;

import java.util.List;

import edu.tamu.app.model.CatalogHolding;
import edu.tamu.framework.util.HttpUtility;

/**
 * An interface describing Catalog Service API connectors    
 * 
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */
public interface CatalogService {

	List<CatalogHolding> getHoldingsByBibId(String bibId);
	
	String getName();
	
	void setName(String name);
	
	String getType();
	
	void  setType(String type);
	
	String getHost();
	
	void setHost(String host);
	
	String getPort();
	
	void setPort(String port);

	String getApp();
	
	void setApp(String app);
	
	String getProtocol();
	
	void setProtocol(String protocol);
	
	HttpUtility getHttpUtility();
	
	void setHttpUtility(HttpUtility httpUtility);

}
