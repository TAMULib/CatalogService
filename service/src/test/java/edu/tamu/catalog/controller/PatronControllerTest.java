package edu.tamu.catalog.controller;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
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
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.tamu.catalog.config.CatalogServiceConfig;
import edu.tamu.catalog.config.RestConfig;
import edu.tamu.catalog.test.AbstractTestRestController;
import edu.tamu.catalog.utility.TokenUtility;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = PatronController.class, secure = false)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@Import({ RestConfig.class, CatalogServiceConfig.class })
public class PatronControllerTest extends AbstractTestRestController {

    private static final String UIN = "1234567890";
    private static final String SERVICE_POINTS_ID = "ebab9ccc-4ece-4f35-bc82-01f3325abed8";
    private static final String REQUEST_ID = "8bbac557-d66f-4571-bbbf-47a107cc1589";
    private static final String INSTANCE_ID1 = "2422160d-23c4-356b-ad1c-44d90fc1320b";
    private static final String INSTANCE_ID2 = "829fecd3-67c3-3ca2-b9d4-281227690e0f";
    private static final String INSTANCE_ID3 = "b8ea27b8-5280-3023-bbf8-9113849120a1";
    private static final String INSTANCE_ID4 = "a0480d3f-181e-3abd-a091-288e1cfc05ab";
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

    private static Resource patronAccountResource;
    private static Resource patronAccountDateParseErrorResource;
    private static Resource patronAccountRenewalResource;
    private static Resource patronAccountCancelHoldResponseResource;
    private static Resource holdRequestResource;
    private static Resource servicePointResource;
    private static Resource instance1Resource;
    private static Resource instance2Resource;
    private static Resource instance3Resource;
    private static Resource instance4Resource;

