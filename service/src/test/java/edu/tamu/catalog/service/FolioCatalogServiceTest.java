package edu.tamu.catalog.service;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.catalog.config.CatalogServiceConfig;
import edu.tamu.catalog.config.RestConfig;
import edu.tamu.catalog.test.AbstractTestRestController;
import edu.tamu.catalog.utility.TokenUtility;

@RunWith(SpringRunner.class)
@RestClientTest(FolioCatalogService.class)
@Import({ RestConfig.class, CatalogServiceConfig.class })
public class FolioCatalogServiceTest extends AbstractTestRestController {

    @Autowired
    private FolioCatalogService folioCatalogService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() throws Exception {
        buildRestServer(restTemplate, true);
        TokenUtility.clearAll();

        expectOkapiLoginResponse(once(), withStatus(CREATED));
    }

    @Test
    public void testOkapiHeadRequest() throws Exception {
        performOkapiRequest("locations", HEAD, OK);
    }

    @Test
    public void testOkapiGetRequest() throws Exception {
        performOkapiRequest("locations", GET, OK);
    }

    @Test
    public void testOkapiPostRequest() throws Exception {
        JsonNode requestBody = objectMapper.createObjectNode();
        performOkapiRequest("locations", POST, CREATED, requestBody);
    }

    @Test
    public void testOkapiPutRequest() throws Exception {
        JsonNode requestBody = objectMapper.createObjectNode();
        performOkapiRequest("locations/uuid", PUT, OK, requestBody);
    }

    @Test
    public void testOkapiDeleteRequest() throws Exception {
        performOkapiRequest("locations/uuid", DELETE, OK);
    }

    private void performOkapiRequest(String path, HttpMethod method, HttpStatus status) throws Exception {
        expectOkapiResponse(path, method, once(), withStatus(status));
        ResponseEntity<JsonNode> entity = folioCatalogService.okapiRequest(getOkapiUrl(path), method, JsonNode.class);

        assertEquals(status, entity.getStatusCode());
        restServer.verify();
    }

    private void performOkapiRequest(String path, HttpMethod method, HttpStatus status, JsonNode requestBody)
            throws Exception {
        expectOkapiJsonResponse(path, method, once(), respondJsonAuto(requestBody, status));
        ResponseEntity<JsonNode> entity = folioCatalogService.okapiRequest(getOkapiUrl(path), method, requestBody, JsonNode.class);

        assertEquals(status, entity.getStatusCode());
        restServer.verify();
    }

}
