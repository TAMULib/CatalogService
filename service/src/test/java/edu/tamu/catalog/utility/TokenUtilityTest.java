package edu.tamu.catalog.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class TokenUtilityTest {

    @Test
    public void testSetToken() {
        TokenUtility.setToken("folio", "token");
        Optional<String> token = TokenUtility.getToken("folio");

        assertTrue(token.isPresent());
        assertEquals("token", token.get());
    }

    @Test
    public void testTokenNotFound() {
        assertFalse(TokenUtility.getToken("evans").isPresent());
    }

    @Test
    public void testClearToken() {
        TokenUtility.setToken("folio1", "token1");
        TokenUtility.setToken("folio2", "token2");
        TokenUtility.clearToken("folio1");

        assertFalse(TokenUtility.getToken("folio1").isPresent());
    }

    @Test
    public void testClearAll() {
        TokenUtility.setToken("folio1", "token1");
        TokenUtility.setToken("folio2", "token2");

        assertTrue(TokenUtility.getToken("folio1").isPresent());
        assertTrue(TokenUtility.getToken("folio2").isPresent());

        TokenUtility.clearAll();

        assertFalse(TokenUtility.getToken("folio1").isPresent());
        assertFalse(TokenUtility.getToken("folio2").isPresent());
    }

}
