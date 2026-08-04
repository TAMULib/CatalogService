package edu.tamu.catalog.test;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.JsonNode;
import edu.tamu.catalog.config.FolioTokenConfig;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.IOUtils;
import org.hamcrest.text.MatchesPattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractTestRestController {

    protected static final String ACCESS_TOKEN_VALUE = "e5760fca-95e6-4804-b1f0-4f1cf5a210d2";
    protected static final String API_KEY = "mock_api_key";
    protected static final String BASE_PATH = "http://localhost:8080/";
    protected static final String CHARSET = "UTF-8";
    protected static final String COOKIE_DOMAIN = ".localhost";
    protected static final String COOKIE_PATH = "/";
    protected static final String OKAPI_BASE_PATH = "http://localhost:9130/";
    protected static final String OKAPI_LOGIN_PATH = "authn/login-with-expiry";
    protected static final String OKAPI_TOKEN = "mocked_token";
    protected static final String OKAPI_TOKEN_HEADER = "X-Okapi-Token";
    protected static final String OKAPI_TENANT = "diku";
    protected static final String OKAPI_TENANT_HEADER = "X-Okapi-Tenant";
    protected static final String REFRESH_TOKEN_VALUE = "de334eaf-dbbb-439c-bfd6-0bb0e631a7a2";
    protected static final String SET_COOKIE_HEADER = "Set-Cookie";
    protected static final String TEXT_PLAIN_UTF8_VALUE = MediaType.TEXT_PLAIN_VALUE + ";charset=" + CHARSET;

    protected static final Long EXPIRES_OFFSET = 100000L;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z").withZone(ZoneId.of("UTC"));

    protected MockRestServiceServer restServer;

    protected FolioTokenConfig tokenConfig;

    protected void buildRestServer(RestTemplate restTemplate, boolean ignoreExpectOrder) {
        MockRestServiceServerBuilder builder = MockRestServiceServer.bindTo(restTemplate);
        builder.ignoreExpectOrder(ignoreExpectOrder);
        restServer = builder.build();
    }

    protected void setTokenConfig(FolioTokenConfig tokenConfig) {
        this.tokenConfig = tokenConfig;
    }

    protected void expectOkapiResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        expectOkapiResponse(path, method, count, response, false);
    }

    protected void expectOkapiResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response, Boolean wildcard) throws Exception  {
        HttpHeaders headers = new HttpHeaders();

        addSetCookieHeaders(headers);
        expectResponse(getOkapiUrl(path), method, count, response.headers(headers), wildcard);
    }

    protected void expectOkapiJsonResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        expectOkapiJsonResponse(path, method, count, response, false);
    }

    protected void expectOkapiJsonResponse(String path, HttpMethod method, ExpectedCount count, DefaultResponseCreator response, Boolean wildcard) throws Exception  {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        addSetCookieHeaders(headers);
        expectResponse(getOkapiUrl(path), method, count, response.headers(headers), wildcard);
    }

    protected void expectOkapiLoginResponse(ExpectedCount count, DefaultResponseCreator response) throws Exception  {
        HttpHeaders headers = new HttpHeaders();

        addSetCookieHeaders(headers);
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

    protected void addSetCookieHeaders(HttpHeaders headers) {
        headers.add(SET_COOKIE_HEADER, buildCookieString(tokenConfig.getAccessCookieName(), ACCESS_TOKEN_VALUE));
        headers.add(SET_COOKIE_HEADER, buildCookieString(tokenConfig.getRefreshCookieName(), REFRESH_TOKEN_VALUE));
    }

    protected String buildCookieString(String name, String value) {
        String formattedDate = ZonedDateTime.now().plusSeconds(EXPIRES_OFFSET).format(DATE_FORMATTER);

        return new StringBuilder()
            .append(name).append("=").append(value)
            .append("; domain=").append(COOKIE_DOMAIN)
            .append("; path=").append(COOKIE_PATH)
            .append("; expires=").append(formattedDate)
            .toString();
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
