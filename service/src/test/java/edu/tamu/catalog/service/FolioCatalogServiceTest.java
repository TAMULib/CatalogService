package edu.tamu.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.catalog.config.CatalogServiceConfig;
import edu.tamu.catalog.config.RestConfig;
import edu.tamu.catalog.test.AbstractTestRestController;
import edu.tamu.catalog.utility.TokenUtility;

@RestClientTest(FolioCatalogService.class)
@Import({ RestConfig.class, CatalogServiceConfig.class })
public class FolioCatalogServiceTest extends AbstractTestRestController {

    @Autowired
    private FolioCatalogService folioCatalogService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        buildRestServer(restTemplate, true);
        TokenUtility.clearAll();

        expectOkapiLoginResponse(once(), withStatus(CREATED));
    }

    @ParameterizedTest
    @MethodSource("performOkapiRequests")
    public void testOkapiRequests(String path, HttpMethod method, HttpStatus status, boolean withJson) throws Exception {
        if (withJson) {
            JsonNode requestBody = objectMapper.createObjectNode();
            expectOkapiJsonResponse(path, method, once(), respondJsonAuto(requestBody, status));
        }
        else {
            expectOkapiResponse(path, method, once(), withStatus(status));
        }

        ResponseEntity<JsonNode> entity = folioCatalogService.okapiRequest(getOkapiUrl(path), method, JsonNode.class);

        assertEquals(status, entity.getStatusCode(), "Received incorrect status for " + method.toString() + " /" + path + "");
        restServer.verify();
    }

    private static Stream<? extends Arguments> performOkapiRequests() throws Exception {
        return Stream.of(
          Arguments.of("locations", HEAD, OK, false),
          Arguments.of("locations", GET, OK, false),
          Arguments.of("locations", POST, CREATED, true),
          Arguments.of("locations/uuid", PUT, OK, true),
          Arguments.of("locations/uuid", DELETE, OK, false)
        );
    }

}
