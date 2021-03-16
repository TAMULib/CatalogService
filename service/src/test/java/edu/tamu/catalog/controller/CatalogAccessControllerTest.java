package edu.tamu.catalog.controller;

import static edu.tamu.weaver.response.ApiStatus.ERROR;
import static edu.tamu.weaver.response.ApiStatus.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.service.FolioCatalogService;
import edu.tamu.weaver.response.ApiResponse;

@ExtendWith(SpringExtension.class)
public class CatalogAccessControllerTest {

    @Mock
    private FolioCatalogService folioCatalogService;

    private CatalogAccessController catalogAccessController;

    @BeforeEach
    public void setup() {
        folioCatalogService = mock(FolioCatalogService.class);
        catalogAccessController = new CatalogAccessController();
    }

    @Test
    public void testGetHoldingsSuccess() {
        when(folioCatalogService.getHoldingsByBibId(any(String.class))).thenReturn(new ArrayList<HoldingsRecord>());

        ApiResponse response = catalogAccessController.getHoldings(folioCatalogService, "foo");
        assertEquals(SUCCESS, response.getMeta().getStatus(), "Did not receive expected successful response");
    }

    @Test
    public void testGetHoldingsFailure() {
        when(folioCatalogService.getHoldingsByBibId(any(String.class))).thenReturn(null);

        ApiResponse response = catalogAccessController.getHoldings(folioCatalogService, "foo");
        assertEquals(ERROR, response.getMeta().getStatus(), "Did not receive expected error response");
    }

    @Test
    public void testGetHoldingSuccess() {
        Map<String, Map<String, String>> catalogItems = new HashMap<>();
        HoldingsRecord holding = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title", "author", "publisher", "place", "year", "genre", "edition", "fallBackLocationCode", "oclc", "recordId", "callNumber", catalogItems);

        when(folioCatalogService.getHolding(any(String.class), any(String.class))).thenReturn(holding);

        ApiResponse response = catalogAccessController.getHolding(folioCatalogService, "foo", "bar");
        assertEquals(SUCCESS, response.getMeta().getStatus(), "Did not receive expected successful response");
    }

    @Test
    public void testGetHoldingFailure() {
        when(folioCatalogService.getHolding(any(String.class), any(String.class))).thenReturn(null);

        ApiResponse response = catalogAccessController.getHolding(folioCatalogService, "foo", "bar");
        assertEquals(ERROR, response.getMeta().getStatus(), "Did not receive expected error response");
    }

}
