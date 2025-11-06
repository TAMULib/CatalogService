package edu.tamu.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The FOLIO access and refresh tokens for a single login session.
 *
 * The X-Okapi-Token is represented by the access token.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FolioTokens {

    private FolioToken access;

    private FolioToken refresh;

}
