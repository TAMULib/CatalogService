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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.NestedServletException;

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

        restServer.expect(once(), requestTo(getFinesUrl(uin)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(getMockPatronAccount(), MediaType.APPLICATION_JSON));

        // @formatter:off
        mockMvc.perform(get("/patron/{uin}/fines", uin)
                .param("catalogName", "folio")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andDo(
                document(
                    "patron/fines",
                    responseFields(
                        fieldWithPath("id").description("The patron UIN."),
                        fieldWithPath("total").description("The sum total of the fines."),
                        fieldWithPath("fineCount").description("The total number of fines in the list."),
                        fieldWithPath("list[]").description("An array of all fines for the patron."),
                        fieldWithPath("list[].amount").description("The title of the item associated with the fine."),
                        fieldWithPath("list[].fineId").description("The UUID associated with the fine."),
                        fieldWithPath("list[].fineType").description("The type of the fine."),
                        fieldWithPath("list[].fineDate").description("A timestamp in milliseconds from UNIX epoch representing the date the fine was accrued."),
                        fieldWithPath("list[].itemTitle").description("The title of the item associated with the fine.")
                    )
                )
            );
        // @formatter:on

        restServer.verify();
    }

    @Test
    public void testFinesMockMVCWithCatalogName() throws Exception {
        String uin = "1234567890";

        restServer.expect(once(), requestTo(getFinesUrl(uin)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(getMockPatronAccount(), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/patron/{uin}/fines", uin)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        restServer.verify();
    }

    @Test
    public void testFinesUINNotFound() throws Exception {
        String uin = "1234567890";

        restServer.expect(once(), requestTo(getFinesUrl(uin)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/patron/{uin}/fines", uin)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());

        restServer.verify();
    }

    @Test
    public void testFinesNotSupportedForCatalog() throws Exception  {
        String uin = "1234567890";

        restServer.expect(never(), requestTo(getFinesUrl(uin)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(getMockPatronAccount(), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/patron/{uin}/fines", uin)
            .param("catalogName", VOYAGER_CATALOG)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesClientException() throws Exception {
        String uin = "1234567890";

        restServer.expect(once(), requestTo(getFinesUrl(uin)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/patron/{uin}/fines", uin)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest());

        restServer.verify();
    }

    @Test
    public void testFinesServerException() throws Exception {
        String uin = "1234567890";

        restServer.expect(once(), requestTo(getFinesUrl(uin)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/patron/{uin}/fines", uin)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isInternalServerError());

        restServer.verify();
    }

    private String getMockPatronAccount() throws JsonParseException, JsonMappingException, IOException {
        return IOUtils.toString(patronAccountResource.getInputStream(), "UTF-8");
    }

    private String getFinesUrl(String uin) {
        String additional = "&includeLoans=false&includeCharges=true&includeHolds=false";
        return String.format("%saccount/%s?apikey=%s%s", basePath, uin, API_KEY, additional);
    }

}
