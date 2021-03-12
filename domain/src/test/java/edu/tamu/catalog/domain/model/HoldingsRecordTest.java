package edu.tamu.catalog.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class HoldingsRecordTest {

    @Test
    public void testCreateAllArgs() {
        final Map<String, Map<String, String>> catalogItems = new HashMap<>();
        final Map<String, String> catalogItem = new HashMap<>();
        catalogItem.put("key", "value");
        catalogItems.put("item", catalogItem);

        final HoldingsRecord holdingsRecord = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", false, catalogItems);

        assertNotNull(holdingsRecord);
        assertNotNull(holdingsRecord.getCatalogItems());

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
        assertEquals("recordId", holdingsRecord.getRecordId());
        assertEquals("callNumber", holdingsRecord.getCallNumber());
        assertEquals(false, holdingsRecord.isLargeVolume());
        assertEquals(1, holdingsRecord.getCatalogItems().size());

        assertFalse(holdingsRecord.isMultiVolume());
    }

    @Test
    public void testCreateNoArgs() {
        final HoldingsRecord holdingsRecord = new HoldingsRecord();

        assertNotNull(holdingsRecord);

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
        assertNull(holdingsRecord.getRecordId());
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
            .recordId("recordId")
            .callNumber("callNumber")
            .largeVolume(false)
            .catalogItems(catalogItems)
            .build();

        assertNotNull(holdingsRecord);
        assertNotNull(holdingsRecord.getCatalogItems());

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
        assertEquals("recordId", holdingsRecord.getRecordId());
        assertEquals("callNumber", holdingsRecord.getCallNumber());
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

        final HoldingsRecord holdingsRecord = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", false, catalogItems);

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
        holdingsRecord.setRecordId("updatedRecordId");
        holdingsRecord.setCallNumber("updatedCallNumber");
        holdingsRecord.setLargeVolume(true);
        holdingsRecord.setCatalogItems(updatedCatalogItems);

        assertNotNull(holdingsRecord);
        assertNotNull(holdingsRecord.getCatalogItems());

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
        assertEquals("updatedRecordId", holdingsRecord.getRecordId());
        assertEquals("updatedCallNumber", holdingsRecord.getCallNumber());
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

        final HoldingsRecord holdingsRecord1 = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", catalogItems);

        final HoldingsRecord holdingsRecord2 = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", false, catalogItems);

        final HoldingsRecord holdingsRecord3 = new HoldingsRecord();

        assertTrue(holdingsRecord1.equals(holdingsRecord2));
        assertFalse(holdingsRecord1.equals(holdingsRecord3));
    }

}