package edu.tamu.catalog.controller;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.tamu.catalog.service.CatalogServiceFactory;
import edu.tamu.catalog.service.FolioCatalogService;
import edu.tamu.catalog.service.VoyagerCatalogService;

@WebMvcTest(value = PatronController.class, secure = false)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@RunWith(SpringRunner.class)
public class PatronControllerTest {

    private static final String UIN = "1234567890";
    private static final String API_KEY = "mocked_key";
    private static final String ITEM_ID = "40053ccb-fd0c-304b-9547-b2fc06f34d3e";
    private static final String FOLIO_CATALOG = "folio";
    private static final String VOYAGER_CATALOG = "voyager";
    private static final String LOANS_ENDPOINT = "loans";
    private static final String FINES_ENDPOINT = "fines";
    private static final String RENEWAL_ENDPOINT = "renew";
    private static final String DOC_PREFIX = "patron/";
    private static final String RENEW_MVC_PATH = RENEWAL_ENDPOINT+"/{itemId}";


    @Value("classpath:mock/patron/account.json")
    private Resource patronAccountResource;

    @Value("classpath:mock/patron/accountRenewableLoanItem.json")
    private Resource patronAccountRenewalResource;

    @Value("classpath:mock/patron/accountDateParseError.json")
    private Resource patronAccountDateParseErrorResource;

    @Autowired
    private MockMvc mockMvc;

    private RestTemplate restTemplate;

    @MockBean
    private CatalogServiceFactory catalogServiceFactory;

    private FolioCatalogService folioCatalogService;

    private VoyagerCatalogService voyagerCatalogService;

    private MockRestServiceServer restServer;

    private String basePath;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        restTemplate = new RestTemplate();
        folioCatalogService = new FolioCatalogService(restTemplate);
        voyagerCatalogService = new VoyagerCatalogService();
        restServer = MockRestServiceServer.createServer(restTemplate);

        Map<String, String> authentication = new HashMap<>();
        authentication.put("apiKey", API_KEY);

        folioCatalogService.setAuthentication(authentication);
        folioCatalogService.setHost("localhost");
        folioCatalogService.setProtocol("http");
        folioCatalogService.setType("folio");

        basePath = String.format("%s://%s/patron/", folioCatalogService.getProtocol(), folioCatalogService.getHost());

