package edu.tamu.catalog.controller;

import static edu.tamu.weaver.response.ApiStatus.ERROR;
import static edu.tamu.weaver.response.ApiStatus.SUCCESS;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.service.CatalogService;
import edu.tamu.catalog.service.CatalogServiceFactory;
import edu.tamu.weaver.response.ApiResponse;

@RestController
@RequestMapping("/catalog-access")
public class CatalogAccessController {

    @Autowired
    private CatalogServiceFactory catalogServiceFactory;

    /**
     * Provides the raw CatalogHolding data
     *
     * @param String catalogName (optional)
     * @param String bibId
     * @return
     * @throws JsonProcessingException
     * @throws IOException
     */
	@RequestMapping("/get-holdings")
	public ApiResponse getHoldings(@RequestParam(value="catalogName", defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId) {
	    List<HoldingsRecord> catalogHoldings = getCatalogServiceByName(catalogName).getHoldingsByBibId(bibId);
        if (catalogHoldings != null) {
            return new ApiResponse(SUCCESS, catalogHoldings);
        } else {
            return new ApiResponse(ERROR,"Error retrieving holdings from " + catalogName + "catalog");
        }
	}

    /**
     * Provides data for a single CatalogHolding
     *
     * @param String catalogName (optional)
     * @param String bibId
     * @param String holdingId
     * @return
     * @throws JsonProcessingException
     * @throws IOException
     */
    @RequestMapping("/get-holding")
    public ApiResponse getHolding(@RequestParam(value="catalogName", defaultValue="evans") String catalogName, @RequestParam("bibId") String bibId, @RequestParam("holdingId") String holdingId) {
        HoldingsRecord catalogHolding = getCatalogServiceByName(catalogName).getHolding(bibId, holdingId);
        if (catalogHolding != null) {
            return new ApiResponse(SUCCESS, catalogHolding);
        } else {
            return new ApiResponse(ERROR,"Error retrieving holding from " + catalogName + "catalog");
        }
    }

    /**
     * Provides data for all loan items associated with a patron.
     *
     * @param String catalogName (optional)
     * @param String user
     * @return
     */
    @GetMapping("/get-loan-items")
    public ApiResponse getItems(@RequestParam(value="catalogName", defaultValue="folio") String catalogName, String uin) {

        List<LoanItem> loanItems = getCatalogServiceByName(catalogName).getLoanItems(uin);
        if (loanItems != null) {
            return new ApiResponse(SUCCESS, loanItems);

        } else {
            return new ApiResponse(ERROR, "Error retrieving loan items from " + catalogName + " catalog");
        }
    }

    private CatalogService getCatalogServiceByName(String catalogName) {
      return catalogServiceFactory.getOrCreateCatalogService(catalogName);
  }
}