    private static Resource finesCatalogResource;
    private static Resource loansCatalogResource;
    private static Resource loanRenewalCatalogResource;
    private static Resource requestsCatalogResource;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
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
            requestParameters, responseFields, loadJsonResource(finesCatalogResource));
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
            fieldWithPath("[].instanceHrid").description("The instance human-readable id."),
            fieldWithPath("[].loanDate").description("The loan date."),
            fieldWithPath("[].loanDueDate").description("The loan due date."),
            fieldWithPath("[].overdue").description("Is the loan overdue."),
            fieldWithPath("[].title").description("The title of the loan item."),
            fieldWithPath("[].author").description("The author of the loan item.")
        );

        expectGetResponse(getLoansUrl(), once(), respondJsonSuccess(patronAccountResource));
        expectOkapiLoginResponse(between(0, 4), withStatus(CREATED));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID1), once(), respondJsonSuccess(instance1Resource));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID2), once(), respondJsonSuccess(instance2Resource));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID3), once(), respondJsonSuccess(instance3Resource));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID4), once(), respondJsonSuccess(instance4Resource));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + LOANS_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(loadJsonResource(loansCatalogResource)))
            .andDo(
                document(
                    DOC_PREFIX + LOANS_ENDPOINT,
                    pathParameters,
                    requestParameters,
                    responseFields
                )
            );

        restServer.verify();
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
            fieldWithPath("instanceHrid").description("The instance human-readable id."),
            fieldWithPath("loanDate").description("The loan date."),
            fieldWithPath("loanDueDate").description("The loan due date."),
            fieldWithPath("overdue").description("Is the loan overdue."),
            fieldWithPath("title").description("The title of the loan item."),
            fieldWithPath("author").description("The author of the loan item.")
        );

        expectPostResponse(getLoanItemRenewalUrl(), once(), respondJsonOk(patronAccountRenewalResource));
        expectOkapiLoginResponse(between(0, 1), withStatus(CREATED));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID1), once(), respondJsonSuccess(instance1Resource));

        mockMvc.perform(
            post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
                .content(loadJsonResource(loanRenewalCatalogResource))
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

        expectGetResponse(getHoldsUrl(), once(), respondJsonSuccess(patronAccountResource));
        expectOkapiLoginResponse(once(), withStatus(CREATED));
        expectGetResponse(getOkapiRequestsUrl(), once(), respondJsonSuccess(holdRequestResource));
        expectGetResponse(getOkapiServicePointsUrl(), once(), respondJsonSuccess(servicePointResource));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + HOLDS_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(loadJsonResource(requestsCatalogResource)))
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

        mockMvc.perform(
            post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            )
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
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

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + BLOCK_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().string("false")) // FIXME: this is clearly not json.
            .andDo(
                document(
                    DOC_PREFIX + BLOCK_ENDPOINT,
                    pathParameters,
                    requestParameters
                )
            );
    }

    @ParameterizedTest
    @MethodSource("argumentsEndpointResponses")
    public void testEndpoints(MockHttpServletRequestBuilder builder, String url, HttpMethod method,
            ExpectedCount count, DefaultResponseCreator response, ResultMatcher result) throws Exception {

        expectResponse(url, method, count, response);

        mockMvc.perform(builder)
            .andExpect(result);

        restServer.verify();
    }

    @ParameterizedTest
    @MethodSource("argumentsLoansResponses")
    public void testLoansEndpoints(MockHttpServletRequestBuilder builder, String url, HttpMethod method,
            ExpectedCount count, DefaultResponseCreator response, ResultMatcher result) throws Exception {

        expectResponse(url, method, count, response);
        expectOkapiLoginResponse(between(0, 4), withStatus(CREATED));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID1), between(0, 1), respondJsonSuccess(instance1Resource));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID2), between(0, 1), respondJsonSuccess(instance2Resource));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID3), between(0, 1), respondJsonSuccess(instance3Resource));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID4), between(0, 1), respondJsonSuccess(instance4Resource));

        mockMvc.perform(builder)
            .andExpect(result);

        restServer.verify();
    }

    @ParameterizedTest
    @MethodSource("argumentsHoldsResponses")
    public void testHoldsEndpoint(MockHttpServletRequestBuilder builder, String url,
            ExpectedCount okapiCount, ExpectedCount holdsCount, ExpectedCount requestsCount,
            ExpectedCount servicePointsCount, DefaultResponseCreator holdsResponse,
            DefaultResponseCreator requestsResponse, DefaultResponseCreator servicePointsResponse,
            ResultMatcher result) throws Exception {

        expectGetResponse(getHoldsUrl(), holdsCount, holdsResponse);
        expectOkapiLoginResponse(okapiCount, withStatus(CREATED));
        expectGetResponse(getOkapiRequestsUrl(), requestsCount, requestsResponse);
        expectGetResponse(getOkapiServicePointsUrl(), servicePointsCount, servicePointsResponse);

        mockMvc.perform(builder)
            .andExpect(result);

        restServer.verify();
    }

    private void performPatronGetWithMockMVC(String url, String endpoint, PathParametersSnippet pathParameters,
            RequestParametersSnippet requestParameters, ResponseFieldsSnippet responseFields, String content) throws Exception {
        expectGetResponse(url, once(), respondJsonSuccess(patronAccountResource));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + endpoint, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(content))
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

    private static Stream<? extends Arguments> argumentsEndpointResponses() throws Exception {
        return Stream.of(
            streamOfFines(),
            streamOfHoldsCancel()
        )
        .flatMap(stream -> stream);
    }

    private static Stream<? extends Arguments> argumentsLoansResponses() throws Exception {
        return Stream.of(
            streamOfLoans(),
            streamOfLoanItems()
        )
        .flatMap(stream -> stream);
    }

    private static Stream<? extends Arguments> argumentsHoldsResponses() throws Exception {
        final MockHttpServletRequestBuilder holds = get(PATRON_MVC_PREFIX + HOLDS_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        final MockHttpServletRequestBuilder holdsCatalog = get(PATRON_MVC_PREFIX + HOLDS_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        return Stream.of(
            Arguments.of(holds, getHoldsUrl(), never(), once(), never(), never(),
                withStatus(NOT_FOUND), withNoContent(),
                withNoContent(), status().isNotFound()),
            Arguments.of(holds, getHoldsUrl(), never(), once(), never(), never(),
                withStatus(BAD_REQUEST), withNoContent(),
                withNoContent(), status().isBadRequest()),
            Arguments.of(holds, getHoldsUrl(), never(), once(), never(), never(),
                withStatus(INTERNAL_SERVER_ERROR), withNoContent(),
                withNoContent(), status().isInternalServerError()),
            Arguments.of(holds, getHoldsUrl(), once(), once(), once(), never(),
                respondJsonSuccess(patronAccountResource), withStatus(BAD_REQUEST),
                withNoContent(), status().isBadRequest()),
            Arguments.of(holds, getHoldsUrl(), once(), once(), once(), never(),
                respondJsonSuccess(patronAccountResource), withStatus(INTERNAL_SERVER_ERROR),
                withNoContent(), status().isInternalServerError()),
            Arguments.of(holdsCatalog, getHoldsUrl(), never(), never(), never(), never(),
                withNoContent(), withNoContent(),
                withNoContent(), status().isBadRequest()),
            Arguments.of(holds, getHoldsUrl(), between(0, 1), once(), between(0, 1), between(0, 1),
                respondJsonSuccess(patronAccountDateParseErrorResource), respondJsonSuccess(holdRequestResource),
                respondJsonSuccess(servicePointResource), status().isInternalServerError()));
    }

    private static Stream<? extends Arguments> streamOfFines() throws Exception {
        final MockHttpServletRequestBuilder fines = get(PATRON_MVC_PREFIX + FINES_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        final MockHttpServletRequestBuilder finesCatalog = get(PATRON_MVC_PREFIX + FINES_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        return Stream.of(
            Arguments.of(fines, getFinesUrl(), GET, once(), withStatus(NOT_FOUND), status().isNotFound()),
            Arguments.of(fines, getFinesUrl(), GET, once(), withStatus(BAD_REQUEST), status().isBadRequest()),
            Arguments.of(fines, getFinesUrl(), GET, once(), withStatus(INTERNAL_SERVER_ERROR), status().isInternalServerError()),
            Arguments.of(finesCatalog, getFinesUrl(), GET, never(), withStatus(OK), status().isBadRequest()),
            Arguments.of(fines, getFinesUrl(), GET, once(), respondJsonSuccess(patronAccountDateParseErrorResource), status().isInternalServerError())
        );
    }

    private static Stream<? extends Arguments> streamOfLoans() throws Exception {
        final MockHttpServletRequestBuilder loans = get(PATRON_MVC_PREFIX + LOANS_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        final MockHttpServletRequestBuilder loansCatalog = get(PATRON_MVC_PREFIX + LOANS_ENDPOINT, UIN)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        return Stream.of(
            Arguments.of(loans, getLoansUrl(), GET, once(), withStatus(NOT_FOUND), status().isNotFound()),
            Arguments.of(loans, getLoansUrl(), GET, once(), withStatus(BAD_REQUEST), status().isBadRequest()),
            Arguments.of(loans, getLoansUrl(), GET, once(), withStatus(INTERNAL_SERVER_ERROR), status().isInternalServerError()),
            Arguments.of(loansCatalog, getLoansUrl(), GET, never(), withStatus(OK), status().isBadRequest()),
            Arguments.of(loans, getLoansUrl(), GET, once(), respondJsonSuccess(patronAccountDateParseErrorResource), status().isInternalServerError())
        );
    }

    private static Stream<? extends Arguments> streamOfLoanItems() {
        final MockHttpServletRequestBuilder loanItems = post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        final MockHttpServletRequestBuilder loanItemsCatalog = post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        return Stream.of(
            Arguments.of(loanItems, getLoanItemRenewalUrl(), POST, once(), withStatus(NOT_FOUND), status().isNotFound()),
            Arguments.of(loanItems, getLoanItemRenewalUrl(), POST, once(), withStatus(BAD_REQUEST), status().isBadRequest()),
            Arguments.of(loanItems, getLoanItemRenewalUrl(), POST, once(), withStatus(INTERNAL_SERVER_ERROR), status().isInternalServerError()),
            Arguments.of(loanItemsCatalog, getLoanItemRenewalUrl(), POST, never(), withStatus(OK), status().isBadRequest())
        );
    }

    private static Stream<? extends Arguments> streamOfHoldsCancel() {
        final MockHttpServletRequestBuilder holdsCancel = post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        final MockHttpServletRequestBuilder holdsCancelCatalog = post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML);

        return Stream.of(
            Arguments.of(holdsCancel, getCancelHoldRequestUrl(), POST, once(), withStatus(NOT_FOUND), status().isNotFound()),
            Arguments.of(holdsCancel, getCancelHoldRequestUrl(), POST, once(), withStatus(BAD_REQUEST), status().isBadRequest()),
            Arguments.of(holdsCancel, getCancelHoldRequestUrl(), POST, once(), withStatus(INTERNAL_SERVER_ERROR), status().isInternalServerError()),
            Arguments.of(holdsCancelCatalog, getCancelHoldRequestUrl(), POST, never(), withStatus(OK), status().isBadRequest()));
    }

    private static String getFinesUrl() {
        return getAccountUrl(false, true, false);
    }

    private static String getLoansUrl() {
        return getAccountUrl(true, false, false);
    }

    private static String getHoldsUrl() {
        return getAccountUrl(false, false, true);
    }

    private static String getAccountUrl(boolean loans, boolean charges, boolean holds) {
        return String.format("%spatron/account/%s?apikey=%s&includeLoans=%s&includeCharges=%s&includeHolds=%s",
            BASE_PATH, UIN, API_KEY, Boolean.toString(loans), Boolean.toString(charges), Boolean.toString(holds));
    }

    private static String getOkapiServicePointsUrl() {
        return getOkapiUrl(String.format("service-points/%s", SERVICE_POINTS_ID));
    }

    private static String getOkapiRequestsUrl() {
        return getOkapiUrl(String.format("circulation/requests/%s", REQUEST_ID));
    }

    private static String getOkapiInstancesUrl(String instanceId) {
        return getOkapiUrl(String.format("instance-storage/instances/%s", instanceId));
    }

    private static String getCancelHoldRequestUrl() {
        return String.format("%spatron/account/%s/holds/%s/cancel?apikey=%s", BASE_PATH, UIN, REQUEST_ID, API_KEY);
    }

    private static String getLoanItemRenewalUrl() {
        return String.format("%spatron/account/%s/item/%s/renew?apikey=%s", BASE_PATH, UIN, ITEM_ID, API_KEY);
    }

    @Value("classpath:mock/response/patron/account.json")
    public void setPatronAccountResource(Resource resource) {
        patronAccountResource = resource;
    }

    @Value("classpath:mock/response/patron/accountDateParseError.json")
    public void setPatronAccountDateParseErrorResource(Resource resource) {
        patronAccountDateParseErrorResource = resource;
    }

    @Value("classpath:mock/response/patron/accountRenewableLoanItem.json")
    public void setPatronAccountRenewalResource(Resource resource) {
        patronAccountRenewalResource = resource;
    }

    @Value("classpath:mock/response/patron/accountCancelHoldResponse.json")
    public void setPatronAccountCancelHoldResponseResource(Resource resource) {
        patronAccountCancelHoldResponseResource = resource;
    }

    @Value("classpath:mock/response/request/holdRequest.json")
    public void setHoldRequestResource(Resource resource) {
        holdRequestResource = resource;
    }

    @Value("classpath:mock/response/service-point/servicePoint.json")
    public void setServicePointResource(Resource resource) {
        servicePointResource = resource;
    }

    @Value("classpath:mock/response/instances/in1.json")
    public void setInstance1Resource(Resource resource) {
        instance1Resource = resource;
    }

    @Value("classpath:mock/response/instances/in2.json")
    public void setInstance2Resource(Resource resource) {
        instance2Resource = resource;
    }

    @Value("classpath:mock/response/instances/in3.json")
    public void setInstance3Resource(Resource resource) {
        instance3Resource = resource;
    }

    @Value("classpath:mock/response/instances/in4.json")
    public void setInstance4Resource(Resource resource) {
        instance4Resource = resource;
    }

    @Value("classpath:mock/response/catalog/fines.json")
    public void setFinesCatalogResource(Resource resource) {
        finesCatalogResource = resource;
    }

    @Value("classpath:mock/response/catalog/loans.json")
    public void setLoansCatalogResource(Resource resource) {
        loansCatalogResource = resource;
    }

    @Value("classpath:mock/response/catalog/loanRenewal.json")
    public void setLoanRenewalCatalogResource(Resource resource) {
        loanRenewalCatalogResource = resource;
    }

    @Value("classpath:mock/response/catalog/requests.json")
    public void setRequestsCatalogResource(Resource resource) {
        requestsCatalogResource = resource;
    }

}
