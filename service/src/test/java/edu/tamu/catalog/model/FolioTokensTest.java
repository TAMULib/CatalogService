package edu.tamu.catalog.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FolioTokensTest {

    private static final String UUID_1 = "3b0c8c59-88b6-4563-919c-86dac2a5c87d";
    private static final String UUID_2 = "1fbd7e9f-d553-47ec-9e2f-0c61ab996ec6";

    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private static final ZonedDateTime LATER = NOW.plusSeconds(1);

    private static final FolioToken ACCESS = new FolioToken(UUID_1, NOW);
    private static final FolioToken REFRESH = new FolioToken(UUID_2, LATER);

    private FolioTokens folioTokens;

    @BeforeEach
    void beforeEach() {
        folioTokens = new FolioTokens();
    }

    @Test
    void initializeAllArgsTest() {
        folioTokens = new FolioTokens(ACCESS, REFRESH);

        FolioToken access = (FolioToken) getField(folioTokens, "access");
        FolioToken refresh = (FolioToken) getField(folioTokens, "refresh");

        assertNotNull(access);
        assertNotNull(refresh);

        assertEquals(ACCESS.getToken(), access.getToken());
        assertEquals(ACCESS.getExpire(), access.getExpire());

        assertEquals(REFRESH.getToken(), refresh.getToken());
        assertEquals(REFRESH.getExpire(), refresh.getExpire());
    }

    @Test
    void initializeNoArgsTest() {
        assertNull(getField(folioTokens, "access"));
        assertNull(getField(folioTokens, "refresh"));
    }

    @Test
        void getAccessWorksTest() {
        setField(folioTokens, "access", ACCESS);

        assertEquals(ACCESS, folioTokens.getAccess());
    }

    @Test
        void setAccessWorksTest() {
        setField(folioTokens, "access", null);

        folioTokens.setAccess(ACCESS);
        assertEquals(ACCESS, getField(folioTokens, "access"));
    }

    @Test
        void getRefreshWorksTest() {
        setField(folioTokens, "refresh", REFRESH);

        assertEquals(REFRESH, folioTokens.getRefresh());
    }

    @Test
        void setRefreshWorksTest() {
        setField(folioTokens, "refresh", null);

        folioTokens.setRefresh(REFRESH);
        assertEquals(REFRESH, getField(folioTokens, "refresh"));
    }

}
