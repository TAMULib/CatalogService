package edu.tamu.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.catalog.annotation.DefaultCatalog;
import edu.tamu.catalog.domain.model.FeeFine;
import edu.tamu.catalog.domain.model.HoldRequest;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.service.CatalogService;

@RestController
@RequestMapping("/patron")
public class PatronController {

    /**
     * Provides data for all fees and fines associated with a patron.
     *
     * @param CatalogService catalogService (resolved by query parameter catalogName).
     * @param String uin
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/{uin}/fines")
    public @ResponseBody ResponseEntity<List<FeeFine>> getFeesFines(
        @DefaultCatalog("folio") CatalogService catalogService,
        @PathVariable(required = true) String uin
    ) throws Exception {
        List<FeeFine> feesFines = catalogService.getFeesFines(uin);

        return ResponseEntity.status(HttpStatus.OK)
            .body(feesFines);
    }

    /**
     * Provides data for all loan items associated with a patron.
     *
     * @param CatalogService catalogService (resolved by query parameter catalogName).
     * @param String uin
     *
     * @return
     */
    @GetMapping("/{uin}/loans")
    public @ResponseBody ResponseEntity<List<LoanItem>> getLoanItems(
        @DefaultCatalog("folio") CatalogService catalogService,
        @PathVariable(required = true) String uin
    ) throws Exception {
        List<LoanItem> loanItems = catalogService.getLoanItems(uin);

        return ResponseEntity.status(HttpStatus.OK)
            .body(loanItems);
    }

    /**
     * Provides data for all loan items associated with a patron.
     *
     * @param CatalogService catalogService (resolved by query parameter catalogName).
     * @param String uin
     *
     * @return
     */
    @GetMapping("/{uin}/holds")
    public @ResponseBody ResponseEntity<List<HoldRequest>> getHoldRequests(
        @DefaultCatalog("folio") CatalogService catalogService,
        @PathVariable(required = true) String uin
    ) throws Exception {
        List<HoldRequest> holdRequests = catalogService.getHoldRequests(uin);

        return ResponseEntity.status(HttpStatus.OK)
            .body(holdRequests);
    }

    /**
     * Cancels a hold request, returning the updated hold request.
     *
     * @param String catalogName (optional)
     * @param String uin
     * @param String requestId
     * @return
     * @throws Exception
     */
    @PostMapping("/{uin}/holds/{requestId}/cancel")
    public @ResponseBody ResponseEntity<?> cancelHoldRequest(
        @DefaultCatalog("folio") CatalogService catalogService,
        @PathVariable(required = true) String uin,
        @PathVariable(required = true) String requestId
    ) throws Exception {
        catalogService.cancelHoldRequest(uin, requestId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Renews a single loan item associated with a patron.
     *
     * @param CatalogService catalogService (resolved by query parameter catalogName)
     * @param String uin
     * @param String itemId
     * @return
     */
    @PostMapping("/{uin}/renew/{itemId}")
    public @ResponseBody ResponseEntity<LoanItem> renewItem(
        @DefaultCatalog("folio") CatalogService catalogService,
        @PathVariable(required = true) String uin,
        @PathVariable(required = true) String itemId
    ) throws Exception {
        LoanItem loanItem = catalogService.renewItem(uin, itemId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(loanItem);
    }

    /**
     * Checks block status for a patron.
     *
     * @param CatalogService catalogService (resolved by query parameter catalogName)
     * @param String uin
     * @return
     */
    @GetMapping("/{uin}/block")
    public @ResponseBody ResponseEntity<Boolean> getBlockStatus(
        @DefaultCatalog("folio") CatalogService catalogService,
        @PathVariable(required = true) String uin
    ) throws Exception {
        Boolean blockStatus = catalogService.getBlockStatus(uin);

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.TEXT_PLAIN)
            .body(blockStatus);
    }

}
