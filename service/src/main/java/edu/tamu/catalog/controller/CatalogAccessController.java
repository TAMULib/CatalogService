package edu.tamu.catalog.controller;

import static edu.tamu.weaver.response.ApiStatus.ERROR;
import static edu.tamu.weaver.response.ApiStatus.SUCCESS;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.catalog.annotation.DefaultCatalog;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.service.CatalogService;
import edu.tamu.weaver.response.ApiResponse;

@RestController
@RequestMapping("/catalog-access")
public class CatalogAccessController {

    /**
     * Provides the raw CatalogHolding data
     *
     * @param CatalogService catalogService (injected)
     * @param String bibId
     * @return
     * @throws JsonProcessingException
     * @throws IOException
     */
    @RequestMapping("/get-holdings")
    public ApiResponse getHoldings(@DefaultCatalog("evans") CatalogService catalogService, @RequestParam(required = true) String bibId) {
        List<HoldingsRecord> catalogHoldings = catalogService.getHoldingsByBibId(bibId);
        if (catalogHoldings != null) {
            return new ApiResponse(SUCCESS, catalogHoldings);
        } else {
            return new ApiResponse(ERROR, "Error retrieving holdings from " + catalogService.getName() + " catalog");
        }
    }

    /**
     * Provides data for a single CatalogHolding
     *
     * @param CatalogService catalogService (injected)
     * @param String bibId
     * @param String holdingId
     * @return
     * @throws JsonProcessingException
     * @throws IOException
     */
    @RequestMapping("/get-holding")
    public ApiResponse getHolding(@DefaultCatalog("evans") CatalogService catalogService, @RequestParam(required = true) String bibId, @RequestParam(required = true) String holdingId) {
        HoldingsRecord catalogHolding = catalogService.getHolding(bibId, holdingId);
        if (catalogHolding != null) {
            return new ApiResponse(SUCCESS, catalogHolding);
        } else {
            return new ApiResponse(ERROR, "Error retrieving holding from " + catalogService.getName() + " catalog");
        }
    }

}
