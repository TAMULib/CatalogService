package edu.tamu.catalog.utility;

import edu.tamu.catalog.model.FolioTokens;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Provide a map for caching FOLIO tokens with a named catalog service.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolioTokenUtility {

    private static final Map<String, FolioTokens> map = new HashMap<>();

    /**
     * Assign the token for the given catalog name.
     *
     * @param catalog The catalog name.
     * @param tokens The FOLIO tokens to associate the catalog name with.
     */
    public static synchronized void setTokens(String catalog, FolioTokens tokens) {
        map.put(catalog, tokens);
    }

    /**
     * Retrieve the FOLIO tokens for the given catalog name.
     *
     * @param catalog The catalog name.
     *
     * @return The FOLIO tokens associated with the given catalog name.
     */
    public static synchronized FolioTokens getTokens(String catalog) {
        return map.get(catalog);
    }

    /**
     * Remove any existing FOLIO tokens from the cache for the given catalog name.
     *
     * @param catalog The catalog name.
     */
    public static synchronized void clearTokens(String catalog) {
        map.remove(catalog);
    }

    /**
     * Remove all existing FOLIO tokens from the cache.
     */
    public static synchronized void clearAll() {
        map.clear();
    }

}
