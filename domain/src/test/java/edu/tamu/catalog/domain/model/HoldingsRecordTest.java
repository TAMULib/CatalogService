package edu.tamu.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class HoldingsRecordTest {

    @Test
    public void testCreateAllArgs() {
        final Map<String, Map<String, String>> catalogItems = new HashMap<>();
        final Map<String, String> catalogItem = new HashMap<>();
        catalogItem.put("key", "value");
        catalogItems.put("item", catalogItem);

        final HoldingsRecord holdingsRecord = new HoldingsRecord("marcRecordLeader", "mfhd", "issn",
            "isbn", "title", "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition",
            "oclc", "recordId", "callNumber", "callNumberPrefix", "holdingLocation", new ArrayList<Note>(), new ArrayList<String>(), false, catalogItems);

        assertNotNull(holdingsRecord);
        assertNotNull(holdingsRecord.getCatalogItems());

        assertEquals("recordId", holdingsRecord.getRecordId());
        assertEquals("marcRecordLeader", holdingsRecord.getMarcRecordLeader());
        assertEquals("mfhd", holdingsRecord.getMfhd());
        assertEquals("issn", holdingsRecord.getIssn());
        assertEquals("isbn", holdingsRecord.getIsbn());
        assertEquals("title", holdingsRecord.getTitle());
        assertEquals("author", holdingsRecord.getAuthor());
        assertEquals("publisher", holdingsRecord.getPublisher());
        assertEquals("place", holdingsRecord.getPlace());
        assertEquals("year", holdingsRecord.getYear());
        assertEquals("genre", holdingsRecord.getGenre());
        assertEquals("fallbackLocationCode", holdingsRecord.getFallbackLocationCode());
        assertEquals("edition", holdingsRecord.getEdition());
        assertEquals("oclc", holdingsRecord.getOclc());
        assertEquals("callNumber", holdingsRecord.getCallNumber());
        assertEquals("callNumberPrefix", holdingsRecord.getCallNumberPrefix());
        assertEquals("holdingLocation", holdingsRecord.getHoldingLocation());
        assertEquals(false, holdingsRecord.isLargeVolume());
        assertEquals(1, holdingsRecord.getCatalogItems().size());

        assertFalse(holdingsRecord.isMultiVolume());
    }

    @Test
    public void testCreateNoArgs() {
        final HoldingsRecord holdingsRecord = new HoldingsRecord();

        assertNotNull(holdingsRecord);

        assertNull(holdingsRecord.getRecordId());
        assertNull(holdingsRecord.getMarcRecordLeader());
        assertNull(holdingsRecord.getMfhd());
        assertNull(holdingsRecord.getIssn());
        assertNull(holdingsRecord.getIsbn());
        assertNull(holdingsRecord.getTitle());
        assertNull(holdingsRecord.getAuthor());
        assertNull(holdingsRecord.getPublisher());
        assertNull(holdingsRecord.getPlace());
        assertNull(holdingsRecord.getYear());
        assertNull(holdingsRecord.getGenre());
        assertNull(holdingsRecord.getFallbackLocationCode());
        assertNull(holdingsRecord.getEdition());
        assertNull(holdingsRecord.getOclc());
        assertNull(holdingsRecord.getCallNumber());
        assertFalse(holdingsRecord.isLargeVolume());
        assertNull(holdingsRecord.getCatalogItems());
    }

    @Test
    public void testBuilder() {
        final Map<String, Map<String, String>> catalogItems = new HashMap<>();
        final Map<String, String> catalogItem = new HashMap<>();
        catalogItem.put("key", "value");
        catalogItems.put("item", catalogItem);

        final HoldingsRecord holdingsRecord = new HoldingsRecord.HoldingsRecordBuilder()
            .recordId("recordId")
            .marcRecordLeader("marcRecordLeader")
            .mfhd("mfhd")
            .issn("issn")
            .isbn("isbn")
            .title("title")
            .author("author")
            .publisher("publisher")
            .place("place")
            .year("year")
            .genre("genre")
            .fallbackLocationCode("fallbackLocationCode")
            .edition("edition")
            .oclc("oclc")
            .callNumber("callNumber")
            .callNumberPrefix("callNumberPrefix")
            .holdingLocation("holdingLocation")
            .largeVolume(false)
            .catalogItems(catalogItems)
            .build();

        assertNotNull(holdingsRecord);
        assertNotNull(holdingsRecord.getCatalogItems());

        assertEquals("recordId", holdingsRecord.getRecordId());
        assertEquals("marcRecordLeader", holdingsRecord.getMarcRecordLeader());
        assertEquals("mfhd", holdingsRecord.getMfhd());
        assertEquals("issn", holdingsRecord.getIssn());
        assertEquals("isbn", holdingsRecord.getIsbn());
        assertEquals("title", holdingsRecord.getTitle());
        assertEquals("author", holdingsRecord.getAuthor());
        assertEquals("publisher", holdingsRecord.getPublisher());
        assertEquals("place", holdingsRecord.getPlace());
        assertEquals("year", holdingsRecord.getYear());
        assertEquals("genre", holdingsRecord.getGenre());
        assertEquals("fallbackLocationCode", holdingsRecord.getFallbackLocationCode());
        assertEquals("edition", holdingsRecord.getEdition());
        assertEquals("oclc", holdingsRecord.getOclc());
        assertEquals("callNumber", holdingsRecord.getCallNumber());
        assertEquals("callNumberPrefix", holdingsRecord.getCallNumberPrefix());
        assertEquals("holdingLocation", holdingsRecord.getHoldingLocation());
        assertEquals(false, holdingsRecord.isLargeVolume());
        assertEquals(1, holdingsRecord.getCatalogItems().size());

        assertFalse(holdingsRecord.isMultiVolume());
    }

    @Test
    public void testUpdate() {
        final Map<String, Map<String, String>> catalogItems = new HashMap<>();
        final Map<String, String> catalogItem = new HashMap<>();
        catalogItem.put("key", "value");
        catalogItems.put("item", catalogItem);

        final Map<String, Map<String, String>> updatedCatalogItems = new HashMap<>();
        final Map<String, String> catalogItem1 = new HashMap<>();
        final Map<String, String> catalogItem2 = new HashMap<>();
        catalogItem1.put("key1", "value1");
        catalogItem2.put("key2", "value2");
        updatedCatalogItems.put("item1", catalogItem1);
        updatedCatalogItems.put("item2", catalogItem2);

        final HoldingsRecord holdingsRecord = new HoldingsRecord("recordId", "marcRecordLeader", "mfhd", "issn",
            "isbn", "title", "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition",
            "oclc", "callNumber", "callNumberPrefix", "holdingLocation", new ArrayList<Note>(), new ArrayList<String>(), false, catalogItems);

        holdingsRecord.setRecordId("updatedRecordId");
        holdingsRecord.setMarcRecordLeader("updatedMarcRecordLeader");
        holdingsRecord.setMfhd("updatedMfhd");
        holdingsRecord.setIssn("updatedIssn");
        holdingsRecord.setIsbn("updatedIsbn");
        holdingsRecord.setTitle("updatedTitle");
        holdingsRecord.setAuthor("updatedAuthor");
        holdingsRecord.setPublisher("updatedPublisher");
        holdingsRecord.setPlace("updatedPlace");
        holdingsRecord.setYear("updatedYear");
        holdingsRecord.setGenre("updatedGenre");
        holdingsRecord.setFallbackLocationCode("updatedFallbackLocationCode");
        holdingsRecord.setEdition("updatedEdition");
        holdingsRecord.setOclc("updatedOclc");
        holdingsRecord.setCallNumber("updatedCallNumber");
        holdingsRecord.setCallNumberPrefix("updatedCallNumberPrefix");
        holdingsRecord.setHoldingLocation("updatedHoldingLocation");
        holdingsRecord.setLargeVolume(true);
        holdingsRecord.setCatalogItems(updatedCatalogItems);

        assertNotNull(holdingsRecord);
        assertNotNull(holdingsRecord.getCatalogItems());

        assertEquals("updatedRecordId", holdingsRecord.getRecordId());
        assertEquals("updatedMarcRecordLeader", holdingsRecord.getMarcRecordLeader());
        assertEquals("updatedMfhd", holdingsRecord.getMfhd());
        assertEquals("updatedIssn", holdingsRecord.getIssn());
        assertEquals("updatedIsbn", holdingsRecord.getIsbn());
        assertEquals("updatedTitle", holdingsRecord.getTitle());
        assertEquals("updatedAuthor", holdingsRecord.getAuthor());
        assertEquals("updatedPublisher", holdingsRecord.getPublisher());
        assertEquals("updatedPlace", holdingsRecord.getPlace());
        assertEquals("updatedYear", holdingsRecord.getYear());
        assertEquals("updatedGenre", holdingsRecord.getGenre());
        assertEquals("updatedFallbackLocationCode", holdingsRecord.getFallbackLocationCode());
        assertEquals("updatedEdition", holdingsRecord.getEdition());
        assertEquals("updatedOclc", holdingsRecord.getOclc());
        assertEquals("updatedCallNumber", holdingsRecord.getCallNumber());
        assertEquals("updatedCallNumberPrefix", holdingsRecord.getCallNumberPrefix());
        assertEquals("updatedHoldingLocation", holdingsRecord.getHoldingLocation());
        assertEquals(true, holdingsRecord.isLargeVolume());
        assertEquals(2, holdingsRecord.getCatalogItems().size());

        assertTrue(holdingsRecord.isMultiVolume());
    }

    @Test
    public void testEquals() {
        final Map<String, Map<String, String>> catalogItems = new HashMap<>();
        final Map<String, String> catalogItem = new HashMap<>();
        catalogItem.put("key", "value");
        catalogItems.put("item", catalogItem);

        final HoldingsRecord holdingsRecord1 = HoldingsRecord.builder()
            .recordId("recordId")
            .marcRecordLeader("marcRecordLeader")
            .mfhd("mfhd")
            .issn("issn")
            .isbn("isbn")
            .title("title")
            .author("author")
            .publisher("publisher")
            .place("place")
            .year("year")
            .genre("genre")
            .fallbackLocationCode("fallbackLocationCode")
            .edition("edition")
            .oclc("oclc")
            .callNumber("callNumber")
            .callNumberPrefix("callNumberPrefix")
            .holdingLocation("holdingLocation")
            .holdingNotes(new ArrayList<Note>())
            .holdingStatements(new ArrayList<String>())
            .largeVolume(false)
            .catalogItems(catalogItems)
            .build();

        final HoldingsRecord holdingsRecord2 = new HoldingsRecord("marcRecordLeader", "mfhd", "issn",
            "isbn", "title", "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition",
            "oclc", "recordId", "callNumber", "callNumberPrefix", "holdingLocation", new ArrayList<Note>(), new ArrayList<String>(), false, catalogItems);

        final HoldingsRecord holdingsRecord3 = new HoldingsRecord();

        assertTrue(holdingsRecord1.equals(holdingsRecord2));
        assertFalse(holdingsRecord1.equals(holdingsRecord3));
    }

}
