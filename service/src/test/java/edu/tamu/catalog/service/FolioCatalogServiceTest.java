package edu.tamu.catalog.service;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.catalog.config.CatalogServiceConfig;
import edu.tamu.catalog.config.RestConfig;
import edu.tamu.catalog.utility.TokenUtility;

@RunWith(SpringRunner.class)
@RestClientTest(FolioCatalogService.class)
@Import({ RestConfig.class, CatalogServiceConfig.class })
public class FolioCatalogServiceTest {

    private static final String OKAPI_PATH = "http://localhost:9130/";
    private static final String OKAPI_TOKEN = "mocked_token";
    private static final String OKAPI_TOKEN_HEADER = "X-Okapi-Token";

    @Autowired
    private FolioCatalogService folioCatalogService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer restServer;

    @Before
    public void setup() throws Exception {
        MockRestServiceServerBuilder builder = MockRestServiceServer.bindTo(restTemplate);

        builder.ignoreExpectOrder(true);

        restServer = builder.build();

        TokenUtility.clearAll();

        expectOkapiLoginResponse(once(), withStatus(HttpStatus.CREATED));
    }

    @Test
    public void testOkapiHeadRequest() throws Exception {
        testOkapiRequest("locations", HttpMethod.HEAD, HttpStatus.OK);
    }

    @Test
    public void testOkapiGetRequest() throws Exception {
        testOkapiRequest("locations", HttpMethod.GET, HttpStatus.OK);
    }

    @Test
    public void testOkapiPostRequest() throws Exception {
        JsonNode requestBody = objectMapper.createObjectNode();
        testOkapiRequest("locations", HttpMethod.POST, HttpStatus.CREATED, requestBody);
    }

    @Test
    public void testOkapiPutRequest() throws Exception {
        JsonNode requestBody = objectMapper.createObjectNode();
        testOkapiRequest("locations/uuid", HttpMethod.PUT, HttpStatus.OK, requestBody);
    }

    @Test
    public void testOkapiDeleteRequest() throws Exception {
        testOkapiRequest("locations/uuid", HttpMethod.DELETE, HttpStatus.OK);
    }

    private void testOkapiRequest(String path, HttpMethod method, HttpStatus status) throws Exception {
        expectResponse(getOkapiUrl(path), method, once(), withStatus(status));
        ResponseEntity<JsonNode> entity = folioCatalogService.okapiRequest(getOkapiUrl(path), method, JsonNode.class);

        assertEquals(status, entity.getStatusCode());
        restServer.verify();
    }

    private void testOkapiRequest(String path, HttpMethod method, HttpStatus status, JsonNode requestBody) throws Exception {
        expectJsonResponse(getOkapiUrl(path), method, once(), autoResponse(requestBody, status));
        ResponseEntity<JsonNode> entity = folioCatalogService.okapiRequest(getOkapiUrl(path), method, requestBody, JsonNode.class);

        assertEquals(status, entity.getStatusCode());
        restServer.verify();
    }

    private void expectResponse(String sourceUrl, HttpMethod method, ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Okapi-Tenant", "diku");

        restServer.expect(count, requestTo(sourceUrl))
            .andExpect(method(method))
            .andRespond(response.headers(headers));
    }

    private void expectJsonResponse(String sourceUrl, HttpMethod method, ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Okapi-Tenant", "diku");

        restServer.expect(count, requestTo(sourceUrl))
            .andExpect(method(method))
            .andRespond(response.headers(headers));
    }

    private void expectOkapiLoginResponse(ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OKAPI_TOKEN_HEADER, OKAPI_TOKEN);

        restServer.expect(count, requestTo(getOkapiUrl("authn/login")))
            .andExpect(method(HttpMethod.POST))
            .andRespond(response.headers(headers));
    }

    private String getOkapiUrl(String path) {
        return String.format("%s%s", OKAPI_PATH, path);
    }

    private DefaultResponseCreator autoResponse(JsonNode node, HttpStatus status) throws Exception {
        if (status == HttpStatus.CREATED) {
            return createdResponse(node);
        }

        return successResponse(node);
    }

    private DefaultResponseCreator successResponse(JsonNode node) throws Exception {
        return withSuccess(node.toString(), MediaType.APPLICATION_JSON);
    }

    private DefaultResponseCreator createdResponse(JsonNode node) throws Exception {
        return withStatus(HttpStatus.CREATED).body(node.toString());
    }

}
