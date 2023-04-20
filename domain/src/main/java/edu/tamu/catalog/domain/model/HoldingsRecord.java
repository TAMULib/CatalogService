package edu.tamu.catalog.domain.model;

import java.util.List;
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

    private String callNumberPrefix;

    private String holdingLocation;

    private List<Note> holdingNotes;

    private List<String> holdingStatements;

    private boolean largeVolume;

    private Map<String, Map<String, String>> catalogItems;

    public boolean isMultiVolume() {
        return (this.getCatalogItems().size() > 1);
    }

}
