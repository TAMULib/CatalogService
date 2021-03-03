package edu.tamu.catalog.controller;

import static edu.tamu.weaver.response.ApiStatus.ERROR;
import static edu.tamu.weaver.response.ApiStatus.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.service.FolioCatalogService;
import edu.tamu.weaver.response.ApiResponse;

@RunWith(SpringRunner.class)
public class CatalogAccessControllerTest {

    @Mock
    private FolioCatalogService folioCatalogService;

    private CatalogAccessController catalogAccessController;

    @Before
    public void setup() {
        folioCatalogService = mock(FolioCatalogService.class);
        catalogAccessController = new CatalogAccessController();
    }

    @Test
    public void testGetHoldingsSuccess() {
        when(folioCatalogService.getHoldingsByBibId(any(String.class))).thenReturn(new ArrayList<HoldingsRecord>());

        ApiResponse response = catalogAccessController.getHoldings(folioCatalogService, "foo");
        assertEquals("Did not receive expected successful response", SUCCESS, response.getMeta().getStatus());
    }

    @Test
    public void testGetHoldingsFailure() {
        when(folioCatalogService.getHoldingsByBibId(any(String.class))).thenReturn(null);

        ApiResponse response = catalogAccessController.getHoldings(folioCatalogService, "foo");
        assertEquals("Did not receive expected error response", ERROR, response.getMeta().getStatus());
    }

    @Test
    public void testGetHoldingSuccess() {
        Map<String, Map<String, String>> catalogItems = new HashMap<>();
        HoldingsRecord holding = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title", "author", "publisher", "place", "year", "genre", "edition", "fallBackLocationCode", "oclc", "recordId", "callNumber", catalogItems);

        when(folioCatalogService.getHolding(any(String.class), any(String.class))).thenReturn(holding);

        ApiResponse response = catalogAccessController.getHolding(folioCatalogService, "foo", "bar");
        assertEquals("Did not receive expected successful response", SUCCESS, response.getMeta().getStatus());
    }

    @Test
    public void testGetHoldingFailure() {
        when(folioCatalogService.getHolding(any(String.class), any(String.class))).thenReturn(null);

        ApiResponse response = catalogAccessController.getHolding(folioCatalogService, "foo", "bar");
        assertEquals("Did not receive expected error response", ERROR, response.getMeta().getStatus());
    }

}
