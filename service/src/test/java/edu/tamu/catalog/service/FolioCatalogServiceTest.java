package edu.tamu.catalog.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import edu.tamu.catalog.properties.Credentials;
import edu.tamu.catalog.properties.FolioProperties;

@RunWith(SpringRunner.class)
public class FolioCatalogServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private Credentials credentials = new Credentials("diku_admin", "admin");

    private FolioProperties properties = new FolioProperties(
        "folio",
        "folio",
        "http://localhost:9130",
        "http://localhost:8080",
        "diku",
        credentials,
        "mock_api_key",
        "localhost"
    );

    @InjectMocks
    private FolioCatalogService folioCatalogService = new FolioCatalogService(properties);

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Okapi-Token", "token");
        when(restTemplate.postForObject(eq("http://localhost:9130/authn/login"), any(HttpEntity.class), eq(ResponseEntity.class)))
            .thenReturn(new ResponseEntity<String>("{ \"username\": \"diku_admin\", \"password\": \"admin\" }", headers, HttpStatus.CREATED));
    }

    @Test
    public void testOkapiHeadRequest() {
        testOkapiRequest("http://localhost:9130/locations", HttpMethod.HEAD, HttpStatus.OK);
    }

    @Test
    public void testOkapiGetRequest() {
        testOkapiRequest("http://localhost:9130/locations", HttpMethod.GET, HttpStatus.OK);
    }

    @Test
    public void testOkapiPostRequest() {
        JsonNode requestBody = objectMapper.createObjectNode();
        testOkapiRequest("http://localhost:9130/locations", HttpMethod.POST, requestBody, HttpStatus.CREATED);
    }

    @Test
    public void testOkapiPutRequest() {
        JsonNode requestBody = objectMapper.createObjectNode();
        testOkapiRequest("http://localhost:9130/locations/uuid", HttpMethod.PUT, requestBody, HttpStatus.OK);
    }

    @Test
    public void testOkapiDeleteRequest() {
        testOkapiRequest("http://localhost:9130/locations/uuid", HttpMethod.DELETE, HttpStatus.OK);
    }

    private void testOkapiRequest(String url, HttpMethod method, HttpStatus status) {
        JsonNode responseBody = objectMapper.createObjectNode();
        mockExchange(url, method, responseBody, status);
        ResponseEntity<JsonNode> response = folioCatalogService.okapiRequest(url, method, JsonNode.class);
        assertEquals(status, response.getStatusCode());
    }

    private void testOkapiRequest(String url, HttpMethod method, JsonNode requestBody, HttpStatus status) {
        JsonNode responseBody = objectMapper.createObjectNode();
        mockExchange(url, method, responseBody, status);
        ResponseEntity<JsonNode> response = folioCatalogService.okapiRequest(url, method, requestBody, JsonNode.class);
        assertEquals(status, response.getStatusCode());
    }

    private void mockExchange(String url, HttpMethod method, JsonNode responseBody, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Okapi-Tenant", "diku");
        when(restTemplate.exchange(eq(url), eq(method), any(HttpEntity.class), eq(JsonNode.class)))
            .thenReturn(new ResponseEntity<JsonNode>(responseBody, headers, status));
    }

}
