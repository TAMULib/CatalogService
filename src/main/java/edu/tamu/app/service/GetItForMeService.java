package edu.tamu.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import edu.tamu.app.model.CatalogHolding;
import edu.tamu.app.model.GetItForMeButton;

/**
 * The GetItForMe engine.
 * 
 * Registers eligible buttons from a configuration file.
 * 
 * Tests holding items for button eligibility based on button configuration rules
 * 
 * Provides access to CatalogHoldings, JSON button data, and HTML buttons
 * 
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 * @author Michael Nichols <mnichols@tamu.edu>
 *
 */

@Service
public class GetItForMeService {
	@Autowired
	private CatalogServiceFactory catalogServiceFactory;
	
	@Value("${buttonsPackage}")
	private String buttonsPackage;
	
	@Value("${activeButtons}")
	private String[] activeButtons;
	
	@Autowired
	Environment environment;
	
	private List<GetItForMeButton> registeredButtons = new ArrayList<GetItForMeButton>();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Get a list of CatalogHolding as provided by the CatalogServiceFactory
	 * 
	 * @param catalogName
	 * @param bibId
	 * @return List<CatalogHolding>
	 */
	public List<CatalogHolding> getHoldingsByBibId(String catalogName, String bibId) {
		return catalogServiceFactory.getOrCreateCatalogService(catalogName).getHoldingsByBibId(bibId);
	}
	
	/**
	 * Registers and instantiates GetItForMe button implementations on app startup based on the button configuration file 
	 * 
	 */
	@PostConstruct
	private void registerButtons() {
		for (String activeButton:activeButtons) {
			try {
				String rawLocationCodes = environment.getProperty(activeButton+".locationCodes");
				String[] itemTypeCodes = environment.getProperty(activeButton+".itemTypeCodes",String[].class);
				Integer[] itemStatusCodes = environment.getProperty(activeButton+".itemStatusCodes",Integer[].class);
				String linkText = environment.getProperty(activeButton+".linkText");
				String SID = environment.getProperty(activeButton+".SID");
				
				GetItForMeButton c = (GetItForMeButton) Class.forName(buttonsPackage+"."+activeButton).newInstance();
				if (rawLocationCodes != null) {
					c.setLocationCodes(rawLocationCodes.split(";"));
				}
				if (itemTypeCodes != null) {
					c.setItemTypeCodes(itemTypeCodes);
				}
				if (itemStatusCodes != null) {
					c.setItemStatusCodes(itemStatusCodes);
				}
				if (linkText != null) {
					c.setLinkText(linkText);
				}
				if (SID != null) {
					c.setSID(SID);
				}
				
				this.registeredButtons.add(c);
			} catch (InstantiationException e) {
				logger.error("Tried to instantiate an instance of "+activeButton, e);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				logger.error("Tried to access something on a "+activeButton, e);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				logger.error("Tried to access something on a "+activeButton, e);
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				logger.error("Couldn't find class: "+activeButton, e);
				e.printStackTrace();
			}
		}
	}
	
	private List<GetItForMeButton> getRegisteredButtons() {
		return this.registeredButtons;
	}
	
	/**
	 * Gets a list of CatalogHolding by bibId, runs registered button test eligibility for all the items in that holding,
	 * 	and builds and returns the resulting button data, keyed by the holding's MFHD. 
	 * 
	 * @param catalogName
	 * @param bibId
	 * @return Map<String,List<Map<String,String>>>
	 */
	
	public Map<String,List<Map<String,String>>> getButtonsByBibId(String catalogName,String bibId) {
		List<CatalogHolding> catalogHoldings = this.getHoldingsByBibId(catalogName,bibId);
		if (catalogHoldings != null) {
			logger.debug("\n\nCATALOG HOLDINGS FOR "+bibId);
			
			Map<String,List<Map<String,String>>> validButtons = new HashMap<String,List<Map<String,String>>>();
			
			//check each holding
			catalogHoldings.forEach(holding -> {
				logger.debug("MARC Record Leader: "+holding.getMarcRecordLeader());
				//TODO: if configured, check for single item monograph
				//button.checkRecordType(marcRecord)
				
				//we'll put the valid buttons here, keyed by their parent holding's MFHD
				//we want the MFHD key, even if we don't have any valid buttons for it, so users can see that the MFHD was tested
				validButtons.put(holding.getMfhd(), new ArrayList<Map<String,String>>());

				//check the all the items for each holding
				holding.getCatalogItems().forEach((uri,itemData) -> {
					logger.debug("Checking holding URI: "+uri);
					//check all registered button for each item
					for (GetItForMeButton button:this.getRegisteredButtons()) {
						logger.debug("Analyzing: "+button.toString());
	
						logger.debug("Location: "+itemData.get("permLocationCode")+": "+button.fitsLocation(itemData.get("permLocationCode")));
						logger.debug("TypeDesc: "+itemData.get("typeDesc")+": "+button.fitsItemType(itemData.get("typeDesc")));
						logger.debug("Status: "+itemData.get("itemStatusCode")+": "+button.fitsItemStatus(Integer.parseInt(itemData.get("itemStatusCode"))));
						
						//test the current item against the current GetItForMe button's requirements for eligibility
						if (button.fitsLocation(itemData.get("permLocationCode")) && button.fitsItemType(itemData.get("typeDesc")) && button.fitsItemStatus(Integer.parseInt(itemData.get("itemStatusCode")))) {
							//used to build the button's link from the template parameter keys it provides
							List<String> parameterKeys = button.getTemplateParameterKeys();
							Map<String,String> parameters = new HashMap<String,String>();
							
							for (String parameterKey:parameterKeys) {
								parameters.put(parameterKey,itemData.get(parameterKey));
							}
							//these template parameter keys are a special case, and come from the parent holding, rather than the item data itself
							String[] getParameterFromHolding = {"isbn","title","author","publisher","place","year"};
							for (String parameterName:getParameterFromHolding) {
								if (parameters.containsKey(parameterName)) {
									try {
										parameters.put(parameterName, holding.getValueByPropertyName(parameterName));
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}

							logger.debug("We want the button with text: "+button.getLinkText());
							logger.debug("It looks like: ");
							logger.debug(button.getLinkTemplate(parameters));

							//generate the button data
							Map<String,String> buttonContent = new HashMap<String,String>();
							//for multi-volume holdings, enrich the linkText to indicate which volume the button represents 
							if (holding.isMultiVolume()) {
								logger.debug("Generating a multi volume button");
								buttonContent.put("linkText",button.getLinkText()+" | "+itemData.get("enumeration")+" "+itemData.get("chron"));
							} else {
								logger.debug("Generating a single item button");
								buttonContent.put("linkText",button.getLinkText());
							}
							buttonContent.put("linkHref",button.getLinkTemplate(parameters));
							buttonContent.put("cssClasses", button.getCssClasses());
							//add the button to the list for the holding's MFHD
							validButtons.get(holding.getMfhd()).add(buttonContent);
						} else {
							logger.debug("We should skip the button with text: "+button.getLinkText());
						}
					}
				});
			});
			return validButtons;
		}
		return null;
	}
}
