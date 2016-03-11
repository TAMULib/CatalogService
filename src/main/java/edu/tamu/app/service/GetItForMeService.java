package edu.tamu.app.service;

import java.util.ArrayList;
import java.util.Collections;
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
import edu.tamu.app.utilities.sort.VolumeComparator;


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

	
	public List<CatalogHolding> getHoldingsByBibId(String catalogName, String bibId) {
		return catalogServiceFactory.getOrCreateCatalogService(catalogName).getHoldingsByBibId(bibId);
	}
	
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
	
	public Map<String,List<Map<String,String>>> getButtonsByBibId(String catalogName,String bibId) {
		List<CatalogHolding> catalogHoldings = this.getHoldingsByBibId(catalogName,bibId);
		if (catalogHoldings != null) {
			logger.debug("\n\nCATALOG HOLDINGS FOR "+bibId);
			
			Map<String,List<Map<String,String>>> validButtons = new HashMap<String,List<Map<String,String>>>();
			
			catalogHoldings.forEach(holding -> {
				logger.debug("MARC Record Leader: "+holding.getMarcRecordLeader());
				//TODO: if configured, check for single item monograph
				//button.checkRecordType(marcRecord)
				validButtons.put(holding.getMfhd(), new ArrayList<Map<String,String>>());
				holding.getCatalogItems().forEach((uri,itemData) -> {
					logger.debug("Checking holding URI: "+uri);
					for (GetItForMeButton button:this.getRegisteredButtons()) {
						logger.debug("Analyzing: "+button.toString());
	
						logger.debug("Location: "+itemData.get("permLocationCode")+": "+button.fitsLocation(itemData.get("permLocationCode")));
						logger.debug("TypeDesc: "+itemData.get("typeDesc")+": "+button.fitsItemType(itemData.get("typeDesc")));
						logger.debug("Status: "+itemData.get("itemStatusCode")+": "+button.fitsItemStatus(Integer.parseInt(itemData.get("itemStatusCode"))));
						List<String> parameterKeys = button.getTemplateParameterKeys();
						Map<String,String> parameters = new HashMap<String,String>();
						
						for (String parameterKey:parameterKeys) {
							parameters.put(parameterKey,itemData.get(parameterKey));
						}
						
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
						
						if (button.fitsLocation(itemData.get("permLocationCode")) && button.fitsItemType(itemData.get("typeDesc")) && button.fitsItemStatus(Integer.parseInt(itemData.get("itemStatusCode")))) {
							logger.debug("We want the button with text: "+button.getLinkText());
							logger.debug("It looks like: ");
							logger.debug(button.getLinkTemplate(parameters));
							Map<String,String> buttonContent = new HashMap<String,String>();
							//multi-volume is a special case, we need to enrich the default button text with item level details
							if (holding.isMultiVolume()) {
								logger.debug("Generating a multi volume button");
								buttonContent.put("linkText",itemData.get("enumeration")+" "+itemData.get("chron")+" | "+button.getLinkText());
								//TODO find out how a multi-volume link should be represented.
								buttonContent.put("linkHref",button.getLinkTemplate(parameters));
							} else {
								logger.debug("Generating a single item button");
								buttonContent.put("linkText",button.getLinkText());
								buttonContent.put("linkHref",button.getLinkTemplate(parameters));
							}
							buttonContent.put("cssClasses", button.getCssClasses());
							validButtons.get(holding.getMfhd()).add(buttonContent);
						} else {
							logger.debug("We should skip the button with text: "+button.getLinkText());
						}
					}
					//for multi-volumes, get the items somewhat ordered by volume (there's no real definition of the order to work from)
					if (holding.isMultiVolume()) {
						Collections.sort(validButtons.get(holding.getMfhd()), new VolumeComparator());
					}
				});
			});
			return validButtons;
		}
		return null;
	}
}
