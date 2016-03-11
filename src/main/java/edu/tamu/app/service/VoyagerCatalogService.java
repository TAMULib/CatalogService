package edu.tamu.app.service;

import java.io.IOException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.app.model.CatalogHolding;

/**
 * A CatalogService implementation for interfacing with the Voyager REST VXWS api   
 * 
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */

class VoyagerCatalogService extends AbstractCatalogService {
	@Autowired
	private ObjectMapper objectMapper;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Fetches holdings from the Voyager API and translates them into catalogHoldings
	 * 
	 * @param bibId
	 * 				String
	 * 
	 * @return List<CatalogHolding>
	 * 
	 */
	public List<CatalogHolding> getHoldingsByBibId(String bibId) {
		try {
			logger.debug("Asking for Record from: "+getAPIBase()+"record/"+bibId+"/?view=full");
			String recordResult = this.getHttpUtility().makeHttpRequest(getAPIBase()+"record/"+bibId+"/?view=full","GET");
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	
	    	Document doc = dBuilder.parse(new InputSource(new StringReader(recordResult)));
	    	
	    	doc.getDocumentElement().normalize();
	    	NodeList dataFields = doc.getElementsByTagName("datafield");
	    	int dataFieldCount = dataFields.getLength();
	    	String isbn = "";
	    	String title = "";
	    	String author = "";
	    	String publisher = "";
	    	String place = "";
	    	String year = "";
	    	for (int i=0;i<dataFieldCount;i++) {
	    		Node currentNode = dataFields.item(i);
	    		switch (currentNode.getAttributes().getNamedItem("tag").getTextContent()) {
	    			case "020":
	    				isbn = currentNode.getChildNodes().item(0).getTextContent().split(" ")[0];;
    				break;
	    			case "245":
	    				title = currentNode.getChildNodes().item(0).getTextContent();
	    			break;
	    			case "100":
	    				author = currentNode.getChildNodes().item(0).getTextContent();
	    			break;
	    			case "264":
	    				NodeList publisherDataNodes = currentNode.getChildNodes();
	    				int childCount = publisherDataNodes.getLength();
	    				place = publisherDataNodes.item(0).getTextContent();
	    				if (childCount > 1) {
	    					publisher = publisherDataNodes.item(1).getTextContent();
	    				}
	    				if (childCount > 2) {
	    					year = publisherDataNodes.item(2).getTextContent();
	    				}
	    			break;
	    		}
	    	}
	    	
	    	logger.debug("Asking for holdings from: "+getAPIBase()+"record/"+bibId+"/holdings?view=items");
			String result = this.getHttpUtility().makeHttpRequest(getAPIBase()+"record/"+bibId+"/holdings?view=items","GET");
			logger.debug("Received holdings from: "+getAPIBase()+"record/"+bibId+"/holdings?view=items");

	    	doc = dBuilder.parse(new InputSource(new StringReader(result)));
	    	
	    	doc.getDocumentElement().normalize();
			NodeList holdings = doc.getElementsByTagName("holding");
			int holdingCount = holdings.getLength();
			
			List<CatalogHolding> catalogHoldings = new ArrayList<CatalogHolding>();
			logger.debug("\n\nThe Holding Count: "+holdingCount);

			for (int i=0;i<holdingCount;i++) {
				logger.debug("Current Holding: "+holdings.item(i).getAttributes().getNamedItem("href").getTextContent());
				NodeList childNodes = holdings.item(i).getChildNodes();
				int childCount = childNodes.getLength();
				logger.debug("The Count of Children: "+childCount);
				Map<String,Map<String,String>> catalogItems = new HashMap<String,Map<String,String>>();
				String marcRecordLeader = childNodes.item(0).getFirstChild().getTextContent();
				String mfhd = childNodes.item(0).getChildNodes().item(1).getTextContent();
				logger.debug("MarcRecordLeader: "+marcRecordLeader);
				logger.debug("MFHD: "+mfhd);
				logger.debug("ISBN: "+isbn);
				logger.debug("Item URL: "+childNodes.item(1).getAttributes().getNamedItem("href").getTextContent());

				for (int j=0;j<childCount;j++) {
					if (childNodes.item(j).getNodeName() == "item") {
						String itemResult = this.getHttpUtility().makeHttpRequest(childNodes.item(j).getAttributes().getNamedItem("href").getTextContent(),"GET");

						logger.debug("Got Item details from: "+childNodes.item(j).getAttributes().getNamedItem("href").getTextContent());
						doc = dBuilder.parse(new InputSource(new StringReader(itemResult)));
						doc.getDocumentElement().normalize();
						NodeList itemDataNode = doc.getElementsByTagName("itemData");

						int itemDataCount = itemDataNode.getLength();
						Map<String,String> itemData = new HashMap<String,String>();
						for (int l=0;l<itemDataCount;l++) {
							if (itemDataNode.item(l).getAttributes().getNamedItem("code") != null) {
								itemData.put(itemDataNode.item(l).getAttributes().getNamedItem("name").getTextContent()+"Code",itemDataNode.item(l).getAttributes().getNamedItem("code").getTextContent());
							}
							itemData.put(itemDataNode.item(l).getAttributes().getNamedItem("name").getTextContent(),itemDataNode.item(l).getTextContent());
						}
						catalogItems.put(childNodes.item(j).getAttributes().getNamedItem("href").getTextContent(),itemData);
					}
				}
				catalogHoldings.add(new CatalogHolding(marcRecordLeader,mfhd,isbn,title,author,publisher,place,year,new HashMap<String,Map<String,String>>(catalogItems)));
				catalogItems.clear();
			}
			return catalogHoldings;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
