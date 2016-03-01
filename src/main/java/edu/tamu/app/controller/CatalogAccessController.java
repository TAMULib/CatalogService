package edu.tamu.app.controller;

import static edu.tamu.framework.enums.ApiResponseType.ERROR;
import static edu.tamu.framework.enums.ApiResponseType.SUCCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.app.service.GetItForMeService;
import edu.tamu.framework.aspect.annotation.ApiMapping;
import edu.tamu.framework.aspect.annotation.SkipAop;
import edu.tamu.framework.model.ApiResponse;

@Controller
@ApiMapping("/catalog-access")
public class CatalogAccessController {
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    GetItForMeService getItForMeService;

	@ApiMapping("/get-holdings")
	@SkipAop
	public ApiResponse getHoldings(@RequestParam(value="catalogName",defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId) throws JsonProcessingException, IOException {
		return new ApiResponse(SUCCESS,getItForMeService.getHoldingsByBibId(catalogName,bibId));
	}

	@ApiMapping("/get-html-buttons")
	@SkipAop
	public ApiResponse getHtmlButtonsByBibId(@RequestParam(value="catalogName",defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId, @RequestParam(value="returnType",defaultValue="html") String returnType) {
		Map<String,List<Map<String,String>>> buttonData = getItForMeService.getButtonsByBibId(catalogName, bibId);
		if (buttonData != null) {
			Map<String,List<String>> buttonContents = new HashMap<String,List<String>>();
			for (Map.Entry<String, List<Map<String,String>>> entry : buttonData.entrySet()) {
			    buttonContents.put(entry.getKey(),new ArrayList<String>());
			    Iterator<Map<String,String>> buttonPropIterator = entry.getValue().iterator();
			    while (buttonPropIterator.hasNext()) {
				    String html = "<a target=\"_blank\" class=\"button-gifm\" href=\"http://{linkHref}\">{linkText}</a>";
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

	@ApiMapping("/get-buttons")
	@SkipAop
	public ApiResponse getButtonsByBibId(@RequestParam(value="catalogName",defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId, @RequestParam(value="returnType",defaultValue="html") String returnType) {
		Map<String,List<Map<String,String>>> buttonData = getItForMeService.getButtonsByBibId(catalogName, bibId);
		if (buttonData != null) {
			return new ApiResponse(SUCCESS,buttonData);
		}
		return new ApiResponse(ERROR,"Catalog or Holding not found");
	}
}
