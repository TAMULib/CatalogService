package edu.tamu.catalog.controller;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;


import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.tamu.catalog.config.CatalogServiceConfig;
import edu.tamu.catalog.config.RestConfig;
import edu.tamu.catalog.utility.TokenUtility;

@RunWith(SpringRunner.class)
@WebMvcTest(value = PatronController.class, secure = false)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@Import({ RestConfig.class, CatalogServiceConfig.class })
public class PatronControllerTest {

    private static final String UIN = "1234567890";
    private static final String SERVICE_POINTS_ID = "ebab9ccc-4ece-4f35-bc82-01f3325abed8";
    private static final String REQUEST_ID = "8bbac557-d66f-4571-bbbf-47a107cc1589";
    private static final String ITEM_ID = "40053ccb-fd0c-304b-9547-b2fc06f34d3e";

    private static final String BASE_PATH = "http://localhost:8080/";
    private static final String API_KEY = "mock_api_key";
    private static final String FOLIO_CATALOG = "folio";
    private static final String VOYAGER_CATALOG = "msl";

    private static final String FINES_ENDPOINT = "fines";
    private static final String LOANS_ENDPOINT = "loans";
    private static final String RENEWAL_ENDPOINT = "renew";
    private static final String HOLDS_ENDPOINT = "holds";
    private static final String BLOCK_ENDPOINT = "block";

    private static final String DOC_PREFIX = "patron/";

    private static final String HOLDS_CANCEL_MVC_PATH = "/patron/{uin}/holds/{requestId}/cancel";
    private static final String RENEW_MVC_PATH = RENEWAL_ENDPOINT + "/{itemId}";

    private static final String OKAPI_PATH = "http://localhost:9130/";
    private static final String OKAPI_TOKEN = "mocked_token";
    private static final String OKAPI_TOKEN_HEADER = "X-Okapi-Token";

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

    private MockRestServiceServer restServer;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        MockRestServiceServerBuilder builder = MockRestServiceServer.bindTo(restTemplate);

        builder.ignoreExpectOrder(true);

        restServer = builder.build();

