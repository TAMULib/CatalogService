package edu.tamu.catalog.controller;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Date;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
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

    private static final String API_KEY = "mocked_key";
    private static final String FOLIO_CATALOG = "folio";
    private static final String VOYAGER_CATALOG = "voyager";

    @Value("classpath:mock/patron/account.json")
    private Resource patronAccountResource;

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
        String uin = "1234567890";
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
        testPatronEndpointMockMVC(uin, getFinesUrl(uin), "fines", responseFields);
    }

    @Test
    public void testLoansMockMVC() throws Exception {
        String uin = "1234567890";

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
        testPatronEndpointMockMVC(uin, getLoansUrl(uin), "loans", responseFields);
    }

    protected void testPatronEndpointMockMVC(String uin, String sourceUrl, String catalogEndpoint, ResponseFieldsSnippet responseFields) throws Exception {
        restServer.expect(once(), requestTo(sourceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(getMockPatronAccount(), MediaType.APPLICATION_JSON));
        String docEndpoint = "patron/"+catalogEndpoint;
        // @formatter:off
        mockMvc.perform(get("/patron/{uin}/{catalogEndpoint}", uin, catalogEndpoint)
                .param("catalogName", "folio")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andDo(
                document(
                    docEndpoint,
                    responseFields
                )
            );
        // @formatter:on

        restServer.verify();
    }

    @Test
    public void testFinesMockMVCWithCatalogName() throws Exception {
        String uin = "1234567890";
        testPatronEndpointMockMVCWithCatalogName(uin, getFinesUrl(uin), "fines");
    }

    @Test
    public void testLoansMockMVCWithCatalogName() throws Exception {
        String uin = "1234567890";
        testPatronEndpointMockMVCWithCatalogName(uin, getLoansUrl(uin), "loans");
    }

    protected void testPatronEndpointMockMVCWithCatalogName(String uin, String sourceUrl, String catalogEndpoint) throws Exception {
        restServer.expect(once(), requestTo(sourceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(getMockPatronAccount(), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/patron/{uin}/{catalogEndpoint}", uin, catalogEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        restServer.verify();
    }

    @Test
    public void testFinesUINNotFound() throws Exception {
        String uin = "1234567890";
        testPatronEndpointUINNotFound(uin, getFinesUrl(uin), "fines");
    }

    @Test
    public void testLoansUINNotFound() throws Exception {
        String uin = "1234567890";
        testPatronEndpointUINNotFound(uin, getLoansUrl(uin), "loans");
    }

    protected void testPatronEndpointUINNotFound(String uin, String sourceUrl, String catalogEndpoint) throws Exception {
        restServer.expect(once(), requestTo(sourceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/patron/{uin}/{catalogEndpoint}", uin, catalogEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testFinesDateParseError() throws Exception  {
        String uin = "1234567890";
        testPatronEndpointDateParseError(uin, getFinesUrl(uin), "fines");
    }

    @Test
    public void testLoansDateParseError() throws Exception  {
        String uin = "1234567890";
        testPatronEndpointDateParseError(uin, getLoansUrl(uin), "loans");
    }

    protected void testPatronEndpointDateParseError(String uin, String sourceUrl, String catalogEndpoint) throws Exception  {
        restServer.expect(once(), requestTo(sourceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(getMockPatronAccountDateParseError(), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/patron/{uin}/{catalogEndpoint}", uin, catalogEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    @Test
    public void testFinesNotSupportedForCatalog() throws Exception  {
        String uin = "1234567890";
        testPatronEndpointNotSupportedForCatalog(uin, getFinesUrl(uin), "fines");
    }

    @Test
    public void testLoansNotSupportedForCatalog() throws Exception  {
        String uin = "1234567890";
        testPatronEndpointNotSupportedForCatalog(uin, getLoansUrl(uin), "loans");
    }

    protected void testPatronEndpointNotSupportedForCatalog(String uin, String sourceUrl, String catalogEndpoint) throws Exception  {
        restServer.expect(never(), requestTo(sourceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(getMockPatronAccount(), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/patron/{uin}/{catalogEndpoint}", uin, catalogEndpoint)
            .param("catalogName", VOYAGER_CATALOG)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesClientException() throws Exception {
        String uin = "1234567890";
        testPatronEndpointClientException(uin, getFinesUrl(uin), "fines");
    }

    @Test
    public void testLoansClientException() throws Exception {
        String uin = "1234567890";
        testPatronEndpointClientException(uin, getLoansUrl(uin), "loans");
    }

    protected void testPatronEndpointClientException(String uin, String sourceUrl, String catalogEndpoint) throws Exception {
        restServer.expect(once(), requestTo(sourceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/patron/{uin}/{catalogEndpoint}", uin, catalogEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesServerException() throws Exception {
        String uin = "1234567890";
        testPatronServerException(uin, getFinesUrl(uin), "fines");
    }

    @Test
    public void testLoansServerException() throws Exception {
        String uin = "1234567890";
        testPatronServerException(uin, getLoansUrl(uin), "loans");
    }

    protected void testPatronServerException(String uin, String sourceUrl, String catalogEndpoint) throws Exception {
        restServer.expect(once(), requestTo(sourceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/patron/{uin}/{catalogEndpoint}", uin, catalogEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    private String getMockPatronAccount() throws JsonParseException, JsonMappingException, IOException {
        return IOUtils.toString(patronAccountResource.getInputStream(), "UTF-8");
    }

    private String getMockPatronAccountDateParseError() throws JsonParseException, JsonMappingException, IOException {
        return IOUtils.toString(patronAccountDateParseErrorResource.getInputStream(), "UTF-8");
    }

    private String getFinesUrl(String uin) {
        String additional = "&includeLoans=false&includeCharges=true&includeHolds=false";
        return String.format("%saccount/%s?apikey=%s%s", basePath, uin, API_KEY, additional);
    }

    private String getLoansUrl(String uin) {
        String additional = "&includeLoans=true&includeCharges=false&includeHolds=false";
        return String.format("%saccount/%s?apikey=%s%s", basePath, uin, API_KEY, additional);
    }
}
