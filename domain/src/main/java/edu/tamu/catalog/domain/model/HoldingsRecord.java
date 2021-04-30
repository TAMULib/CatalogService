package edu.tamu.catalog.domain.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HoldingsRecord {

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

    private boolean largeVolume;

    private Map<String, Map<String, String>> catalogItems;

    public HoldingsRecord(String marcRecordLeader, String mfhd, String issn, String isbn, String title, String author,
            String publisher, String place, String year, String genre, String edition, String fallBackLocationCode, String oclc, String recordId, String callNumber,
            Map<String, Map<String, String>> catalogItems) {
        this(marcRecordLeader, mfhd, issn, isbn, title, author, publisher, place, year, genre, fallBackLocationCode, edition, oclc, recordId, callNumber, false, catalogItems);
    }

    public boolean isMultiVolume() {
        return (this.getCatalogItems().size() > 1);
    }

}