        when(catalogServiceFactory.getOrCreateCatalogService(FOLIO_CATALOG)).thenReturn(folioCatalogService);
        when(catalogServiceFactory.getOrCreateCatalogService(VOYAGER_CATALOG)).thenReturn(voyagerCatalogService);
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
            fieldWithPath("id").description("The patron UIN."),
            fieldWithPath("total").description("The sum total of the fines."),
            fieldWithPath("fineCount").description("The total number of fines in the list."),
            fieldWithPath("list[]").description("An array of all fines for the patron."),
            fieldWithPath("list[].amount").description("The title of the item associated with the fine."),
            fieldWithPath("list[].fineId").description("The UUID associated with the fine."),
            fieldWithPath("list[].fineType").description("The type of the fine."),
            fieldWithPath("list[].fineDate").description("A timestamp in milliseconds from UNIX epoch representing the date the fine was accrued."),
            fieldWithPath("list[].itemTitle").description("The title of the item associated with the fine.")
        );
        getEndpointWithMockMVC(getFinesUrl(), FINES_ENDPOINT, pathParameters, requestParameters, responseFields);
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
        getEndpointWithMockMVC(getLoansUrl(), LOANS_ENDPOINT, pathParameters, requestParameters, responseFields);
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

        expectPostResponse(getRenewalUrl(), once(), okResponse(patronAccountRenewalResource));
        mockMvc.perform(
            post("/patron/{uin}/" + RENEW_MVC_PATH, UIN, ITEM_ID)
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
    public void testFinesUINNotFound() throws Exception {
        getEndpointNotFound(getFinesUrl(), FINES_ENDPOINT);
    }

    @Test
    public void testLoansUINNotFound() throws Exception {
        getEndpointNotFound(getLoansUrl(), LOANS_ENDPOINT);
    }

    @Test
    public void testFinesDateParseError() throws Exception  {
        getEndpointDateParseError(getFinesUrl(), FINES_ENDPOINT);
    }

    @Test
    public void testLoansDateParseError() throws Exception  {
        getEndpointDateParseError(getLoansUrl(), LOANS_ENDPOINT);
    }

    @Test
    public void testFinesNotSupportedForCatalog() throws Exception  {
        getEndpointNotSupportedForCatalog(getFinesUrl(), FINES_ENDPOINT);
    }

    @Test
    public void testLoansNotSupportedForCatalog() throws Exception  {
        getEndpointNotSupportedForCatalog(getLoansUrl(), LOANS_ENDPOINT);
    }

    @Test
    public void testFinesClientException() throws Exception {
        getEndpointBadRequest(getFinesUrl(), FINES_ENDPOINT);
    }

    @Test
    public void testLoansClientException() throws Exception {
        getEndpointBadRequest(getLoansUrl(), LOANS_ENDPOINT);
    }

    @Test
    public void testFinesServerException() throws Exception {
        getEndpointInternalServerError(getFinesUrl(), FINES_ENDPOINT);
    }

    @Test
    public void testLoansServerException() throws Exception {
        getEndpointInternalServerError(getLoansUrl(), LOANS_ENDPOINT);
    }

    private void getEndpointWithMockMVC(String sourceUrl, String catalogEndpoint, PathParametersSnippet pathParameters, RequestParametersSnippet requestParameters, ResponseFieldsSnippet responseFields) throws Exception {
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

    private void getEndpointInternalServerError(String sourceUrl, String catalogEndpoint) throws Exception {
        performGet(sourceUrl, catalogEndpoint, once(), withStatus(HttpStatus.INTERNAL_SERVER_ERROR))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    private void getEndpointBadRequest(String sourceUrl, String catalogEndpoint) throws Exception  {
        performGet(sourceUrl, catalogEndpoint, once(), withStatus(HttpStatus.BAD_REQUEST))
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    private void getEndpointNotFound(String sourceUrl, String catalogEndpoint) throws Exception {
        performGet(sourceUrl, catalogEndpoint, once(), withStatus(HttpStatus.NOT_FOUND))
            .andExpect(status().isNotFound());

        restServer.verify();
    }

    private void getEndpointDateParseError(String sourceUrl, String catalogEndpoint) throws Exception  {
        performGet(sourceUrl, catalogEndpoint, once(), successResponse(patronAccountDateParseErrorResource))
            .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    private void getEndpointNotSupportedForCatalog(String sourceUrl, String catalogEndpoint) throws Exception  {
        performGetWithCatalogName(sourceUrl, catalogEndpoint, never(), successResponse(patronAccountResource), VOYAGER_CATALOG)
            .andExpect(status().isBadRequest());

        restServer.verify();
    }

    private ResultActions performGet(String sourceUrl, String catalogEndpoint, ExpectedCount count, ResponseCreator response) throws Exception  {
        expectGetResponse(sourceUrl, count, response);

        return mockMvc.perform(get("/patron/{uin}/" + catalogEndpoint, UIN)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performGetWithCatalogName(String sourceUrl, String catalogEndpoint, ExpectedCount count, ResponseCreator response, String catalogName) throws Exception  {
        expectGetResponse(sourceUrl, count, response);

        return mockMvc.perform(get("/patron/{uin}/" + catalogEndpoint, UIN)
            .param("catalogName", catalogName)
            .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private void expectGetResponse(String sourceUrl, ExpectedCount count, ResponseCreator response) throws Exception  {
        expectResponse(HttpMethod.GET, sourceUrl, count, response);
    }

    private void expectPostResponse(String sourceUrl, ExpectedCount count, ResponseCreator response) throws Exception  {
        expectResponse(HttpMethod.POST, sourceUrl, count, response);
    }

    private void expectResponse(HttpMethod httpMethod, String sourceUrl, ExpectedCount count, ResponseCreator response) throws Exception  {
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

    private String getAccountUrl(boolean loans, boolean charges, boolean holds) {
        return String.format("%saccount/%s?apikey=%s&includeLoans=%s&includeCharges=%s&includeHolds=%s",
            basePath, UIN, API_KEY, Boolean.toString(loans), Boolean.toString(charges), Boolean.toString(holds));
    }

    private String getRenewalUrl() {
        return String.format("%saccount/%s/item/%s/renew?apikey=%s", basePath, UIN, ITEM_ID, API_KEY);
    }

    private DefaultResponseCreator okResponse(Resource resource) throws Exception {
        return withStatus(HttpStatus.OK).body(getMockJson(resource)).contentType(MediaType.APPLICATION_JSON);
    }

    private String getMockJson(Resource resource) throws JsonParseException, JsonMappingException, IOException {
        return IOUtils.toString(resource.getInputStream(), "UTF-8");
    }

    private DefaultResponseCreator successResponse(Resource resource) throws Exception {
        return withSuccess(getMockJson(resource), MediaType.APPLICATION_JSON);
    }

}
