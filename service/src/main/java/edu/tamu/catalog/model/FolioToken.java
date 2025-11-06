package edu.tamu.catalog.model;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A FOLIO token and its associated expiration date.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FolioToken {

    private String token;

    private ZonedDateTime expire;

}
