package edu.tamu.catalog.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.client.ExpectedCount.between;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.tamu.catalog.config.CatalogServiceConfig;
import edu.tamu.catalog.config.RestConfig;
import edu.tamu.catalog.test.AbstractTestRestController;
import edu.tamu.catalog.utility.TokenUtility;

@RunWith(SpringRunner.class)
@WebMvcTest(value = PatronController.class, secure = false)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@Import({ RestConfig.class, CatalogServiceConfig.class })
public class PatronControllerTest extends AbstractTestRestController {

    private static final String UIN = "1234567890";
    private static final String SERVICE_POINTS_ID = "ebab9ccc-4ece-4f35-bc82-01f3325abed8";
    private static final String REQUEST_ID = "8bbac557-d66f-4571-bbbf-47a107cc1589";
    private static final String ITEM_ID = "40053ccb-fd0c-304b-9547-b2fc06f34d3e";

    private static final String FOLIO_CATALOG = "folio";
    private static final String VOYAGER_CATALOG = "msl";

    private static final String UIN_FIELD = "uin";
    private static final String CATALOG_FIELD = "catalogName";

    private static final String FINES_ENDPOINT = "fines";
    private static final String LOANS_ENDPOINT = "loans";
    private static final String RENEWAL_ENDPOINT = "renew";
    private static final String HOLDS_ENDPOINT = "holds";
    private static final String BLOCK_ENDPOINT = "block";

    private static final String DOC_PREFIX = "patron/";
    private static final String PATRON_MVC_PREFIX = "/patron/{uin}/";

    private static final String HOLDS_CANCEL_MVC_PATH = PATRON_MVC_PREFIX + "holds/{requestId}/cancel";
    private static final String RENEW_MVC_PATH = RENEWAL_ENDPOINT + "/{itemId}";

    @Value("classpath:mock/response/patron/account.json")
    private Resource patronAccountResource;

    @Value("classpath:mock/response/patron/accountDateParseError.json")
    private Resource patronAccountDateParseErrorResource;

    @Value("classpath:mock/response/patron/accountRenewableLoanItem.json")
    private Resource patronAccountRenewalResource;

    @Value("classpath:mock/response/patron/accountCancelHoldResponse.json")
    private Resource patronAccountCancelHoldResponseResource;

    @Value("classpath:mock/response/request/holdRequest.json")
    private Resource holdRequestResource;

