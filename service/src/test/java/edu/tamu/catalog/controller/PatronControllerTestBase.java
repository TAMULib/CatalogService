package edu.tamu.catalog.controller;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.tamu.catalog.test.AbstractTestRestController;
import edu.tamu.catalog.utility.TokenUtility;

public class PatronControllerTestBase extends AbstractTestRestController {

    protected static final String UIN = "1234567890";
    protected static final String SERVICE_POINTS_ID = "ebab9ccc-4ece-4f35-bc82-01f3325abed8";
    protected static final String REQUEST_ID = "8bbac557-d66f-4571-bbbf-47a107cc1589";
    protected static final String INSTANCE_ID1 = "2422160d-23c4-356b-ad1c-44d90fc1320b";
    protected static final String ITEM_ID = "40053ccb-fd0c-304b-9547-b2fc06f34d3e";
    protected static final String USER_ID = "93710b5b-aa9a-43be-af34-7dcb1f7b0669";

    protected static final int INSTANCES_TOTAL = 4;

    protected static final String FOLIO_CATALOG = "folio";
    protected static final String VOYAGER_CATALOG = "msl";

    protected static final String UIN_FIELD = "uin";
    protected static final String CATALOG_FIELD = "catalogName";

    protected static final String FINES_ENDPOINT = "fines";
    protected static final String LOANS_ENDPOINT = "loans";
    protected static final String RENEWAL_ENDPOINT = "renew";
    protected static final String HOLDS_ENDPOINT = "holds";
    protected static final String BLOCK_ENDPOINT = "block";

    protected static final String DOC_PREFIX = "patron/";
    protected static final String PATRON_MVC_PREFIX = "/patron/{uin}/";

    protected static final String HOLDS_CANCEL_MVC_PATH = PATRON_MVC_PREFIX + "holds/{requestId}/cancel";
    protected static final String RENEW_MVC_PATH = RENEWAL_ENDPOINT + "/{itemId}";

    protected static String patronAccountPayload;
    protected static String patronAccountDateParseErrorPayload;
    protected static String patronAccountRenewalPayload;
    protected static String patronAccountCancelHoldResponsePayload;
    protected static String holdRequestPayload;
    protected static String servicePointPayload;
    protected static String blUserResponsePayload;
    protected static String blUserBadUUIDErrorPayload;
    protected static String blUserDuplicateErrorPayload;
    protected static String blUserEmptyErrorPayload;
    protected static String automatedBlocksResponsePayload;
    protected static String instance1Payload;
    protected static String instancesPayload;

    protected static String finesCatalogPayload;
    protected static String loansCatalogPayload;
    protected static String loanRenewalCatalogPayload;
    protected static String requestsCatalogPayload;
    protected static String blockCatalogPayload;
    protected static String blockEmptyCatalogPayload;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestTemplate restTemplate;

    @BeforeAll
    public static void setupStatic() throws IOException {
        patronAccountPayload = loadPayload("mock/response/patron/account.json");
        patronAccountDateParseErrorPayload = loadPayload("mock/response/patron/accountDateParseError.json");
        patronAccountRenewalPayload = loadPayload("mock/response/patron/accountRenewableLoanItem.json");
        patronAccountCancelHoldResponsePayload = loadPayload("mock/response/patron/accountCancelHoldResponse.json");
        holdRequestPayload = loadPayload("mock/response/request/holdRequest.json");
        servicePointPayload = loadPayload("mock/response/service-point/servicePoint.json");
        blUserResponsePayload = loadPayload("mock/response/bl-users/user.json");
        blUserBadUUIDErrorPayload = loadPayload("mock/response/bl-users/userBadUUIDError.json");
        blUserDuplicateErrorPayload = loadPayload("mock/response/bl-users/userDuplicateError.json");
        blUserEmptyErrorPayload = loadPayload("mock/response/bl-users/userEmptyError.json");
        automatedBlocksResponsePayload = loadPayload("mock/response/patron-blocks/automatedBlocks.json");
        instance1Payload = loadPayload("mock/response/instances/in1.json");
        instancesPayload = loadPayload("mock/response/instances/instances.json");

        finesCatalogPayload = loadPayload("mock/response/catalog/fines.json");
        loansCatalogPayload = loadPayload("mock/response/catalog/loans.json");
        loanRenewalCatalogPayload = loadPayload("mock/response/catalog/loanRenewal.json");
        requestsCatalogPayload = loadPayload("mock/response/catalog/requests.json");
        blockCatalogPayload = loadPayload("mock/response/catalog/block.txt");
        blockEmptyCatalogPayload = loadPayload("mock/response/catalog/blockEmpty.txt");
    }