        TokenUtility.clearAll();
    }

    @Test
    public void testFinesMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName("uin").description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName("catalogName").description("The name of the catalog to use.").optional()
        );

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].amount").description("The title of the item associated with the fine."),
            fieldWithPath("[].fineId").description("The UUID associated with the fine."),
            fieldWithPath("[].fineType").description("The type of the fine."),
            fieldWithPath("[].fineDate").description("A timestamp in milliseconds from UNIX epoch representing the date the fine was accrued."),
            fieldWithPath("[].itemTitle").description("The title of the item associated with the fine.")
        );

        getAccountEndpointWithMockMVC(getFinesUrl(), FINES_ENDPOINT, pathParameters, requestParameters, responseFields);
    }

    @Test
    public void testLoansMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName("uin").description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName("catalogName").description("The name of the catalog to use.").optional()
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

        getAccountEndpointWithMockMVC(getLoansUrl(), LOANS_ENDPOINT, pathParameters, requestParameters, responseFields);
    }

    @Test
    public void testLoanItemRenewalMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName("uin").description("The patron UIN."),
            parameterWithName("itemId").description("The UUID of the loan item.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName("catalogName").description("The name of the catalog to use.").optional()
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

        expectPostResponse(getLoanItemRenewalUrl(), once(), okResponse(patronAccountRenewalResource));

        mockMvc.perform(post("/patron/{uin}/" + RENEW_MVC_PATH, UIN, ITEM_ID)
            .param("catalogName", FOLIO_CATALOG)
            .contentType(MediaType.APPLICATION_JSON)
            .content(getMockJson(patronAccountRenewalResource))
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
            parameterWithName("uin").description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName("catalogName").description("The name of the catalog to use.").optional()
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

        performHoldsGetWithCatalogName(once(), once(), once(), once(), successResponse(patronAccountResource),
            successResponse(holdRequestResource), successResponse(servicePointResource), FOLIO_CATALOG)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andDo(
                document(
                    "patron/" + HOLDS_ENDPOINT,
                    pathParameters,
                    requestParameters,
                    responseFields
                )
            );

        restServer.verify();
    }

    public void testCancelHoldRequestMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName("uin").description("The patron UIN."),
            parameterWithName("requestId").description("The Hold Request UUID.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName("catalogName").description("The name of the catalog to use.").optional()
        );

        expectPostResponse(getCancelHoldRequestUrl(), once(), createdResponse(patronAccountCancelHoldResponseResource));

        mockMvc.perform(post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .param("catalogName", FOLIO_CATALOG)
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
            parameterWithName("uin").description("The patron UIN.")
        );

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName("catalogName").description("The name of the catalog to use.").optional()
        );

        mockMvc.perform(get("/patron/{uin}/" + BLOCK_ENDPOINT, UIN)
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
        performGet(getFinesUrl(), FINES_ENDPOINT, once(), withStatus(HttpStatus.NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testLoansNotFound() throws Exception {
        performGet(getLoansUrl(), LOANS_ENDPOINT, once(), withStatus(HttpStatus.NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalNotFound() throws Exception {
        performLoanItemRenewalPost(once(), withStatus(HttpStatus.NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testHoldsNotFound() throws Exception {
        performHoldsGet(never(), once(), never(), never(), withStatus(HttpStatus.NOT_FOUND), withNoContent(),
            withNoContent())
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testCancelHoldNotFound() throws Exception {
        performHoldsCancelPost(once(), withStatus(HttpStatus.NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testFinesDateParseError() throws Exception {
        performGet(getFinesUrl(), FINES_ENDPOINT, once(), successResponse(patronAccountDateParseErrorResource))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testLoansDateParseError() throws Exception {
        performGet(getLoansUrl(), LOANS_ENDPOINT, once(), successResponse(patronAccountDateParseErrorResource))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testHoldsDateParseError() throws Exception {
        performHoldsGet(between(0, 1), once(), between(0, 1), between(0, 1), successResponse(patronAccountDateParseErrorResource),
            successResponse(holdRequestResource), successResponse(servicePointResource))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testFinesNotSupportedForCatalog() throws Exception {
        performGetWithCatalogName(getFinesUrl(), FINES_ENDPOINT, never(), withStatus(HttpStatus.OK), VOYAGER_CATALOG)
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoansNotSupportedForCatalog() throws Exception {
        performGetWithCatalogName(getLoansUrl(), LOANS_ENDPOINT, never(), withStatus(HttpStatus.OK), VOYAGER_CATALOG)
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalNotSupportedForCatalog() throws Exception {
        expectPostResponse(getLoanItemRenewalUrl(), never(), withStatus(HttpStatus.OK));

        mockMvc.perform(post("/patron/{uin}/" + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .param("catalogName", VOYAGER_CATALOG)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testHoldsNotSupportedForCatalog() throws Exception {
        performHoldsGetWithCatalogName(never(), never(), never(), never(), withNoContent(), withNoContent(),
            withNoContent(), VOYAGER_CATALOG)
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testCancelHoldNotSupportedForCatalog() throws Exception {
        expectPostResponse(getCancelHoldRequestUrl(), never(), withStatus(HttpStatus.OK));

        mockMvc.perform(post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .param("catalogName", VOYAGER_CATALOG)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesClientException() throws Exception {
        performGet(getFinesUrl(), FINES_ENDPOINT, once(), withStatus(HttpStatus.BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoansClientException() throws Exception {
        performGet(getLoansUrl(), LOANS_ENDPOINT, once(), withStatus(HttpStatus.BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalClientException() throws Exception {
        performLoanItemRenewalPost(once(), withStatus(HttpStatus.BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testHoldsClientException() throws Exception {
        performHoldsGet(never(), once(), never(), never(), withStatus(HttpStatus.BAD_REQUEST), withNoContent(),
            withNoContent())
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testCancelHoldClientException() throws Exception {
        performHoldsCancelPost(once(), withStatus(HttpStatus.BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesServerException() throws Exception {
        performGet(getFinesUrl(), FINES_ENDPOINT, once(), withStatus(HttpStatus.INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testLoansServerException() throws Exception {
        performGet(getLoansUrl(), LOANS_ENDPOINT, once(), withStatus(HttpStatus.INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testLoanItemRenewalServerException() throws Exception {
        performLoanItemRenewalPost(once(), withStatus(HttpStatus.INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testHoldsServerException() throws Exception {
        performHoldsGet(never(), once(), never(), never(), withStatus(HttpStatus.INTERNAL_SERVER_ERROR),
            withNoContent(), withNoContent())
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testCancelHoldServerException() throws Exception {
        performHoldsCancelPost(once(), withStatus(HttpStatus.INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    private ResultActions performLoanItemRenewalPost(ExpectedCount count, DefaultResponseCreator response) throws Exception {
        expectPostResponse(getLoanItemRenewalUrl(), count, response);

        return mockMvc.perform(post("/patron/{uin}/" + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performHoldsGet(ExpectedCount okapiCount, ExpectedCount holdsCount, ExpectedCount requestsCount, ExpectedCount servicePointsCount, DefaultResponseCreator holdsResponse, DefaultResponseCreator requestsResponse, DefaultResponseCreator servicePointsResponse) throws Exception  {
        expectGetResponse(getHoldsUrl(), holdsCount, holdsResponse);
        expectOkapiLoginResponse(okapiCount, withStatus(HttpStatus.CREATED));
        expectGetResponse(getOkapiRequestsUrl(), requestsCount, requestsResponse);
        expectGetResponse(getOkapiServicePointsUrl(), servicePointsCount, servicePointsResponse);

        return mockMvc.perform(get("/patron/{uin}/" + HOLDS_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performHoldsGetWithCatalogName(ExpectedCount okapiCount, ExpectedCount holdsCount, ExpectedCount requestsCount, ExpectedCount servicePointsCount, DefaultResponseCreator holdsResponse, DefaultResponseCreator requestsResponse, DefaultResponseCreator servicePointsResponse, String catalogName) throws Exception  {
        expectGetResponse(getHoldsUrl(), holdsCount, holdsResponse);
        expectOkapiLoginResponse(okapiCount, withStatus(HttpStatus.CREATED));
        expectGetResponse(getOkapiRequestsUrl(), requestsCount, requestsResponse);
        expectGetResponse(getOkapiServicePointsUrl(), servicePointsCount, servicePointsResponse);

        return mockMvc.perform(get("/patron/{uin}/" + HOLDS_ENDPOINT, UIN)
            .param("catalogName", catalogName)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performHoldsCancelPost(ExpectedCount count, DefaultResponseCreator response) throws Exception {
        expectPostResponse(getCancelHoldRequestUrl(), count, response);

        return mockMvc.perform(post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private void getAccountEndpointWithMockMVC(String sourceUrl, String catalogEndpoint, PathParametersSnippet pathParameters, RequestParametersSnippet requestParameters, ResponseFieldsSnippet responseFields) throws Exception {
        performGetWithCatalogName(sourceUrl, catalogEndpoint, once(), successResponse(patronAccountResource), FOLIO_CATALOG)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andDo(
                document(
                    DOC_PREFIX + catalogEndpoint,
                    pathParameters,
                    requestParameters,
                    responseFields
                )
            );

        restServer.verify();
    }

    private ResultActions performGet(String sourceUrl, String catalogEndpoint, ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        expectGetResponse(sourceUrl, count, response);

        return mockMvc.perform(get("/patron/{uin}/" + catalogEndpoint, UIN)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performGetWithCatalogName(String sourceUrl, String catalogEndpoint, ExpectedCount count, DefaultResponseCreator response, String catalogName) throws Exception  {
        expectGetResponse(sourceUrl, count, response);

        return mockMvc.perform(get("/patron/{uin}/" + catalogEndpoint, UIN)
            .param("catalogName", catalogName)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private void expectOkapiLoginResponse(ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OKAPI_TOKEN_HEADER, OKAPI_TOKEN);

        restServer.expect(count, requestTo(getOkapiLoginUrl()))
            .andExpect(method(HttpMethod.POST))
            .andRespond(response.headers(headers));
    }

    private void expectGetResponse(String sourceUrl, ExpectedCount count, DefaultResponseCreator response) throws Exception {
        expectResponse(HttpMethod.GET, sourceUrl, count, response);
    }

    private void expectPostResponse(String sourceUrl, ExpectedCount count, DefaultResponseCreator response) throws Exception {
        expectResponse(HttpMethod.POST, sourceUrl, count, response);
    }

    private void expectResponse(HttpMethod httpMethod, String sourceUrl, ExpectedCount count, DefaultResponseCreator response) throws Exception {
        restServer.expect(count, requestTo(sourceUrl))
            .andExpect(method(httpMethod))
            .andRespond(response);
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

    private String getOkapiServicePointsUrl() {
        return String.format("%sservice-points/%s", OKAPI_PATH, SERVICE_POINTS_ID);
    }

    private String getOkapiRequestsUrl() {
        return String.format("%scirculation/requests/%s", OKAPI_PATH, REQUEST_ID);
    }

    private String getOkapiLoginUrl() {
        return String.format("%sauthn/login", OKAPI_PATH);
    }

    private String getAccountUrl(boolean loans, boolean charges, boolean holds) {
        return String.format("%spatron/account/%s?apikey=%s&includeLoans=%s&includeCharges=%s&includeHolds=%s",
            BASE_PATH, UIN, API_KEY, Boolean.toString(loans), Boolean.toString(charges), Boolean.toString(holds));
    }

    private String getCancelHoldRequestUrl() {
        return String.format("%spatron/account/%s/holds/%s/cancel?apikey=%s", BASE_PATH, UIN, REQUEST_ID, API_KEY);
    }

    private String getLoanItemRenewalUrl() {
        return String.format("%spatron/account/%s/item/%s/renew?apikey=%s", BASE_PATH, UIN, ITEM_ID, API_KEY);
    }

    private DefaultResponseCreator okResponse(Resource resource) throws Exception {
        return withStatus(HttpStatus.OK).body(resource).contentType(MediaType.APPLICATION_JSON);
    }

    private DefaultResponseCreator createdResponse(Resource resource) throws Exception {
        return withStatus(HttpStatus.CREATED).body(resource).contentType(MediaType.APPLICATION_JSON);
    }

    private DefaultResponseCreator successResponse(Resource resource) throws Exception {
        return withSuccess(resource, MediaType.APPLICATION_JSON);
    }

    private String getMockJson(Resource resource) throws JsonParseException, JsonMappingException, IOException {
        return IOUtils.toString(resource.getInputStream(), "UTF-8");
    }

}
