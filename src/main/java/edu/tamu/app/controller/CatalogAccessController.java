package edu.tamu.app.controller;

import static edu.tamu.weaver.response.ApiStatus.SUCCESS;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.app.service.CatalogService;
import edu.tamu.app.service.CatalogServiceFactory;
import edu.tamu.weaver.response.ApiResponse;

@RestController
@RequestMapping("/catalog-access")
public class CatalogAccessController {

    @Autowired
    private CatalogServiceFactory catalogServiceFactory;

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
	public ApiResponse getHoldings(@RequestParam(value="catalogName", defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId) throws JsonProcessingException, IOException {
		return new ApiResponse(SUCCESS, getCatalogServiceByName(catalogName).getHoldingsByBibId(bibId));
	}

    private CatalogService getCatalogServiceByName(String catalogName) {
      return catalogServiceFactory.getOrCreateCatalogService(catalogName);
  }
}