    @Value("classpath:mock/response/service-point/servicePoint.json")
    private Resource servicePointResource;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        buildRestServer(restTemplate, true);
        TokenUtility.clearAll();
    }

    @Test
    public void testFinesMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional()
        );

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].amount").description("The title of the item associated with the fine."),
            fieldWithPath("[].fineId").description("The UUID associated with the fine."),
            fieldWithPath("[].fineType").description("The type of the fine."),
            fieldWithPath("[].fineDate").description("A timestamp in milliseconds from UNIX epoch representing the date the fine was accrued."),
            fieldWithPath("[].itemTitle").description("The title of the item associated with the fine.")
        );

        performPatronGetWithMockMVC(getFinesUrl(), FINES_ENDPOINT, pathParameters,
            requestParameters, responseFields);
    }

    @Test
    public void testLoansMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional()
        );

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].loanId").description("The loan id."),
            fieldWithPath("[].itemId").description("The item id."),
            fieldWithPath("[].instanceId").description("The instance id."),
            fieldWithPath("[].loanDate").description("The loan date."),
            fieldWithPath("[].loanDueDate").description("The loan due date."),
            fieldWithPath("[].overdue").description("Is the loan overdue."),
            fieldWithPath("[].title").description("The title of the loan item."),
            fieldWithPath("[].author").description("The author of the loan item.")
        );

        performPatronGetWithMockMVC(getLoansUrl(), LOANS_ENDPOINT, pathParameters, requestParameters, responseFields);
    }

    @Test
    public void testLoanItemRenewalMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN."),
            parameterWithName("itemId").description("The UUID of the loan item.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional()
        );

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("loanId").description("The loan id."),
            fieldWithPath("itemId").description("The item id."),
            fieldWithPath("instanceId").description("The instance id."),
            fieldWithPath("loanDate").description("The loan date."),
            fieldWithPath("loanDueDate").description("The loan due date."),
            fieldWithPath("overdue").description("Is the loan overdue."),
            fieldWithPath("title").description("The title of the loan item."),
            fieldWithPath("author").description("The author of the loan item.")
        );

        expectPostResponse(getLoanItemRenewalUrl(), once(), respondJsonOk(patronAccountRenewalResource));

        mockMvc.perform(post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
            .param(CATALOG_FIELD, FOLIO_CATALOG)
            .contentType(MediaType.APPLICATION_JSON)
            .content(loadJsonResource(patronAccountRenewalResource))
        )
        .andExpect(status().isOk())
        .andDo(
            document(
                DOC_PREFIX + RENEWAL_ENDPOINT,
                pathParameters,
                requestParameters,
                responseFields
            )
        );

        restServer.verify();
    }

    @Test
    public void testHoldRequestsMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional()
        );

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].requestId").description("The UUID of the hold request."),
            fieldWithPath("[].itemId").description("The UUID of the item associated with the hold request."),
            fieldWithPath("[].requestType").description("The type of the hold request."),
            fieldWithPath("[].itemTitle").description("The title of the item associated with the fine."),
            fieldWithPath("[].statusText").description("A descriptive status of the hold request."),
            fieldWithPath("[].pickupServicePoint").description("A title describing the pickup service point location."),
            fieldWithPath("[].queuePosition").description("The position within the queue."),
            fieldWithPath("[].expirationDate").description("A timestamp in milliseconds from UNIX epoch representing the date the hold request will expire.")
        );

        performHoldsGet(once(), once(), once(), once(), respondJsonSuccess(patronAccountResource),
            respondJsonSuccess(holdRequestResource), respondJsonSuccess(servicePointResource), FOLIO_CATALOG)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andDo(
                document(
                    DOC_PREFIX + HOLDS_ENDPOINT,
                    pathParameters,
                    requestParameters,
                    responseFields
                )
            );

        restServer.verify();
    }

    @Test
    public void testCancelHoldRequestMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN."),
            parameterWithName("requestId").description("The Hold Request UUID.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional()
        );

        expectPostResponse(getCancelHoldRequestUrl(), once(),
            respondJsonCreated(patronAccountCancelHoldResponseResource));

        mockMvc.perform(post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .param(CATALOG_FIELD, FOLIO_CATALOG)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(
            document(
                DOC_PREFIX + "holds/cancel",
                pathParameters,
                requestParameters
            )
        );

        restServer.verify();
    }

    @Test
    public void testBlockMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional()
        );

        mockMvc.perform(get(PATRON_MVC_PREFIX + BLOCK_ENDPOINT, UIN)
            .param(CATALOG_FIELD, FOLIO_CATALOG)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andDo(
            document(
                DOC_PREFIX + BLOCK_ENDPOINT,
                pathParameters,
                requestParameters
            )
        );
    }

    @Test
    public void testFinesNotFound() throws Exception {
        performPatronGet(getFinesUrl(), FINES_ENDPOINT, once(), withStatus(NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testLoansNotFound() throws Exception {
        performPatronGet(getLoansUrl(), LOANS_ENDPOINT, once(), withStatus(NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalNotFound() throws Exception {
        performLoanItemRenewalPost(once(), withStatus(NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testHoldsNotFound() throws Exception {
        performHoldsGet(never(), once(), never(), never(), withStatus(NOT_FOUND), withNoContent(),
            withNoContent())
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testCancelHoldNotFound() throws Exception {
        performHoldsCancelPost(once(), withStatus(NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testFinesDateParseError() throws Exception {
        performPatronGet(getFinesUrl(), FINES_ENDPOINT, once(),
            respondJsonSuccess(patronAccountDateParseErrorResource))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testLoansDateParseError() throws Exception {
        performPatronGet(getLoansUrl(), LOANS_ENDPOINT, once(),
            respondJsonSuccess(patronAccountDateParseErrorResource))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testHoldsDateParseError() throws Exception {
        performHoldsGet(between(0, 1), once(), between(0, 1), between(0, 1),
            respondJsonSuccess(patronAccountDateParseErrorResource), respondJsonSuccess(holdRequestResource),
            respondJsonSuccess(servicePointResource))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testFinesNotSupportedForCatalog() throws Exception {
        performPatronGet(getFinesUrl(), FINES_ENDPOINT, never(), withStatus(OK), VOYAGER_CATALOG)
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoansNotSupportedForCatalog() throws Exception {
        performPatronGet(getLoansUrl(), LOANS_ENDPOINT, never(), withStatus(OK), VOYAGER_CATALOG)
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalNotSupportedForCatalog() throws Exception {
        expectPostResponse(getLoanItemRenewalUrl(), never(), withStatus(OK));

        mockMvc.perform(post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testHoldsNotSupportedForCatalog() throws Exception {
        performHoldsGet(never(), never(), never(), never(), withNoContent(), withNoContent(),
            withNoContent(), VOYAGER_CATALOG)
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testCancelHoldNotSupportedForCatalog() throws Exception {
        expectPostResponse(getCancelHoldRequestUrl(), never(), withStatus(OK));

        mockMvc.perform(post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesClientException() throws Exception {
        performPatronGet(getFinesUrl(), FINES_ENDPOINT, once(), withStatus(BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoansClientException() throws Exception {
        performPatronGet(getLoansUrl(), LOANS_ENDPOINT, once(), withStatus(BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalClientException() throws Exception {
        performLoanItemRenewalPost(once(), withStatus(BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testHoldsClientException() throws Exception {
        performHoldsGet(never(), once(), never(), never(), withStatus(BAD_REQUEST), withNoContent(),
            withNoContent())
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testHoldsOkapiClientException() throws Exception {
        performHoldsGet(once(), once(), once(), never(), respondJsonSuccess(patronAccountResource),
            withStatus(BAD_REQUEST), withNoContent())
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testCancelHoldClientException() throws Exception {
        performHoldsCancelPost(once(), withStatus(BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesServerException() throws Exception {
        performPatronGet(getFinesUrl(), FINES_ENDPOINT, once(), withStatus(INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testLoansServerException() throws Exception {
        performPatronGet(getLoansUrl(), LOANS_ENDPOINT, once(), withStatus(INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalServerException() throws Exception {
        performLoanItemRenewalPost(once(), withStatus(INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testHoldsServerException() throws Exception {
        performHoldsGet(never(), once(), never(), never(), withStatus(INTERNAL_SERVER_ERROR),
            withNoContent(), withNoContent())
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testHoldsOkapiServerException() throws Exception {
        performHoldsGet(once(), once(), once(), never(), respondJsonSuccess(patronAccountResource),
            withStatus(INTERNAL_SERVER_ERROR), withNoContent())
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testCancelHoldServerException() throws Exception {
        performHoldsCancelPost(once(), withStatus(INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    private void performPatronGetWithMockMVC(String url, String endpoint, PathParametersSnippet pathParameters,
            RequestParametersSnippet requestParameters, ResponseFieldsSnippet responseFields) throws Exception {
        performPatronGet(url, endpoint, once(), respondJsonSuccess(patronAccountResource), FOLIO_CATALOG)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andDo(
                document(
                    DOC_PREFIX + endpoint,
                    pathParameters,
                    requestParameters,
                    responseFields
                )
            );

        restServer.verify();
    }

    private ResultActions performLoanItemRenewalPost(ExpectedCount count, DefaultResponseCreator response)
            throws Exception {
        expectPostResponse(getLoanItemRenewalUrl(), count, response);

        return mockMvc.perform(post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performHoldsGet(ExpectedCount okapiCount, ExpectedCount holdsCount,
            ExpectedCount requestsCount, ExpectedCount servicePointsCount, DefaultResponseCreator holdsResponse,
            DefaultResponseCreator requestsResponse, DefaultResponseCreator servicePointsResponse) throws Exception {
        expectGetResponse(getHoldsUrl(), holdsCount, holdsResponse);
        expectOkapiLoginResponse(okapiCount, withStatus(CREATED));
        expectGetResponse(getOkapiRequestsUrl(), requestsCount, requestsResponse);
        expectGetResponse(getOkapiServicePointsUrl(), servicePointsCount, servicePointsResponse);

        return mockMvc.perform(get(PATRON_MVC_PREFIX + HOLDS_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performHoldsGet(ExpectedCount okapiCount, ExpectedCount holdsCount,
            ExpectedCount requestsCount, ExpectedCount servicePointsCount, DefaultResponseCreator holdsResponse,
            DefaultResponseCreator requestsResponse, DefaultResponseCreator servicePointsResponse, String catalogName)
            throws Exception  {
        expectGetResponse(getHoldsUrl(), holdsCount, holdsResponse);
        expectOkapiLoginResponse(okapiCount, withStatus(CREATED));
        expectGetResponse(getOkapiRequestsUrl(), requestsCount, requestsResponse);
        expectGetResponse(getOkapiServicePointsUrl(), servicePointsCount, servicePointsResponse);

        return mockMvc.perform(get(PATRON_MVC_PREFIX + HOLDS_ENDPOINT, UIN)
            .param(CATALOG_FIELD, catalogName)
            .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performHoldsCancelPost(ExpectedCount count, DefaultResponseCreator response)
            throws Exception {
        expectPostResponse(getCancelHoldRequestUrl(), count, response);

        return mockMvc.perform(post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPatronGet(String url, String endpoint, ExpectedCount count,
            DefaultResponseCreator response) throws Exception  {
        expectGetResponse(url, count, response);

        return mockMvc.perform(get(PATRON_MVC_PREFIX + endpoint, UIN)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performPatronGet(String url, String endpoint, ExpectedCount count,
            DefaultResponseCreator response, String catalog) throws Exception  {
        expectGetResponse(url, count, response);

        return mockMvc.perform(get(PATRON_MVC_PREFIX + endpoint, UIN)
            .param(CATALOG_FIELD, catalog)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private String getFinesUrl() {
        return getAccountUrl(false, true, false);
    }

    private String getLoansUrl() {
        return getAccountUrl(true, false, false);
    }

    private String getHoldsUrl() {
        return getAccountUrl(false, false, true);
    }

    private String getAccountUrl(boolean loans, boolean charges, boolean holds) {
        return String.format("%spatron/account/%s?apikey=%s&includeLoans=%s&includeCharges=%s&includeHolds=%s",
            BASE_PATH, UIN, API_KEY, Boolean.toString(loans), Boolean.toString(charges), Boolean.toString(holds));
    }

    private String getOkapiServicePointsUrl() {
        return getOkapiUrl(String.format("service-points/%s", SERVICE_POINTS_ID));
    }

    private String getOkapiRequestsUrl() {
        return getOkapiUrl(String.format("circulation/requests/%s", REQUEST_ID));
    }

    private String getCancelHoldRequestUrl() {
        return String.format("%spatron/account/%s/holds/%s/cancel?apikey=%s", BASE_PATH, UIN, REQUEST_ID, API_KEY);
    }

    private String getLoanItemRenewalUrl() {
        return String.format("%spatron/account/%s/item/%s/renew?apikey=%s", BASE_PATH, UIN, ITEM_ID, API_KEY);
    }

}
