package edu.tamu.catalog.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a (somewhat) generalize holding from a catalog
 *
 * @author Jason Savell <jsavell@library.tamu.edu>
 *
 */

public class CatalogHolding {
    private String marcRecordLeader;
    private String mfhd;
    private String issn;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String place;
    private String year;
    private String genre;
    private String fallbackLocationCode;
    private String edition;
    private String oclc;
    private String recordId;
    private String callNumber;
    private boolean largeVolume = false;

    private Map<String, Map<String, String>> catalogItems = new HashMap<String, Map<String, String>>();

    public CatalogHolding(String marcRecordLeader, String mfhd, String issn, String isbn, String title, String author,
            String publisher, String place, String year, String genre, String edition, String fallBackLocationCode, String oclc, String recordId, String callNumber,
            Map<String, Map<String, String>> catalogItems) {
        this.setMarcRecordLeader(marcRecordLeader);
        this.setMfhd(mfhd);
        this.setCatalogItems(catalogItems);
        this.setIssn(issn);
        this.setIsbn(isbn);
        this.setTitle(title);
        this.setAuthor(author);
        this.setPublisher(publisher);
        this.setPlace(place);
        this.setYear(year);
        this.setGenre(genre);
        this.setEdition(edition);
        this.setFallbackLocationCode(fallBackLocationCode);
        this.setOclc(oclc);
        this.setRecordId(recordId);
        this.setCallNumber(callNumber);
    }

    public CatalogHolding(String marcRecordLeader, String mfhd, String issn, String isbn, String title, String author,
            String publisher, String place, String year, String genre, String edition, String fallBackLocationCode, String oclc, String recordId, String callNumber,
            boolean largeVolume, Map<String, Map<String, String>> catalogItems) {
        this(marcRecordLeader, mfhd, issn, isbn, title, author, publisher, place, year, genre, edition, fallBackLocationCode, oclc, recordId, callNumber, catalogItems);
        this.setLargeVolume(largeVolume);
    }

    public String getMarcRecordLeader() {
        return marcRecordLeader;
    }

    public void setMarcRecordLeader(String marcRecordLeader) {
        this.marcRecordLeader = marcRecordLeader;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getMfhd() {
        return mfhd;
    }

    public void setMfhd(String mfhd) {
        this.mfhd = mfhd;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getFallbackLocationCode() {
        return fallbackLocationCode;
    }

    public void setFallbackLocationCode(String fallbackLocationCode) {
        this.fallbackLocationCode = fallbackLocationCode;
    }

    public String getOclc() {
        return oclc;
    }

    public void setOclc(String oclc) {
        this.oclc = oclc;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public Map<String, Map<String, String>> getCatalogItems() {
        return catalogItems;
    }

    public void setCatalogItems(Map<String, Map<String, String>> catalogItems) {
        this.catalogItems = catalogItems;
    }

    public boolean isMultiVolume() {
        return (this.getCatalogItems().size() > 1);
    }

    public boolean isLargeVolume() {
        return largeVolume;
    }

    public void setLargeVolume(boolean largeVolume) {
        this.largeVolume = largeVolume;
    }

    /**
     * A helper method to allow outside callers to access getters when all they know
     * is the name of the parameter
     *
     * @param String propertyName
     * @return String
     * @throws Exception
     */
    public String getValueByPropertyName(String propertyName) throws Exception {
        Class<?> c = Class.forName(this.getClass().getCanonicalName());
        Method m = null;
        m = c.getDeclaredMethod("get" + StringUtils.capitalize(propertyName));
        return (String) m.invoke(this);
    }
}
