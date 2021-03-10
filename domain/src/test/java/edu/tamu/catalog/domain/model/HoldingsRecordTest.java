package edu.tamu.catalog.domain.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class HoldingsRecordTest {

    @Test
    public void testCreate() {
        final Map<String, Map<String, String>> catalogItems = new HashMap<>();
        final Map<String, String> catalogItem = new HashMap<>();
        catalogItem.put("key", "value");
        catalogItems.put("item", catalogItem);

        final HoldingsRecord holdingsRecord = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", false, catalogItems);

        Assert.assertNotNull(holdingsRecord);
        Assert.assertNotNull(holdingsRecord.getCatalogItems());

        Assert.assertEquals("marcRecordLeader", holdingsRecord.getMarcRecordLeader());
        Assert.assertEquals("mfhd", holdingsRecord.getMfhd());
        Assert.assertEquals("issn", holdingsRecord.getIssn());
        Assert.assertEquals("isbn", holdingsRecord.getIsbn());
        Assert.assertEquals("title", holdingsRecord.getTitle());
        Assert.assertEquals("author", holdingsRecord.getAuthor());
        Assert.assertEquals("publisher", holdingsRecord.getPublisher());
        Assert.assertEquals("place", holdingsRecord.getPlace());
        Assert.assertEquals("year", holdingsRecord.getYear());
        Assert.assertEquals("genre", holdingsRecord.getGenre());
        Assert.assertEquals("fallbackLocationCode", holdingsRecord.getFallbackLocationCode());
        Assert.assertEquals("edition", holdingsRecord.getEdition());
        Assert.assertEquals("oclc", holdingsRecord.getOclc());
        Assert.assertEquals("recordId", holdingsRecord.getRecordId());
        Assert.assertEquals("callNumber", holdingsRecord.getCallNumber());
        Assert.assertEquals(false, holdingsRecord.isLargeVolume());
        Assert.assertEquals(1, holdingsRecord.getCatalogItems().size());
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

        Assert.assertNotNull(holdingsRecord);
        Assert.assertNotNull(holdingsRecord.getCatalogItems());

        Assert.assertEquals("updatedMarcRecordLeader", holdingsRecord.getMarcRecordLeader());
        Assert.assertEquals("updatedMfhd", holdingsRecord.getMfhd());
        Assert.assertEquals("updatedIssn", holdingsRecord.getIssn());
        Assert.assertEquals("updatedIsbn", holdingsRecord.getIsbn());
        Assert.assertEquals("updatedTitle", holdingsRecord.getTitle());
        Assert.assertEquals("updatedAuthor", holdingsRecord.getAuthor());
        Assert.assertEquals("updatedPublisher", holdingsRecord.getPublisher());
        Assert.assertEquals("updatedPlace", holdingsRecord.getPlace());
        Assert.assertEquals("updatedYear", holdingsRecord.getYear());
        Assert.assertEquals("updatedGenre", holdingsRecord.getGenre());
        Assert.assertEquals("updatedFallbackLocationCode", holdingsRecord.getFallbackLocationCode());
        Assert.assertEquals("updatedEdition", holdingsRecord.getEdition());
        Assert.assertEquals("updatedOclc", holdingsRecord.getOclc());
        Assert.assertEquals("updatedRecordId", holdingsRecord.getRecordId());
        Assert.assertEquals("updatedCallNumber", holdingsRecord.getCallNumber());
        Assert.assertEquals(true, holdingsRecord.isLargeVolume());
        Assert.assertEquals(2, holdingsRecord.getCatalogItems().size());
    }

    @Test
    public void testEquals() {
        final Map<String, Map<String, String>> catalogItems = new HashMap<>();
        final Map<String, String> catalogItem = new HashMap<>();
        catalogItem.put("key", "value");
        catalogItems.put("item", catalogItem);

        final HoldingsRecord holdingsRecord1 = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", false, catalogItems);

        final HoldingsRecord holdingsRecord2 = new HoldingsRecord("marcRecordLeader", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", false, catalogItems);

        final HoldingsRecord holdingsRecord3 = new HoldingsRecord("different", "mfhd", "issn", "isbn", "title",
            "author", "publisher", "place", "year", "genre", "fallbackLocationCode", "edition", "oclc", "recordId",
            "callNumber", false, catalogItems);

        Assert.assertTrue(holdingsRecord1.equals(holdingsRecord2));
        Assert.assertFalse(holdingsRecord1.equals(holdingsRecord3));
    }

}