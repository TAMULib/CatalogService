package edu.tamu.catalog.controller;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.catalog.domain.model.FeesFines;
import edu.tamu.catalog.exception.NotFoundException;
import edu.tamu.catalog.service.CatalogService;
import edu.tamu.catalog.service.CatalogServiceFactory;

@RestController
@RequestMapping("/patron")
public class PatronController {

    @Autowired
    private CatalogServiceFactory catalogServiceFactory;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Provides data for all fees and fines associated with a patron.
     *
     * @param String catalogName (optional)
     * @param String user
     * @return
     * @throws ParseException
     */
    @GetMapping("/{uin}/fines")
    public @ResponseBody ResponseEntity<FeesFines> fines(@RequestParam(value="catalogName", defaultValue="folio") String catalogName, @PathVariable String uin) throws ParseException {
        FeesFines feesFines = getCatalogServiceByName(catalogName).getFeesFines(uin);

        if (feesFines == null) {
            logger.debug("Requested patron was not found in the " + catalogName + " catalog.");
            throw new NotFoundException();
        }

        return new ResponseEntity<>(feesFines, HttpStatus.OK);
    }

    private CatalogService getCatalogServiceByName(String catalogName) {
        return catalogServiceFactory.getOrCreateCatalogService(catalogName);
    }
}
