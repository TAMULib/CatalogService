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

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import edu.tamu.catalog.config.CatalogServiceConfig;
import edu.tamu.catalog.config.RestConfig;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = PatronController.class, secure = false)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@Import({ RestConfig.class, CatalogServiceConfig.class })
public class PatronControllerTest extends PatronControllerTestBase {

    @Test
    public void testFinesMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description(descUIN()));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description(descCatalogName(FOLIO_CATALOG)).optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].fineId").description(descId("*Fee* or *Fine*")),
            fieldWithPath("[].itemId").description(descItemId("*Fee* or *Fine*")),
            fieldWithPath("[].instanceId").description(descInstanceId("*Fee* or *Fine*")),
            fieldWithPath("[].amount").description(descAmount(true)),
            fieldWithPath("[].fineType").description(descType("*Fee* or *Fine*")),
            fieldWithPath("[].fineDate").description(descTimestamp("the *Fee* or *Fine* was accrued")),
            fieldWithPath("[].itemTitle").description(descItemTitle("*Fee* or *Fine*")));

        performPatronGetWithMockMVC(getFinesUrl(), FINES_ENDPOINT, pathParameters,
            requestParameters, responseFields, finesCatalogPayload);
    }

    @Test
    public void testLoansMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description(descUIN()));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description(descCatalogName(FOLIO_CATALOG)).optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].loanId").description(descId("*Loan*")),
            fieldWithPath("[].itemId").description(descItemId("*Loan*")),
            fieldWithPath("[].instanceId").description(descInstanceId("*Loan*")),
            fieldWithPath("[].instanceHrid").description(descInstanceHrid("*Loan*")),
            fieldWithPath("[].loanDate").description(descTimestamp("the *Loan* was created")),
            fieldWithPath("[].loanDueDate").description(descTimestamp("the *Loan* is due")),
            fieldWithPath("[].overdue").description(descBoolean("*Loan* is overdue")),
            fieldWithPath("[].title").description(descField("*Loan*", "title")),
            fieldWithPath("[].author").description(descField("*Loan*", "author")));

        expectGetResponse(getLoansUrl(), once(), respondJsonOk(patronAccountPayload));
        expectOkapiLoginResponse(between(0, 1), withStatus(CREATED));
        expectGetResponse(getOkapiBatchInstancesUrl(INSTANCES_TOTAL), once(), respondJsonOk(instancesPayload), true);

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
    public void testLoanRenewalMockMVC() throws Exception {
        PathParametersSnippet pathParameters = pathParameters(
            parameterWithName(UIN_FIELD).description(descUIN()),
            parameterWithName("itemId").description(descItemId("*Loan*")));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description(descCatalogName(FOLIO_CATALOG)).optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("loanId").description(descId("*Loan*")),
            fieldWithPath("itemId").description(descItemId("*Loan*")),
            fieldWithPath("instanceId").description(descInstanceId("*Loan*")),
            fieldWithPath("instanceHrid").description(descInstanceHrid("*Loan*")),
            fieldWithPath("loanDate").description(descTimestamp("the *Loan* was created")),
            fieldWithPath("loanDueDate").description(descTimestamp("the *Loan* is due")),
            fieldWithPath("overdue").description(descBoolean("*Loan* is overdue")),
            fieldWithPath("title").description(descField("*Loan*", "title")),
            fieldWithPath("author").description(descField("*Loan*", "author")));

        expectPostResponse(getLoanRenewalUrl(), once(), respondJsonOk(patronAccountRenewalPayload));
        expectOkapiLoginResponse(between(0, 1), withStatus(CREATED));
        expectGetResponse(getOkapiInstancesUrl(INSTANCE_ID1), between(0, 1), respondJsonOk(instance1Payload));

        mockMvc.perform(
            post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
                .content(loanRenewalCatalogPayload)
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
            parameterWithName(UIN_FIELD).description(descUIN()));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description(descCatalogName(FOLIO_CATALOG)).optional());

        ResponseFieldsSnippet responseFields = responseFields(
            fieldWithPath("[].requestId").description(descId("*Hold Request*")),
            fieldWithPath("[].itemId").description(descItemId("*Hold Request*")),
            fieldWithPath("[].instanceId").description(descInstanceId("*Hold Request*")),
            fieldWithPath("[].requestType").description(descType("*Hold Request*")),
            fieldWithPath("[].itemTitle").description(descItemTitle("*Hold Request*")),
            fieldWithPath("[].statusText").description(descField("*Hold Request*", "descriptive status")),
            fieldWithPath("[].pickupServicePoint").description(descPickupServicePoint()),
            fieldWithPath("[].queuePosition").description(descQueuePosition()),
            fieldWithPath("[].expirationDate").description(descTimestamp("the *Hold Request* will expire")));

        expectGetResponse(getHoldsUrl(), once(), respondJsonOk(patronAccountPayload));
        expectOkapiLoginResponse(once(), withStatus(CREATED));
        expectGetResponse(getOkapiRequestsUrl(), once(), respondJsonOk(holdRequestPayload));
        expectGetResponse(getOkapiServicePointsUrl(), once(), respondJsonOk(servicePointPayload));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + HOLDS_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(requestsCatalogPayload))
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
            parameterWithName(UIN_FIELD).description(descUIN()),
            parameterWithName("requestId").description(descId("*Hold Request*")));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description(descCatalogName(FOLIO_CATALOG)).optional());

        expectPostResponse(getCancelHoldRequestUrl(), once(),
            respondJsonCreated(patronAccountCancelHoldResponsePayload));

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
            parameterWithName(UIN_FIELD).description(descUIN()));

        RequestParametersSnippet requestParameters = requestParameters(
            parameterWithName(CATALOG_FIELD).description(descCatalogName(FOLIO_CATALOG)).optional());

        expectOkapiLoginResponse(once(), withStatus(CREATED));
        expectGetResponse(getOkapiBLUsersByUinUrl(), once(), respondJsonOk(blUserResponsePayload));
        expectGetResponse(getOkapiAutomatedBlocksUrl(USER_ID), once(), respondJsonOk(automatedBlocksResponsePayload));

        mockMvc.perform(
            get(PATRON_MVC_PREFIX + BLOCK_ENDPOINT, UIN)
                .param(CATALOG_FIELD, FOLIO_CATALOG)
                .contentType(MediaType.TEXT_PLAIN)
                .characterEncoding(CHARSET)
                .accept(MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string(blockCatalogPayload))
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

        expectResponse(url, method, count, response, false);

        mockMvc.perform(builder)
            .andExpect(result);

        restServer.verify();
    }

    @ParameterizedTest
    @MethodSource("argumentsLoansResponses")
    public void testLoansEndpoints(MockHttpServletRequestBuilder builder, String url, HttpMethod method,
            ExpectedCount count, DefaultResponseCreator response, ResultMatcher result) throws Exception {

        expectResponse(url, method, count, response, false);
        expectOkapiLoginResponse(between(0, 1), withStatus(CREATED));
        expectGetResponse(getOkapiBatchInstancesUrl(1), between(0, 1), respondJsonOk(instancesPayload), true);
        expectGetResponse(getOkapiBatchInstancesUrl(INSTANCES_TOTAL), between(0, 1), respondJsonOk(instancesPayload), true);

        mockMvc.perform(builder)
            .andExpect(result);

        restServer.verify();
    }

    @ParameterizedTest
    @MethodSource("argumentsLoanRenewalResponses")
    public void testLoanRenewalEndpoints(MockHttpServletRequestBuilder builder, String url, HttpMethod method,
            ExpectedCount count, DefaultResponseCreator response, ResultMatcher result) throws Exception {

        expectResponse(url, method, count, response, false);
        expectOkapiLoginResponse(between(0, 1), withStatus(CREATED));
        expectGetResponse(getOkapiBatchInstancesUrl(1), between(0, 4), respondJsonOk(instancesPayload), true);

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
                withNoContent(), USER_ID, status().isInternalServerError()),
            Arguments.of(block, between(0, 1), once(), between(0, 1), respondJsonOk(blUserDuplicateErrorPayload),
                respondJsonOk(automatedBlocksResponsePayload), USER_ID, status().isInternalServerError()),
            Arguments.of(block, between(0, 1), once(), between(0, 1), respondJsonOk(blUserEmptyErrorPayload),
                respondJsonOk(automatedBlocksResponsePayload), USER_ID, status().isInternalServerError()));
    }

    private static Stream<? extends Arguments> argumentsLoansResponses() throws Exception {
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

    private static Stream<? extends Arguments> argumentsLoanRenewalResponses() {
        final MockHttpServletRequestBuilder loans = post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            .content(loanRenewalCatalogPayload);

        final MockHttpServletRequestBuilder loansCatalog = post(PATRON_MVC_PREFIX + RENEW_MVC_PATH, UIN, ITEM_ID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param(CATALOG_FIELD, VOYAGER_CATALOG)
            .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_HTML)
            .content(loanRenewalCatalogPayload);

        return Stream.of(
            Arguments.of(loans, getLoanRenewalUrl(), POST, once(), withStatus(NOT_FOUND), status().isNotFound()),
            Arguments.of(loans, getLoanRenewalUrl(), POST, once(), withStatus(BAD_REQUEST),
                status().isBadRequest()),
            Arguments.of(loans, getLoanRenewalUrl(), POST, once(), withStatus(INTERNAL_SERVER_ERROR),
                status().isInternalServerError()),
            Arguments.of(loansCatalog, getLoanRenewalUrl(), POST, never(), withStatus(OK), status().isBadRequest()));
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

}
