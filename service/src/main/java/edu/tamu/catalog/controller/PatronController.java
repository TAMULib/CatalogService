package edu.tamu.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.catalog.annotation.DefaultCatalog;
import edu.tamu.catalog.domain.model.FeesFines;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.service.CatalogService;

@RestController
@RequestMapping("/patron")
public class PatronController {

    /**
     * Provides data for all fees and fines associated with a patron.
     *
     * @param CatalogService catalogService (resolved by query parameter catalogName)
     * @param String uin
     * @return
     * @throws Exception
     */
    @GetMapping("/{uin}/fines")
    public @ResponseBody ResponseEntity<FeesFines> fines(
        @DefaultCatalog("folio") CatalogService catalogService,
        @PathVariable(required = true) String uin
    ) throws Exception {
        FeesFines feesFines = catalogService.getFeesFines(uin);

        return ResponseEntity.status(HttpStatus.OK)
            .body(feesFines);
    }

    /**
     * Provides data for all loan items associated with a patron.
     *
     * @param CatalogService catalogService (resolved by query parameter catalogName)
     * @param String uin
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
     * Renews a single loan item associated with a patron.
     *
     * @param String catalogName (optional)
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

}
