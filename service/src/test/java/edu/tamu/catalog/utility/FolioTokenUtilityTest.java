package edu.tamu.catalog.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.tamu.catalog.config.FolioTenantConfig;
import edu.tamu.catalog.config.FolioTokenConfig;
import edu.tamu.catalog.model.FolioToken;
import edu.tamu.catalog.model.FolioTokens;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import({ FolioTenantConfig.class , FolioTokenConfig.class })
public class FolioTokenUtilityTest {

    private static final String CATALOG_NAME_1 = "folio1";
    private static final String CATALOG_NAME_2 = "folio2";
    private static final String CATALOG_NAME_3 = "folio3";

    private static final String UUID_1 = "3b0c8c59-88b6-4563-919c-86dac2a5c87d";
    private static final String UUID_2 = "1fbd7e9f-d553-47ec-9e2f-0c61ab996ec6";
    private static final String UUID_3 = "c0b4c07b-418f-4ed1-bfdc-2dde643a6546";
    private static final String UUID_4 = "5c23dcf5-aedc-4a5b-9692-1922b5c98891";
    private static final String UUID_5 = "92d6ca6d-aa3e-49b1-bf92-db24b7543aa7";
    private static final String UUID_6 = "18e893c1-8501-41b0-ad3e-e0ef1edbbf85";

    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private static final ZonedDateTime LATER_1 = NOW.plusSeconds(1);
    private static final ZonedDateTime LATER_2 = NOW.plusSeconds(2);
    private static final ZonedDateTime LATER_3 = NOW.plusSeconds(3);
    private static final ZonedDateTime LATER_4 = NOW.plusSeconds(4);
    private static final ZonedDateTime LATER_5 = NOW.plusSeconds(5);

    private static final FolioToken ACCESS_1 = new FolioToken(UUID_1, NOW);
    private static final FolioToken ACCESS_2= new FolioToken(UUID_2, LATER_1);
    private static final FolioToken ACCESS_3 = new FolioToken(UUID_3, LATER_2);

    private static final FolioToken REFRESH_1 = new FolioToken(UUID_4, LATER_3);
    private static final FolioToken REFRESH_2 = new FolioToken(UUID_5, LATER_4);
    private static final FolioToken REFRESH_3 = new FolioToken(UUID_6, LATER_5);

    private static final FolioTokens FOLIO_TOKENS_1 = new FolioTokens(ACCESS_1, REFRESH_1);
    private static final FolioTokens FOLIO_TOKENS_2 = new FolioTokens(ACCESS_2, REFRESH_2);
    private static final FolioTokens FOLIO_TOKENS_3 = new FolioTokens(ACCESS_3, REFRESH_3);

    @BeforeEach
    void beforeEach() {
        FolioTokenUtility.clearAll();
    }

    @Test
    public void setAndGetTokensTest() {
        FolioTokenUtility.setTokens(CATALOG_NAME_1, FOLIO_TOKENS_1);

        assertEquals(FOLIO_TOKENS_1, FolioTokenUtility.getTokens(CATALOG_NAME_1));
    }

    @Test
    public void getTokensNotFoundTest() {
        assertNull(FolioTokenUtility.getTokens("does_not_exist"));
    }

    @Test
    public void clearTokensTest() {
        FolioTokenUtility.setTokens(CATALOG_NAME_2, FOLIO_TOKENS_2);
        FolioTokenUtility.setTokens(CATALOG_NAME_3, FOLIO_TOKENS_3);
        FolioTokenUtility.clearTokens(CATALOG_NAME_2);

        assertNull(FolioTokenUtility.getTokens(CATALOG_NAME_2));
    }

    @Test
    public void clearAllTest() {
        FolioTokenUtility.setTokens(CATALOG_NAME_1, FOLIO_TOKENS_1);
        FolioTokenUtility.setTokens(CATALOG_NAME_2, FOLIO_TOKENS_2);
        FolioTokenUtility.setTokens(CATALOG_NAME_3, FOLIO_TOKENS_3);

        assertEquals(FOLIO_TOKENS_1, FolioTokenUtility.getTokens(CATALOG_NAME_1));
        assertEquals(FOLIO_TOKENS_2, FolioTokenUtility.getTokens(CATALOG_NAME_2));
        assertEquals(FOLIO_TOKENS_3, FolioTokenUtility.getTokens(CATALOG_NAME_3));

        FolioTokenUtility.clearAll();

        assertNull(FolioTokenUtility.getTokens(CATALOG_NAME_1));
        assertNull(FolioTokenUtility.getTokens(CATALOG_NAME_2));
        assertNull(FolioTokenUtility.getTokens(CATALOG_NAME_3));
    }

}
