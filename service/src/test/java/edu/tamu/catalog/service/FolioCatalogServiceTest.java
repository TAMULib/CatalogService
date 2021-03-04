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
        when(restTemplate.exchange(eq("http://localhost:9130/authn/login"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<String>("{ \"username\": \"diku_admin\", \"password\": \"admin\" }", headers, HttpStatus.CREATED));
    }

    @Test
    public void testOkapiRequestGET() {
        String url = "http://localhost:9130/locations";
        HttpMethod method = HttpMethod.GET;
        JsonNode requestBody = objectMapper.createObjectNode();
        JsonNode responseBody = objectMapper.createObjectNode();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Okapi-Tenant", "diku");
        when(restTemplate.exchange(eq(url), eq(method), any(HttpEntity.class), eq(JsonNode.class)))
            .thenReturn(new ResponseEntity<JsonNode>(responseBody, headers, HttpStatus.OK));

        ResponseEntity<JsonNode> response = folioCatalogService.okapiRequest(url, method, requestBody, JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
