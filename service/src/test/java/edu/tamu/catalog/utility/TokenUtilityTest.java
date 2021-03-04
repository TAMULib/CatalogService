package edu.tamu.catalog.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
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

}
