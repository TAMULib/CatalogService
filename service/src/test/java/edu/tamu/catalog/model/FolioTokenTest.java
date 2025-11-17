package edu.tamu.catalog.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FolioTokenTest {

    private static final String UUID = "3b0c8c59-88b6-4563-919c-86dac2a5c87d";

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private FolioToken folioToken;

    @BeforeEach
    void beforeEach() {
        folioToken = new FolioToken();
    }

    @Test
    void initializeAllArgsTest() {
        folioToken = new FolioToken(UUID, NOW);

        assertEquals(NOW, getField(folioToken, "expire"));
        assertEquals(UUID, getField(folioToken, "token"));
    }

    @Test
    void initializeNoArgsTest() {
        assertNull(getField(folioToken, "expire"));
        assertNull(getField(folioToken, "token"));
    }

    @Test
    void getExpireWorksTest() {
        setField(folioToken, "expire", NOW);

        assertEquals(NOW, folioToken.getExpire());
    }

    @Test
    void getIdWorksTest() {
        setField(folioToken, "token", UUID);

        assertEquals(UUID, folioToken.getToken());
    }

    @Test
    void setExpireWorksTest() {
        setField(folioToken, "expire", null);

        folioToken.setExpire(NOW);
        assertEquals(NOW, getField(folioToken, "expire"));
    }

    @Test
    void setIdWorksTest() {
        setField(folioToken, "token", null);

        folioToken.setToken(UUID);
        assertEquals(UUID, getField(folioToken, "token"));
    }

}
