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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
    private static final String USER_ID = "93710b5b-aa9a-43be-af34-7dcb1f7b0669";

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

    private static String patronAccountPayload;
    private static String patronAccountDateParseErrorPayload;
    private static String patronAccountRenewalPayload;
    private static String patronAccountCancelHoldResponsePayload;
    private static String holdRequestPayload;
    private static String servicePointPayload;
    private static String blUserResponsePayload;
    private static String blUserBadUUIDErrorPayload;
    private static String blUserDuplicateErrorPayload;
    private static String blUserEmptyErrorPayload;
    private static String automatedBlocksResponsePayload;
    private static String instance1Payload;
    private static String instance2Payload;
    private static String instance3Payload;
    private static String instance4Payload;

    private static String finesCatalogPayload;
    private static String loansCatalogPayload;
    private static String loanRenewalCatalogPayload;
    private static String requestsCatalogPayload;
    private static String blockCatalogPayload;
    private static String blockEmptyCatalogPayload;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

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
        instance2Payload = loadPayload("mock/response/instances/in2.json");
        instance3Payload = loadPayload("mock/response/instances/in3.json");
        instance4Payload = loadPayload("mock/response/instances/in4.json");

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

    @Test
    public void testFinesMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN."));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].fineId").description("The Fine UUID."),
            fieldWithPath("[].itemId").description("The Item UUID."),
            fieldWithPath("[].instanceId").description("The Instance UUID."),
            fieldWithPath("[].amount").description("The title of the Item associated with the fine."),
            fieldWithPath("[].fineType").description("The type of the Fine."),
            fieldWithPath("[].fineDate")
                .description("A timestamp in milliseconds from UNIX epoch representing the date the Fine was accrued."),
            fieldWithPath("[].itemTitle").description("The title of the Item associated with the Fine."));

        performPatronGetWithMockMVC(getFinesUrl(), FINES_ENDPOINT, pathParameters,
            requestParameters, responseFields, finesCatalogPayload);
    }

    @Test
    public void testLoansMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN."));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].loanId").description("The Loan UUID."),
            fieldWithPath("[].itemId").description("The Item UUID."),
            fieldWithPath("[].instanceId").description("The Instance UUID."),
            fieldWithPath("[].instanceHrid").description("The instance human-readable id."),
            fieldWithPath("[].loanDate").description("The Loan date."),
            fieldWithPath("[].loanDueDate").description("The Loan due date."),
            fieldWithPath("[].overdue").description("Is the Loan overdue."),
            fieldWithPath("[].title").description("The title of the Loan Item."),
            fieldWithPath("[].author").description("The author of the Loan Item."));

        expectGetResponse(getLoansUrl(), once(), respondJsonOk(patronAccountPayload));
        expectOkapiLoginResponse(between(0, 4), withStatus(CREATED));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID1), once(), respondJsonOk(instance1Payload));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID2), once(), respondJsonOk(instance2Payload));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID3), once(), respondJsonOk(instance3Payload));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID4), once(), respondJsonOk(instance4Payload));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + LOANS_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(loansCatalogPayload))
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
            parameterWithName("itemId").description("The UUID of the Loan Item."));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("loanId").description("The Loan UUID."),
            fieldWithPath("itemId").description("The Item UUID."),
            fieldWithPath("instanceId").description("The Instance UUID."),
            fieldWithPath("instanceHrid").description("The instance human-readable id."),
            fieldWithPath("loanDate").description("The Loan date."),
            fieldWithPath("loanDueDate").description("The Loan due date."),
            fieldWithPath("overdue").description("Is the Loan overdue."),
            fieldWithPath("title").description("The title of the Loan Item."),
            fieldWithPath("author").description("The author of the Loan Item."));

        expectPostResponse(getLoanItemRenewalUrl(), once(), respondJsonOk(patronAccountRenewalPayload));
        expectOkapiLoginResponse(between(0, 1), withStatus(CREATED));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID1), once(), respondJsonOk(instance1Payload));

        mockMvc.perform(
            post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
                .content(loanRenewalCatalogPayload))
            .andExpect(status().isOk())
            .andDo(
                document(
                    DOC_PREFIX + RENEWAL_ENDPOINT,
                    pathParameters,
                    requestParameters,
                    responseFields));

        restServer.verify();
    }

    @Test
    public void testHoldRequestsMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN."));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].requestId").description("The Hold Request UUID."),
            fieldWithPath("[].itemId").description("The Item UUID."),
            fieldWithPath("[].instanceId").description("The Instance UUID."),
            fieldWithPath("[].requestType").description("The type of the Hold Request."),
            fieldWithPath("[].itemTitle").description("The title of the Item associated with the Hold Request."),
            fieldWithPath("[].statusText").description("A descriptive status of the Hold Request."),
            fieldWithPath("[].pickupServicePoint").description("A title describing the pickup service point location."),
            fieldWithPath("[].queuePosition").description("The position within the queue."),
            fieldWithPath("[].expirationDate")
                .description("A timestamp in milliseconds from UNIX epoch representing the date the hold request will expire."));

        expectGetResponse(getHoldsUrl(), once(), respondJsonOk(patronAccountPayload));
        expectOkapiLoginResponse(once(), withStatus(CREATED));
        expectGetResponse(getOkapiRequestsUrl(), once(), respondJsonOk(holdRequestPayload));
        expectGetResponse(getOkapiServicePointsUrl(), once(), respondJsonOk(servicePointPayload));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + HOLDS_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(requestsCatalogPayload))
            .andDo(
                document(
                    DOC_PREFIX + HOLDS_ENDPOINT,
                    pathParameters,
                    requestParameters,
                    responseFields));

        restServer.verify();
    }

    @Test
    public void testCancelHoldRequestMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN."),
            parameterWithName("requestId").description("The Hold Request UUID."));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional());

        expectPostResponse(getCancelHoldRequestUrl(), once(),
            respondJsonCreated(patronAccountCancelHoldResponsePayload));

        mockMvc.perform(
            post(HOLDS_CANCEL_MVC_PATH, UIN, REQUEST_ID)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
            .andDo(
                document(
                    DOC_PREFIX + "holds/cancel",
                    pathParameters,
                    requestParameters));

        restServer.verify();
    }

    @Test
    public void testBlockMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description("The patron UIN."));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description("The name of the catalog to use.").optional());

        expectOkapiLoginResponse(once(), withStatus(CREATED));
        expectGetResponse(getOkapiBLUsersByUinUrl(), once(), respondJsonOk(blUserResponsePayload));
        expectGetResponse(getOkapiAutomatedBlocksUrl(USER_ID), once(), respondJsonOk(automatedBlocksResponsePayload));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + BLOCK_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.TEXT_PLAIN)
                .characterEncoding(CHARSET)
                .accept(MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string(blockCatalogPayload))
            .andDo(
                document(
                    DOC_PREFIX + BLOCK_ENDPOINT,
                    pathParameters,
                    requestParameters));
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
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID1), between(0, 1), respondJsonOk(instance1Payload));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID2), between(0, 1), respondJsonOk(instance2Payload));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID3), between(0, 1), respondJsonOk(instance3Payload));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID4), between(0, 1), respondJsonOk(instance4Payload));

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

    @ParameterizedTest
    @MethodSource("argumentsBlockResponses")
    public void testBlockEndpoint(MockHttpServletRequestBuilder builder, ExpectedCount okapiCount,
            ExpectedCount blUsersCount, ExpectedCount automatedBlocksCount, DefaultResponseCreator blUsersResponse,
            DefaultResponseCreator automatedBlocksResponse, String userId, ResultMatcher result) throws Exception {

        expectOkapiLoginResponse(okapiCount, withStatus(CREATED));
        expectGetResponse(getOkapiBLUsersByUinUrl(), blUsersCount, blUsersResponse);
        expectGetResponse(getOkapiAutomatedBlocksUrl(userId), automatedBlocksCount, automatedBlocksResponse);

        mockMvc.perform(builder)
            .andExpect(result);

        restServer.verify();
    }

    private void performPatronGetWithMockMVC(String url, String endpoint, PathParametersSnippet pathParameters,
            RequestParametersSnippet requestParameters, ResponseFieldsSnippet responseFields, String content)
            throws Exception {

        expectGetResponse(url, once(), respondJsonOk(patronAccountPayload));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + endpoint, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(content))
            .andDo(
                document(
                    DOC_PREFIX + endpoint,
                    pathParameters,
                    requestParameters,
                    responseFields));

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
                respondJsonOk(patronAccountPayload), withStatus(BAD_REQUEST),
                withNoContent(), status().isBadRequest()),
            Arguments.of(holds, getHoldsUrl(), once(), once(), once(), never(),
                respondJsonOk(patronAccountPayload), withStatus(INTERNAL_SERVER_ERROR),
                withNoContent(), status().isInternalServerError()),
            Arguments.of(holdsCatalog, getHoldsUrl(), never(), never(), never(), never(),
                withNoContent(), withNoContent(),
                withNoContent(), status().isBadRequest()),
            Arguments.of(holds, getHoldsUrl(), between(0, 1), once(), between(0, 1), between(0, 1),
                respondJsonOk(patronAccountDateParseErrorPayload), respondJsonOk(holdRequestPayload),
                respondJsonOk(servicePointPayload), status().isInternalServerError()));
    }

    private static Stream<? extends Arguments> argumentsBlockResponses() throws Exception {
        final MockHttpServletRequestBuilder block = get(PATRON_MVC_PREFIX + BLOCK_ENDPOINT, UIN)
            .contentType(MediaType.TEXT_PLAIN)
            .characterEncoding(CHARSET);

        final MockHttpServletRequestBuilder blockCatalog = get(PATRON_MVC_PREFIX + BLOCK_ENDPOINT, UIN)
            .contentType(MediaType.TEXT_PLAIN)
            .characterEncoding(CHARSET)
            .param(CATALOG_FIELD, VOYAGER_CATALOG);

        return Stream.of(
            Arguments.of(block, once(), once(), never(), withStatus(NOT_FOUND), withNoContent(), USER_ID,
                status().isNotFound()),
            Arguments.of(block, once(), once(), never(), withStatus(BAD_REQUEST), withNoContent(), USER_ID,
                status().isBadRequest()),
            Arguments.of(block, once(), once(), never(), withStatus(INTERNAL_SERVER_ERROR), withNoContent(), USER_ID,
                status().isInternalServerError()),
            Arguments.of(block, once(), once(), once(), respondJsonOk(blUserResponsePayload),
                withStatus(NOT_FOUND), USER_ID, status().isNotFound()),
            Arguments.of(block, once(), once(), once(), respondJsonOk(blUserResponsePayload),
                withStatus(BAD_REQUEST), USER_ID, status().isBadRequest()),
            Arguments.of(block, once(), once(), once(), respondJsonOk(blUserResponsePayload),
                withStatus(INTERNAL_SERVER_ERROR), USER_ID, status().isInternalServerError()),
            Arguments.of(blockCatalog, never(), never(), never(), withNoContent(), withNoContent(), USER_ID,
                status().isBadRequest()),
            Arguments.of(block, between(0, 1), once(), once(), respondJsonOk(blUserBadUUIDErrorPayload),
                withStatus(BAD_REQUEST), "Bad%20UUID", status().isBadRequest()),
            Arguments.of(block, between(0, 1), once(), never(), respondJsonOk(blockEmptyCatalogPayload),
                withNoContent(), USER_ID, status().isNotFound()),
            Arguments.of(block, between(0, 1), once(), between(0, 1), respondJsonOk(blUserDuplicateErrorPayload),
                respondJsonOk(automatedBlocksResponsePayload), USER_ID, status().isInternalServerError()),
            Arguments.of(block, between(0, 1), once(), between(0, 1), respondJsonOk(blUserEmptyErrorPayload),
                respondJsonOk(automatedBlocksResponsePayload), USER_ID, status().isNotFound()));
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
            Arguments.of(fines, getFinesUrl(), GET, once(), withStatus(INTERNAL_SERVER_ERROR),
                status().isInternalServerError()),
            Arguments.of(finesCatalog, getFinesUrl(), GET, never(), withStatus(OK), status().isBadRequest()),
            Arguments.of(fines, getFinesUrl(), GET, once(), respondJsonOk(patronAccountDateParseErrorPayload),
                status().isInternalServerError()));
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
            Arguments.of(loans, getLoansUrl(), GET, once(), withStatus(INTERNAL_SERVER_ERROR),
                status().isInternalServerError()),
            Arguments.of(loansCatalog, getLoansUrl(), GET, never(), withStatus(OK), status().isBadRequest()),
            Arguments.of(loans, getLoansUrl(), GET, once(), respondJsonOk(patronAccountDateParseErrorPayload),
                status().isInternalServerError()));
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
            Arguments.of(loanItems, getLoanItemRenewalUrl(), POST, once(), withStatus(NOT_FOUND),
                status().isNotFound()),
            Arguments.of(loanItems, getLoanItemRenewalUrl(), POST, once(), withStatus(BAD_REQUEST),
                status().isBadRequest()),
            Arguments.of(loanItems, getLoanItemRenewalUrl(), POST, once(), withStatus(INTERNAL_SERVER_ERROR),
                status().isInternalServerError()),
            Arguments.of(loanItemsCatalog, getLoanItemRenewalUrl(), POST, never(), withStatus(OK),
                status().isBadRequest()));
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
            Arguments.of(holdsCancel, getCancelHoldRequestUrl(), POST, once(), withStatus(NOT_FOUND),
                status().isNotFound()),
            Arguments.of(holdsCancel, getCancelHoldRequestUrl(), POST, once(), withStatus(BAD_REQUEST),
                status().isBadRequest()),
            Arguments.of(holdsCancel, getCancelHoldRequestUrl(), POST, once(), withStatus(INTERNAL_SERVER_ERROR),
                status().isInternalServerError()),
            Arguments.of(holdsCancelCatalog, getCancelHoldRequestUrl(), POST, never(), withStatus(OK),
                status().isBadRequest()));
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

    private static String getOkapiBLUsersByUinUrl() {
        return getOkapiUrl(String.format("bl-users?query=(externalSystemId%%3D%%3D%%22%s%%22)&limit=2", UIN));
    }

    private static String getOkapiAutomatedBlocksUrl(String userId) {
        return getOkapiUrl(String.format("automated-patron-blocks/%s", userId));
    }

    private static String getCancelHoldRequestUrl() {
        return String.format("%spatron/account/%s/holds/%s/cancel?apikey=%s", BASE_PATH, UIN, REQUEST_ID, API_KEY);
    }

    private static String getLoanItemRenewalUrl() {
        return String.format("%spatron/account/%s/item/%s/renew?apikey=%s", BASE_PATH, UIN, ITEM_ID, API_KEY);
    }

    private static String loadPayload(String path) throws IOException {
        return loadResource(PatronControllerTest.class.getClassLoader().getResource(path));
    }
}
