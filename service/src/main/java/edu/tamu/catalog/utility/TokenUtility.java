package edu.tamu.catalog.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TokenUtility {

    private static final Map<String, String> tokens = new HashMap<>();

    private TokenUtility() {

    }

    /**
     * Assign the token for the given catalog name.
     * @param catalog The catalog name.
     * @param token The token to associate the catalog name with.
     */
    public static synchronized void setToken(String catalog, String token) {
        tokens.put(catalog, token);
    }

    /**
     * Retreive the token for the given catalog name.
     * @param catalog The catalog name.
     * @return The token associated with the given catalog name.
     */
    public static synchronized Optional<String> getToken(String catalog) {
        return Optional.ofNullable(tokens.get(catalog));
    }

    /**
     * Remove any existing token from the cache for the given catalog name.
     * @param catalog The catalog name.
     */
    public static synchronized void clearToken(String catalog) {
        tokens.remove(catalog);
    }

    /**
     * Remove all existing tokens from the cache.
     */
    public static synchronized void clearAll() {
        tokens.clear();
    }

}
