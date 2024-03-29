package edu.tamu.catalog.test;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestTemplate;
import org.hamcrest.text.MatchesPattern;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractTestRestController {

    protected static final String BASE_PATH = "http://localhost:8080/";
    protected static final String OKAPI_BASE_PATH = "http://localhost:9130/";
    protected static final String OKAPI_LOGIN_PATH = "authn/login";
    protected static final String OKAPI_TOKEN = "mocked_token";
    protected static final String OKAPI_TOKEN_HEADER = "X-Okapi-Token";
    protected static final String OKAPI_TENANT = "diku";
    protected static final String OKAPI_TENANT_HEADER = "X-Okapi-Tenant";
    protected static final String API_KEY = "mock_api_key";

    protected static final String CHARSET = "UTF-8";
    protected static final String TEXT_PLAIN_UTF8_VALUE = MediaType.TEXT_PLAIN_VALUE
        + ";charset=" + CHARSET;

    protected MockRestServiceServer restServer;

    protected void buildRestServer(RestTemplate restTemplate, boolean ignoreExpectOrder) {
        MockRestServiceServerBuilder builder = MockRestServiceServer.bindTo(restTemplate);
        builder.ignoreExpectOrder(ignoreExpectOrder);
        restServer = builder.build();
    }

    protected void expectOkapiResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        expectOkapiResponse(path, method, count, response, false);
    }

    protected void expectOkapiResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response, Boolean wildcard) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.set(OKAPI_TENANT_HEADER, OKAPI_TENANT);

        expectResponse(getOkapiUrl(path), method, count, response.headers(headers), wildcard);
    }

    protected void expectOkapiJsonResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        expectOkapiJsonResponse(path, method, count, response, false);
    }

    protected void expectOkapiJsonResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response, Boolean wildcard) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(OKAPI_TENANT_HEADER, OKAPI_TENANT);

        expectResponse(getOkapiUrl(path), method, count, response.headers(headers), wildcard);
    }

    protected void expectOkapiLoginResponse(ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OKAPI_TOKEN_HEADER, OKAPI_TOKEN);

        expectPostResponse(getOkapiLoginUrl(), count, response.headers(headers));
    }

    protected void expectGetResponse(String url, ExpectedCount count, DefaultResponseCreator response) throws Exception {
        expectResponse(url, GET, count, response, false);
    }

    protected void expectGetResponse(String url, ExpectedCount count, DefaultResponseCreator response, Boolean wildcard) throws Exception {
        expectResponse(url, GET, count, response, wildcard);
    }

    protected void expectPostResponse(String url, ExpectedCount count, DefaultResponseCreator response) throws Exception {
        expectResponse(url, POST, count, response, false);
    }

    protected void expectPostResponse(String url, ExpectedCount count, DefaultResponseCreator response, Boolean wildcard) throws Exception {
        expectResponse(url, POST, count, response, wildcard);
    }

    protected void expectResponse(String url, HttpMethod method, ExpectedCount count, DefaultResponseCreator response, Boolean wildcard) throws Exception {
        restServer.expect(count, wildcard ? requestTo(MatchesPattern.matchesPattern(url)) : requestTo(url))
            .andExpect(method(method))
            .andRespond(response);
    }

    protected static DefaultResponseCreator respondJsonAuto(JsonNode node, HttpStatus status) throws Exception {
        if (status == CREATED) {
            return respondJsonCreated(node);
        }

        return respondJsonOk(node);
    }

    protected static DefaultResponseCreator respondJsonOk(String payload) throws Exception {
        return withStatus(OK).body(payload).contentType(MediaType.APPLICATION_JSON);
    }

    protected static DefaultResponseCreator respondJsonOk(JsonNode node) throws Exception {
        return withStatus(OK).body(node.toString()).contentType(MediaType.APPLICATION_JSON);
    }

    protected static DefaultResponseCreator respondJsonCreated(String payload) throws Exception {
        return withStatus(CREATED).body(payload).contentType(MediaType.APPLICATION_JSON);
    }

    protected static DefaultResponseCreator respondJsonCreated(JsonNode node) throws Exception {
        return withStatus(CREATED).body(node.toString()).contentType(MediaType.APPLICATION_JSON);
    }

    protected static DefaultResponseCreator respondTextSuccess(String payload) throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", TEXT_PLAIN_UTF8_VALUE);
        return withStatus(OK).body(payload).headers(headers);
    }

    protected static String getOkapiLoginUrl() {
        return getOkapiUrl(OKAPI_LOGIN_PATH);
    }

    protected static String getOkapiUrl(String path) {
        return String.format("%s%s", OKAPI_BASE_PATH, path);
    }

    protected static String loadResource(URL url) throws IOException {
        return IOUtils.toString(url.openStream(), CHARSET);
    }

}
