package edu.tamu.catalog.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TokenUtility {

    private static final Map<String, String> tokens = new HashMap<>();

    private TokenUtility() {

    }

    public static synchronized void setToken(String catalog, String token) {
        tokens.put(catalog, token);
    }

    public static synchronized Optional<String> getToken(String catalog) {
        return Optional.ofNullable(tokens.get(catalog));
    }

}
