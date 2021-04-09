package edu.tamu.catalog.controller;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import edu.tamu.catalog.test.AbstractTestRestController;
import edu.tamu.catalog.utility.TokenUtility;

public class PatronControllerTestBase extends AbstractTestRestController {

    protected static final String UIN = "1234567890";
    protected static final String SERVICE_POINTS_ID = "ebab9ccc-4ece-4f35-bc82-01f3325abed8";
    protected static final String LOCATION_ID = "1d1ca55a-86a1-489b-a645-2d52742c196a";
    protected static final String REQUEST_ID = "8bbac557-d66f-4571-bbbf-47a107cc1589";
    protected static final String INSTANCE_ID = "829fecd3-67c3-3ca2-b9d4-281227690e0f";
    protected static final String ITEM_ID = "f5a63a6f-4b1d-3a75-8fba-668efffae4ad";
    protected static final String LOAN_ID = "adbe373f-89ec-44e9-8840-f6906adc7adf";
    protected static final String USER_ID = "93710b5b-aa9a-43be-af34-7dcb1f7b0669";

    protected static final String LOAN_POLICY_NAME = "day_367_renew_indefinite_recall_y";

    protected static final int INSTANCES_TOTAL = 4;
    protected static final int ITEMS_TOTAL = 4;
    protected static final int LOANS_TOTAL = 4;

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
    protected static String locationPayload;
    protected static String loanPolicyPayload;
    protected static String blUserResponsePayload;
    protected static String blUserBadUUIDErrorPayload;
    protected static String blUserDuplicateErrorPayload;
    protected static String blUserEmptyErrorPayload;
    protected static String automatedBlocksResponsePayload;
    protected static String instancePayload;
    protected static String instancesPayload;
    protected static String itemPayload;
    protected static String itemsPayload;
    protected static String loanPayload;
    protected static String loansPayload;

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
        holdRequestPayload = loadPayload("mock/response/requests/holdRequest.json");
        servicePointPayload = loadPayload("mock/response/service-points/servicePoint.json");
        blUserResponsePayload = loadPayload("mock/response/users/user.json");
        blUserBadUUIDErrorPayload = loadPayload("mock/response/users/userBadUUIDError.json");
        blUserDuplicateErrorPayload = loadPayload("mock/response/users/userDuplicateError.json");
        blUserEmptyErrorPayload = loadPayload("mock/response/users/userEmptyError.json");
        automatedBlocksResponsePayload = loadPayload("mock/response/patron-blocks/automatedBlocks.json");
        instancePayload = loadPayload("mock/response/instances/instance.json");
        instancesPayload = loadPayload("mock/response/instances/instances.json");
        itemPayload = loadPayload("mock/response/items/item.json");
        itemsPayload = loadPayload("mock/response/items/items.json");
        loanPayload = loadPayload("mock/response/loans/loan.json");
        loansPayload = loadPayload("mock/response/loans/loans.json");
        locationPayload = loadPayload("mock/response/locations/location.json");
        loanPolicyPayload = loadPayload("mock/response/loanPolicies/loanPolicy.json");

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

    protected static String getOkapiLocationsUrl() {
        return getOkapiUrl(String.format("locations/%s", LOCATION_ID));
    }

    protected static String getOkapiLoanPoliciesUrl() {
        return getOkapiUrl(String.format("loan-policy-storage/loan-policies?query=name%%3D%%3D%s", LOAN_POLICY_NAME));
    }

    protected static String getOkapiRequestsUrl() {
        return getOkapiUrl(String.format("circulation/requests/%s", REQUEST_ID));
    }

    protected static String getOkapiInstancesUrl(String instanceId) {
        return getOkapiUrl(String.format("instance-storage/instances/%s", instanceId));
    }

    protected static String getOkapiItemsUrl(String itemId) {
        return getOkapiUrl(String.format("inventory/items/%s", itemId));
    }

    protected static String getOkapiLoansUrl(String loanId) {
        return getOkapiUrl(String.format("circulation/loans/%s", loanId));
    }

    protected static String getOkapiBatchInstancesUrl(int size) {
        return getOkapiUrl(String.format("instance-storage/instances\\?limit=%s&query=id%%3D%%3D.*", size));
    }

    protected static String getOkapiBatchItemsUrl(int size) {
        return getOkapiUrl(String.format("inventory/items\\?limit=%s&query=id%%3D%%3D.*", size));
    }

    protected static String getOkapiBatchLoansUrl(int size) {
        return getOkapiUrl(String.format("circulation/loans\\?limit=%s&query=id%%3D%%3D.*", size));
    }

    protected static String getOkapiBLUsersByUinUrl() {
        return getOkapiUrl(String.format("bl-users?query=(externalSystemId%%3D%%3D%%22%s%%22)&limit=2", UIN));
    }

    protected static String getOkapiAutomatedBlocksUrl(String userId) {
        return getOkapiUrl(String.format("automated-patron-blocks/%s", userId));
    }

    protected static String getCancelHoldRequestUrl() {
        return String.format("%spatron/account/%s/hold/%s/cancel?apikey=%s", BASE_PATH, UIN, REQUEST_ID, API_KEY);
    }

    protected static String getLoanRenewalUrl() {
        return String.format("%spatron/account/%s/item/%s/renew?apikey=%s", BASE_PATH, UIN, ITEM_ID, API_KEY);
    }

    protected static String loadPayload(String path) throws IOException {
        return loadResource(PatronControllerTestBase.class.getClassLoader().getResource(path));
    }

    protected static String descUIN() {
        return "The *Patron* _UIN_.\n\n" +
            "In _FOLIO_, this is the `externalSystemId`.";
    }

    protected static String descCatalogName(String defaultName) {
        return "A name of the catalog to use.\n\n" +
            "The catalog settings are loaded from `src/main/resources/catalogs/{catalogName}.json` file.\n\n" +
            "This endpoint defaults to `" + defaultName + "`.";
    }

    protected static String descId(String modelName) {
        return "A _UUID_ representing a " + modelName + ".";
    }

    protected static String descItemId(String modelName) {
        return "A _UUID_ representing the Item associated with this " + modelName + ".";
    }

    protected static String descItemTitle(String modelName) {
        return "A title of the *Item* associated with this " + modelName + ".";
    }

    protected static String descInstanceId(String modelName) {
        return "A _UUID_ representing a *Holding Record Instance* associated with this " + modelName + ".";
    }

    protected static String descInstanceHrid(String modelName) {
        return "A human-readable number representing associated with this " + modelName + ".";
    }

    protected static String descAmount(boolean futureTense) {
        return "A dollar amount " + (futureTense ? "to be" : "") + " charged.";
    }

    protected static String descType(String modelName) {
        return "A type of the " + modelName + ".";
    }

    protected static String descTimestamp(String details) {
        return "A timestamp in milliseconds from _UNIX Epoch_ representing the date " + details + ".";
    }

    protected static String descBoolean(String details) {
        return "Designates that the " + details + ".";
    }

    protected static String descField(String modelName, String fieldName) {
        return "A " + fieldName + " of the " + modelName + ".";
    }

    protected static String descPickupServicePoint() {
        return "A title representing the *Pickup Service Point* location.";
    }

    protected static String descQueuePosition() {
        return "The position within the queue.";
    }

}