    @BeforeEach
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        buildRestServer(restTemplate, true);
        TokenUtility.clearAll();
    }

    protected static String getFinesUrl() {
        return getAccountUrl(false, true, false);
    }

    protected static String getLoansUrl() {
        return getAccountUrl(true, false, false);
    }

    protected static String getHoldsUrl() {
        return getAccountUrl(false, false, true);
    }

    protected static String getAccountUrl(boolean loans, boolean charges, boolean holds) {
        return String.format("%spatron/account/%s?apikey=%s&includeLoans=%s&includeCharges=%s&includeHolds=%s",
            BASE_PATH, UIN, API_KEY, Boolean.toString(loans), Boolean.toString(charges), Boolean.toString(holds));
    }

    protected static String getOkapiServicePointsUrl() {
        return getOkapiUrl(String.format("service-points/%s", SERVICE_POINTS_ID));
    }

    protected static String getOkapiRequestsUrl() {
        return getOkapiUrl(String.format("circulation/requests/%s", REQUEST_ID));
    }

    protected static String getOkapiInstancesUrl(String instanceId) {
        return getOkapiUrl(String.format("instance-storage/instances/%s", instanceId));
    }

    protected static String getOkapiBatchInstancesUrl(int size) {
        return getOkapiUrl(String.format("instance-storage/instances\\?limit=%s&query=id%%3D%%3D.*", size));
    }

    protected static String getOkapiBLUsersByUinUrl() {
        return getOkapiUrl(String.format("bl-users?query=(externalSystemId%%3D%%3D%%22%s%%22)&limit=2", UIN));
    }

    protected static String getOkapiAutomatedBlocksUrl(String userId) {
        return getOkapiUrl(String.format("automated-patron-blocks/%s", userId));
    }

    protected static String getCancelHoldRequestUrl() {
        return String.format("%spatron/account/%s/holds/%s/cancel?apikey=%s", BASE_PATH, UIN, REQUEST_ID, API_KEY);
    }

    protected static String getLoanRenewalUrl() {
        return String.format("%spatron/account/%s/item/%s/renew?apikey=%s", BASE_PATH, UIN, ITEM_ID, API_KEY);
    }

    protected static String loadPayload(String path) throws IOException {
        return loadResource(PatronControllerTestBase.class.getClassLoader().getResource(path));
    }

    protected static String describeUIN() {
        return "The *Patron* _UIN_.\n\n" +
            "In _FOLIO_, this is the `externalSystemId`.";
    }

    protected static String describeCatalogName(String defaultName) {
        return "A name of the catalog to use.\n\n" +
            "The catalog settings are loaded from `src/main/resources/catalogs/{catalogName}.json` file.\n\n" +
            "This endpoint defaults to `" + defaultName + "`.";
    }

    protected static String describeId(String modelName) {
        return "A _UUID_ representing a " + modelName + ".";
    }

    protected static String describeItemId(String modelName) {
        return "A _UUID_ representing the Item associated with this " + modelName + ".";
    }

    protected static String describeItemTitle(String modelName) {
        return "A title of the *Item* associated with this " + modelName + ".";
    }

    protected static String describeInstanceId(String modelName) {
        return "A _UUID_ representing a *Holding Record Instance* associated with this " + modelName + ".";
    }

    protected static String describeInstanceHrid(String modelName) {
        return "A human-readable number representing associated with this " + modelName + ".";
    }

    protected static String describeAmount(boolean futureTense) {
        return "A dollar amount " + (futureTense ? "to be" : "") + " charged.";
    }

    protected static String describeType(String modelName) {
        return "A type of the " + modelName + ".";
    }

    protected static String describeTimestamp(String details) {
        return "A timestamp in milliseconds from _UNIX Epoch_ representing the date " + details + ".";
    }

    protected static String describeBoolean(String details) {
        return "Designates that the " + details + ".";
    }

    protected static String describeField(String modelName, String fieldName) {
        return "A " + fieldName + " of the " + modelName + ".";
    }

    protected static String describePickupServicePoint() {
        return "A title representing the *Pickup Service Point* location.";
    }

    protected static String describeQueuePosition() {
        return "The position within the queue.";
    }

}
