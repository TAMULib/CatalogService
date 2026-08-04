package edu.tamu.catalog.controller;

import static edu.tamu.weaver.response.ApiStatus.ERROR;
import static edu.tamu.weaver.response.ApiStatus.SUCCESS;

import edu.tamu.catalog.annotation.DefaultCatalog;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.service.CatalogService;
import edu.tamu.weaver.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog-access")
public class CatalogAccessController {

    /**
     * Provides the raw CatalogHolding data
     *
     * @param catalogService (resolved by query parameter catalogName).
     * @param bibId The Bibliographic ID.
     *
     * @return The API Response.
     *
     * @throws Exception
     */
    @RequestMapping("/get-holdings")
    public ApiResponse getHoldings(
        @DefaultCatalog("evans") CatalogService catalogService,
        @RequestParam(required = true) String bibId
    ) throws Exception {
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
     * @param catalogService (resolved by query parameter catalogName)
     * @param bibId The Bibliographic ID.
     * @param holdingId The Holdings ID.
     *
     * @return The API Response.
     *
     * @throws Exception
     */
    @RequestMapping("/get-holding")
    public ApiResponse getHolding(
        @DefaultCatalog("evans") CatalogService catalogService,
        @RequestParam(required = true) String bibId,
        @RequestParam(required = true) String holdingId
    ) throws Exception {
        HoldingsRecord catalogHolding = catalogService.getHolding(bibId, holdingId);
        if (catalogHolding != null) {
            return new ApiResponse(SUCCESS, catalogHolding);
        } else {
            return new ApiResponse(ERROR, "Error retrieving holding from " + catalogService.getName() + " catalog");
        }
    }

}
