package edu.tamu.app.controller;

import static edu.tamu.weaver.response.ApiStatus.ERROR;
import static edu.tamu.weaver.response.ApiStatus.SUCCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.app.service.GetItForMeService;
import edu.tamu.weaver.response.ApiResponse;

@RestController
@RequestMapping("/catalog-access")
public class CatalogAccessController {
    @Autowired
    GetItForMeService getItForMeService;

    /**
     * Provides the raw CatalogHolding data
     *
     * @param catalogName
     * @param bibId
     * @return
     * @throws JsonProcessingException
     * @throws IOException
     */

	@RequestMapping("/get-holdings")
	public ApiResponse getHoldings(@RequestParam(value="catalogName",defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId) throws JsonProcessingException, IOException {
		return new ApiResponse(SUCCESS,getItForMeService.getHoldingsByBibId(catalogName,bibId));
	}

	/**
	 * Provides fully formatted HTML buttons, keyed by item MFHD
	 * @param catalogName
	 * @param bibId
	 * @param returnType
	 * @return
	 */
	@RequestMapping("/get-html-buttons")
	public ApiResponse getHtmlButtonsByBibId(@RequestParam(value="catalogName",defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId, @RequestParam(value="returnType",defaultValue="html") String returnType) {
		Map<String,List<Map<String,String>>> buttonData = getItForMeService.getButtonsByBibId(catalogName, bibId);
		if (buttonData != null) {
			Map<String,List<String>> buttonContents = new HashMap<String,List<String>>();
			for (Map.Entry<String, List<Map<String,String>>> entry : buttonData.entrySet()) {
			    buttonContents.put(entry.getKey(),new ArrayList<String>());
			    Iterator<Map<String,String>> buttonPropIterator = entry.getValue().iterator();
			    while (buttonPropIterator.hasNext()) {
				    String html = "<a target=\"_blank\" class=\"{cssClasses}\" href=\"http://{linkHref}\">{linkText}</a>";
			    	Map<String,String> buttonProperties = (Map<String, String>) buttonPropIterator.next();

			    	Iterator<String> propKeysIterator = buttonProperties.keySet().iterator();
			    	while (propKeysIterator.hasNext()) {
			    		String propKey = (String) propKeysIterator.next();
			    		html = html.replace("{"+propKey+"}",buttonProperties.get(propKey));
			    	}
			    	buttonContents.get(entry.getKey()).add(html);

			    }
			}
			return new ApiResponse(SUCCESS,buttonContents);
		}
		return new ApiResponse(ERROR,"Catalog or Holding not found");
	}

	/**
	 * Provides the raw button data in JSON format, keyed by MFHD.
	 * @param catalogName
	 * @param bibId
	 * @param returnType
	 * @return
	 */

	@RequestMapping("/get-buttons")
	public ApiResponse getButtonsByBibId(@RequestParam(value="catalogName",defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId, @RequestParam(value="returnType",defaultValue="html") String returnType) {
		Map<String,List<Map<String,String>>> buttonData = getItForMeService.getButtonsByBibId(catalogName, bibId);
		if (buttonData != null) {
			return new ApiResponse(SUCCESS,buttonData);
		}
		return new ApiResponse(ERROR,"Catalog or Holding not found");
	}

	@RequestMapping("/get-button-config")
	public ApiResponse getButtonConfiguration() {
	    return new ApiResponse(SUCCESS,"Current Button Configuration",getItForMeService.getRegisteredButtons());
	}
}
