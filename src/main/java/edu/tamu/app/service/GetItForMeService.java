package edu.tamu.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.tamu.app.model.BorrowItNowButton;
import edu.tamu.app.model.CatalogHolding;
import edu.tamu.app.model.CushingButton;
import edu.tamu.app.model.GetIt1WeekButton;
import edu.tamu.app.model.GetIt2DaysButton;
import edu.tamu.app.model.GetIt2DaysDocDelButton;
import edu.tamu.app.model.GetIt4DaysButton;
import edu.tamu.app.model.GetItForMeButton;
import edu.tamu.app.model.RecallItButton;

@Service
public class GetItForMeService {
	@Autowired
	private CatalogServiceFactory catalogServiceFactory;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	
	public List<CatalogHolding> getHoldingsByBibId(String catalogName, String bibId) {
		return catalogServiceFactory.getOrCreateCatalogService(catalogName).getHoldingsByBibId(bibId);
	}
	
	public Map<String,List<Map<String,String>>> getButtonsByBibId(String catalogName,String bibId) {
		List<CatalogHolding> catalogHoldings = this.getHoldingsByBibId(catalogName,bibId);
		if (catalogHoldings != null) {
			logger.debug("\n\nCATALOG HOLDINGS FOR "+bibId);
			
			List<GetItForMeButton> eligibleButtons = new ArrayList<GetItForMeButton>();
			Map<String,List<Map<String,String>>> validButtons = new HashMap<String,List<Map<String,String>>>();
			
			eligibleButtons.add(new CushingButton());
			eligibleButtons.add(new GetIt2DaysButton());
			eligibleButtons.add(new GetIt4DaysButton());
			eligibleButtons.add(new GetIt1WeekButton());
			eligibleButtons.add(new RecallItButton());
			eligibleButtons.add(new GetIt2DaysDocDelButton());
			eligibleButtons.add(new BorrowItNowButton());
			
			catalogHoldings.forEach(holding -> {
				logger.debug("MARC Record Leader: "+holding.getMarcRecordLeader());
				//todo: if configured, check for single item monograph
				//button.checkRecordType(marcRecord)
				validButtons.put(holding.getMfhd(), new ArrayList<Map<String,String>>());
				holding.getCatalogItems().forEach((uri,items) -> {
					logger.debug("Checking holding URI: "+uri);
					for (GetItForMeButton button:eligibleButtons) {
						logger.debug("Analyzing: "+button.toString());
	
						logger.debug("Location: "+items.get("permLocationCode")+": "+button.checkLocation(items.get("permLocationCode")));
						logger.debug("TypeDesc: "+items.get("typeDesc")+": "+button.checkItemType(items.get("typeDesc")));
						logger.debug("Status: "+items.get("itemStatusCode")+": "+button.checkItemStatus(Integer.parseInt(items.get("itemStatusCode"))));
						List<String> parameterKeys = button.getTemplateParameterKeys();
						Map<String,String> parameters = new HashMap<String,String>();
						for (String parameterKey:parameterKeys) {
							parameters.put(parameterKey,items.get(parameterKey));
						}
						//ToDo ISBN will need to either be passed in as an argument or found through another API
						if (parameters.containsKey("isbn")) {
							parameters.put("isbn", "placeHolderValue");
						}
						if (button.checkLocation(items.get("permLocationCode")) && button.checkItemType(items.get("typeDesc")) && button.checkItemStatus(Integer.parseInt(items.get("itemStatusCode")))) {
							logger.debug("We want the button with text: "+button.getLinkText());
							logger.debug("It looks like: ");
							logger.debug(button.getLinkTemplate(parameters));
							Map<String,String> buttonContent = new HashMap<String,String>();
							buttonContent.put("linkText",button.getLinkText());
							buttonContent.put("linkHref",button.getLinkTemplate(parameters));
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
