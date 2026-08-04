package edu.tamu.catalog.service;

import static edu.tamu.catalog.utility.JsonNodeUtility.getBoolean;
import static edu.tamu.catalog.utility.JsonNodeUtility.getDouble;
import static edu.tamu.catalog.utility.JsonNodeUtility.getInt;
import static edu.tamu.catalog.utility.JsonNodeUtility.getText;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_AUTHOR;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_CALL_NUMBER;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_EDITION;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_FALLBACK_LOCATION_CODE;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_GENRE;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_ISBN;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_ISSN;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_MARC_RECORD_LEADER;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_MFHD;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_OCLC;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_PLACE;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_PUBLISHER;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_RECORD_ID;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_TITLE;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_VALID_LARGE_VOLUME;
import static edu.tamu.catalog.utility.Marc21Xml.RECORD_YEAR;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.tamu.catalog.config.FolioTenantConfig;
import edu.tamu.catalog.config.FolioTokenConfig;
import edu.tamu.catalog.domain.model.FeeFine;
import edu.tamu.catalog.domain.model.HoldRequest;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.domain.model.Note;
import edu.tamu.catalog.exception.BibIdNotFoundError;
import edu.tamu.catalog.exception.HoldingsRequestError;
import edu.tamu.catalog.exception.RemoteServerError;
import edu.tamu.catalog.exception.CatalogHttpClientException;
import edu.tamu.catalog.exception.CatalogHttpServerException;
import edu.tamu.catalog.exception.RenewFailureException;
import edu.tamu.catalog.model.FolioHoldCancellation;
import edu.tamu.catalog.model.FolioToken;
import edu.tamu.catalog.model.FolioTokens;
import edu.tamu.catalog.properties.CatalogServiceProperties;
import edu.tamu.catalog.properties.Credentials;
import edu.tamu.catalog.properties.FolioProperties;
import edu.tamu.catalog.utility.FolioDateTime;
import edu.tamu.catalog.utility.FolioTokenUtility;
import edu.tamu.catalog.utility.Marc21Xml;
import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FolioCatalogService implements CatalogService {

    private static final String RENEWAL_WOULD_NOT_CHANGE_THE_DUE_DATE = "renewal would not change the due date";

    private static final Logger logger = LoggerFactory.getLogger(FolioCatalogService.class);

    private static final Map<String, JsonNode> LOCATION_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, JsonNode> SERVICE_POINT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, JsonNode> LOAN_POLICY_CACHE = new ConcurrentHashMap<>();

    private static final String EXPIRES = "expires";
    private static final String METADATA_PREFIX = "marc21_withholdings";
    private static final String NODE_PREFIX = "marc:";
    private static final String NODE_CONTROL_FIELD = "controlfield";
    private static final String NODE_DATA_FIELD = "datafield";
    private static final String NODE_ERROR = "error";
    private static final String NODE_LEADER = "leader";
    private static final String NODE_MARC_RECORD_LEADER = "marcRecordLeader";
    private static final String NODE_METADATA = "metadata";
    private static final String NODE_OAI = "oai";
    private static final String NODE_RECORD = "record";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String VERB_GET_RECORD = "GetRecord";

    private static final int MAX_BATCH_SIZE = 90;

    @Autowired
    private FolioTenantConfig tenantConfig;

    @Autowired
    private FolioTokenConfig tokenConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private FolioProperties properties;

    public FolioCatalogService(CatalogServiceProperties properties) {
        this.properties = (FolioProperties) properties;
        this.properties.setBaseEdgeUrl(StringUtils.removeEnd(this.properties.getBaseEdgeUrl(), "/"));
        this.properties.setBaseOkapiUrl(StringUtils.removeEnd(this.properties.getBaseOkapiUrl(), "/"));
    }

    @Override
    public String getName() {
        return properties.getName();
    }

    private boolean isUUID(String id) {
        String[] components = id.split("-");
        if (components.length == 5) {
            return true;
        }
        return false;
    }

    @Override
    public List<HoldingsRecord> getHoldingsByBibId(String id) throws Exception {
        String instanceId = null;

        // If it's not a uuid, assume hrid and try to get the uuid from the instance data
        if (!isUUID(id)) {
            JsonNode instanceData = getInstanceByHrid(id);
            JsonNode instances = null;

            if (instanceData != null && instanceData.size() > 0) {
                instances = instanceData.at("/instances");
            }

            if (instances == null || instances.size() == 0) {
                throw new BibIdNotFoundError(id, this.getName());
            }

            try {
                JsonNode instance = instances.get(0).at("/id");

                if (instance == null) {
                    logger.error("Error retrieving instance by hrid: {}", id);
                    return null;
                }

                instanceId = instance.asText();
            } catch (Exception e) {
                logger.error("Error retrieving instance by hrid: {}", id);
                e.printStackTrace();
                return null;
            }
        } else {
            instanceId = id;
        }

        return requestHoldings(instanceId, null);
    }

    @Override
    public HoldingsRecord getHolding(String instanceId, String holdingId) throws Exception {
        List<HoldingsRecord> holdings = requestHoldings(instanceId, holdingId);

        if (holdings.size() > 0) {
            return holdings.get(0);
        }

        return null;
    }

    @Override
    public List<FeeFine> getFeesFines(String uin) throws Exception {
        String path = "patron/account";
        String queryString = "apikey={apikey}&includeLoans=false&includeCharges=true&includeHolds=false";
        String url = String.format("%s/%s/%s?%s", properties.getBaseEdgeUrl(), path, uin, queryString);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for fines from: {}", url);

        JsonNode node = restGet(url, JsonNode.class, apiKey);

        List<FeeFine> list = new ArrayList<>();

        if (node != null) {
            JsonNode charges = node.at("/charges");

            if (charges.isContainerNode() && charges.isArray()) {
                Iterator<JsonNode> iter = charges.elements();

                while (iter.hasNext()) {
                    JsonNode charge = iter.next();
                    list.add(FeeFine.builder()
                        .fineId(getText(charge, "/feeFineId"))
                        .itemId(getText(charge, "/item/itemId"))
                        .instanceId(getText(charge, "/item/instanceId"))
                        .fineType(getText(charge, "/reason"))
                        .fineDate(getDate(charge, "/accrualDate"))
                        .itemTitle(getText(charge, "/item/title"))
                        .amount(getDouble(charge, "/chargeAmount/amount", 0))
                        .build());
                }
            }
        }

        return list;
    }

    @Override
    public List<LoanItem> getLoanItems(String uin) throws Exception {
        String path = "patron/account";
        String queryString = "apikey={apikey}&includeLoans=true&includeCharges=false&includeHolds=false";
        String url = String.format("%s/%s/%s?%s", properties.getBaseEdgeUrl(), path, uin, queryString);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for patron loans from: {}", url);

        JsonNode node = restGet(url, JsonNode.class, apiKey);

        List<LoanItem> list = new ArrayList<>();

        JsonNode loans = node.at("/loans");

        if (loans.isContainerNode() && loans.isArray()) {
            Iterator<JsonNode> iter = loans.elements();

            Map<String, JsonNode> loanIdToPartialLoan = new HashMap<>();
            Map<String, String> instanceIdToLoanId = new HashMap<>();
            Map<String, String> itemIdToLoanId = new HashMap<>();

            while (iter.hasNext()) {
                JsonNode partialLoan = iter.next();
                String loanId = getText(partialLoan, "/id");
                String instanceId = getText(partialLoan, "/item/instanceId");
                String itemId = getText(partialLoan, "/item/itemId");

                loanIdToPartialLoan.put(loanId, partialLoan);
                instanceIdToLoanId.put(instanceId, loanId);
                itemIdToLoanId.put(itemId, loanId);
            }

            Map<String, JsonNode> loanIdToLoan = new HashMap<>();
            Map<String, JsonNode> instanceIdToInstance = new HashMap<>();
            Map<String, JsonNode> itemIdToItem = new HashMap<>();

            for (JsonNode loan : getLoans(loanIdToPartialLoan.keySet())) {
                String loanId = getText(loan, "/id");
                loanIdToLoan.put(loanId, loan);
            }

            for (JsonNode instance : getInstances(instanceIdToLoanId.keySet())) {
                String instanceId = getText(instance, "/id");
                instanceIdToInstance.put(instanceId, instance);
            }

            for (JsonNode item : getItems(itemIdToLoanId.keySet())) {
                String itemId = getText(item, "/id");
                itemIdToItem.put(itemId, item);
            }

            for (String loanId : itemIdToLoanId.values()) {
                JsonNode partialLoan = loanIdToPartialLoan.get(loanId);

                String instanceId = getText(partialLoan, "/item/instanceId");
                String itemId = getText(partialLoan, "/item/itemId");

                JsonNode loan = loanIdToLoan.get(loanId);
                JsonNode instance = instanceIdToInstance.get(instanceId);
                JsonNode item = itemIdToItem.get(itemId);

                String locationId = getText(item, "/effectiveLocation/id");
                String loanPolicyName = getText(loan, "/loanPolicy/name");

                JsonNode loanPolicy = getLoanPolicy(loanPolicyName);

                LoanItem.LoanItemBuilder builder = LoanItem.builder()
                    .loanId(getText(partialLoan, "/id"))
                    .itemId(itemId)
                    .instanceId(instanceId)
                    .instanceHrid(getText(instance, "/hrid"))
                    .itemType(getText(item, "/permanentLoanType/name"))
                    .loanDate(getDate(partialLoan, "/loanDate"))
                    .loanDueDate(getDate(partialLoan, "/dueDate"))
                    .overdue(getBoolean(partialLoan, "/overdue", false))
                    .title(getText(partialLoan, "/item/title"))
                    .author(getText(partialLoan, "/item/author"))
                    .canRenew(getBoolean(loanPolicy, "/renewable"));

                if (StringUtils.isNotEmpty(locationId)) {
                    JsonNode location = getLocation(locationId);
                    builder.location(getText(location, "/discoveryDisplayName"))
                        .locationCode(getText(location, "/code"));
                }

                list.add(builder.build());
            }
        }
        return list;
    }

    @Override
    public List<HoldRequest> getHoldRequests(String uin) throws Exception {
        String path = "patron/account";
        String queryString = "apikey={apikey}&includeLoans=false&includeCharges=false&includeHolds=true";
        String url = String.format("%s/%s/%s?%s", properties.getBaseEdgeUrl(), path, uin, queryString);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for patron hold requests from: {}", url);

        JsonNode node = restGet(url, JsonNode.class, apiKey);

        List<HoldRequest> list = new ArrayList<>();

        JsonNode holds = node.at("/holds");

        if (holds.isContainerNode() && holds.isArray()) {
            Iterator<JsonNode> iter = holds.elements();

            while (iter.hasNext()) {
                JsonNode hold = iter.next();
                String requestId = getText(hold, "/requestId");
                String servicePointId = getText(hold, "/pickupLocationId");
                list.add(HoldRequest.builder()
                    .requestId(requestId)
                    .itemId(getText(hold, "/item/itemId"))
                    .instanceId(getText(hold, "/item/instanceId"))
                    .itemTitle(getText(hold, "/item/title"))
                    .statusText(getText(hold, "/status"))
                    .queuePosition(getInt(hold, "/queuePosition"))
                    .requestDate(getDate(hold, "/requestDate"))
                    .expirationDate(getDate(hold, "/expirationDate"))
                    .requestType(getRequestType(requestId))
                    .pickupServicePoint(getServicePointDisplayName(servicePointId))
                    .build());
            }
        }

        return list;
    }

    @Override
    public void cancelHoldRequest(String uin, String requestId) throws Exception {
        String path = String.format("patron/account/%s/hold/%s/cancel", uin, requestId);
        String queryString = "apikey={apikey}";
        String url = String.format("%s/%s?%s", properties.getBaseEdgeUrl(), path, queryString);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Cancelling hold request via: {}", url);

        // edge-patron uses "holdId" instead of "requestId" for the cancellation request body json.
        FolioHoldCancellation folioCancellation = new FolioHoldCancellation();
        folioCancellation.setHoldId(requestId);
        folioCancellation.setCancellationReasonId(properties.getCancelHoldReasonId());
        folioCancellation.setCanceledDate(FolioDateTime.convert(new Date()));

        restTemplate.postForObject(url, folioCancellation, Object.class, apiKey);
    }

    @Override
    public LoanItem renewItem(String uin, String itemId) throws Exception {
        String path = String.format("patron/account/%s/item/%s/renew", uin, itemId);
        String queryString = "apikey={apikey}";
        String url = String.format("%s/%s?%s", properties.getBaseEdgeUrl(), path, queryString);
        String apiKey = properties.getEdgeApiKey();

        JsonNode partialLoan;

        try {
            partialLoan = restTemplate.postForObject(url, null, JsonNode.class, apiKey);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 422) {
                JsonNode error = objectMapper.readTree(e.getResponseBodyAsString());
                JsonNode errorMessage = error.at("/errorMessage");
                if (errorMessage.isValueNode() && errorMessage.textValue().equals(RENEWAL_WOULD_NOT_CHANGE_THE_DUE_DATE)) {
                    throw new RenewFailureException(errorMessage.textValue());
                }
            }
            throw e;
        }

        JsonNode item = getItem(itemId);

        String loanId = getText(partialLoan, "/id");

        String instanceId = getText(partialLoan, "/item/instanceId");
        String instanceHrid = getInstanceHrid(instanceId);

        JsonNode loan = getLoan(loanId);

        String loanPolicyName = getText(loan, "/loanPolicy/name");

        JsonNode loanPolicy = getLoanPolicy(loanPolicyName);

        LoanItem.LoanItemBuilder builder = LoanItem.builder()
            .loanId(loanId)
            .itemId(itemId)
            .instanceId(instanceId)
            .instanceHrid(instanceHrid)
            .itemType(getText(item, "/permanentLoanType/name"))
            .loanDate(getDate(partialLoan, "/loanDate"))
            .loanDueDate(getDate(partialLoan, "/dueDate"))
            .overdue(getBoolean(partialLoan, "/overdue", false))
            .title(getText(partialLoan, "/item/title"))
            .author(getText(partialLoan, "/item/author"))
            .canRenew(getBoolean(loanPolicy, "/renewable"));

        String locationId = getText(item, "/effectiveLocation/id");

        if (StringUtils.isNotEmpty(locationId)) {
            JsonNode location = getLocation(locationId);
            builder.location(getText(location, "/name"))
                .locationCode(getText(location, "/code"));
        }

        return builder.build();
    }

    @Override
    public Boolean getBlockStatus(String uin) throws Exception {
        JsonNode user = getUserByUin(uin);

        String userId = getText(user, "/id");
        if (StringUtils.isNotEmpty(userId)) {

            return getAutomatedBlockStatus(userId) || getManualBlockStatus(userId);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "%s: Unable to retrieve automated block status for user.";
        throw new HttpClientErrorException(status, String.format(message, status.getReasonPhrase()));
    }

    /**
     * Use OKAPI to retrieve the JsonNode, throwing a customized exception on client or server errors.
     *
     * @param url String the URL to retrieve.
     * @param method The HTTP Method to use when making the request.
     * @param message The request payload.
     *
     * @return response entity with response type as body.
     */
    JsonNode okapiRequestJsonNode(String url, HttpMethod method, String message) {
        String errorReason = null;

        HttpEntity<?> requestEntity = new HttpEntity<>(headers(properties.getTenant(), getOkapiToken()));

        try {
            ResponseEntity<JsonNode> response = okapiRequest(url, method, requestEntity, JsonNode.class);

            if (response != null) {
              JsonNode node = response.getBody();

              if (node != null && node.isContainerNode()) {
                return node;
              }
            }

            errorReason = "Invalid body in the HTTP response";
        }
        catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s, reason: %s.", e.getStatusText(), message, e.getMessage()));
        }
        catch (HttpServerErrorException e) {
            throw new HttpServerErrorException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s, reason: %s.", e.getStatusText(), message, e.getMessage()));
        }
        catch (Exception e) {
            errorReason = e.getMessage();
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        throw new HttpServerErrorException(status,
            String.format("%s: Catalog service failed to find %s, reason: %s.", status.getReasonPhrase(), message, errorReason));
    }

     /**
      * Use OKAPI to retrieve the JsonNode, throwing a customized exception on client or server errors.
      *
      * @param url String the URL to retrieve
      * @param method HttpMethod
      * @param message exception response message
      * @param uriVariables interpolation variables
      * @return response entity with response type as body.
      */
    JsonNode okapiRequestJsonNode(String url, HttpMethod method, String message, Object... uriVariables) {

        HttpEntity<?> requestEntity = new HttpEntity<>(headers(properties.getTenant(), getOkapiToken()));

        try {
            ResponseEntity<JsonNode> response = okapiRequest(url, method, requestEntity, JsonNode.class, uriVariables);

            if (response != null) {
              JsonNode node = response.getBody();

              if (node != null && node.isContainerNode()) {
                return node;
              }
            }
        }
        catch (HttpClientErrorException e) {
            logger.error(String.format("%s: Request failed for %s: %s", e.getStatusCode(), url, e.getMessage()));

            throw new CatalogHttpClientException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s.", e.getStatusText(), message));
        }
        catch (HttpServerErrorException e) {
            logger.error(String.format("%s: Request failed for %s: %s", e.getStatusCode(), url, e.getMessage()));

            throw new CatalogHttpServerException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s.", e.getStatusText(), message));
        }
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        throw new HttpServerErrorException(status,
            String.format("%s: Catalog service failed to find %s.", status.getReasonPhrase(), message));
    }

    /**
     * Okapi request method not requiring a request body. i.e. HEAD, GET, DELETE.
     *
     * @param <T> generic class for response body type.
     * @param url String
     * @param method HttpMethod
     * @param requestEntity The entity containing the tenant properties and authentication token.
     * @param responseType Class<T>
     * @param uriVariables Object... uri variables to be expanded into url.
     *
     * @return response entity with response type as body
     */
    <T> ResponseEntity<T> okapiRequest(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {

        return okapiRequestRetry(1, url, method, requestEntity, responseType, uriVariables);
    }

    /**
     * Okapi request method requiring a request body. i.e. PUT, POST.
     *
     * @param <B> generic class for request body type.
     * @param <T> generic class for response body type.
     * @param url String
     * @param method HttpMethod
     * @param body B request body
     * @param responseType Class<T>
     * @param uriVariables Object... uri variables to be expanded into url.
     *
     * @return response entity with response type as body
     */
    <B,T> ResponseEntity<T> okapiRequest(String url, HttpMethod method, B body, Class<T> responseType, Object... uriVariables) {
        HttpEntity<B> requestEntity = new HttpEntity<>(body, headers(properties.getTenant(), getOkapiToken()));

        return okapiRequestRetry(1, url, method, requestEntity, responseType, uriVariables);
    }

    /**
     * Get request type.
     *
     * @param requestId String
     * @return request type
     */
    private String getRequestType(String requestId) {
        if (Objects.isNull(requestId)) {
            return null;
        }
        JsonNode request = getRequest(requestId);
        JsonNode requestType = request.at("/requestType");
        if (requestType.isValueNode()) {
            return requestType.asText();
        }

        return  null;
    }

    /**
     * Get service point display name.
     *
     * @param servicePointId String
     * @return service point display name
     */
    private String getServicePointDisplayName(String servicePointId) {
        if (Objects.isNull(servicePointId)) {
            return null;
        }
        JsonNode servicePoint = getServicePoint(servicePointId);
        JsonNode discoveryDisplayName = servicePoint.at("/discoveryDisplayName");
        if (discoveryDisplayName.isValueNode()) {
            return discoveryDisplayName.asText();
        }

        return  null;
    }

    /**
     * Get FOLIO User by the user's UIN via OKAPI.
     *
     * @param uin The user's UIN.
     * @return request type
     */
    private JsonNode getUserByUin(String uin) {
        String path = "%s/bl-users?query=(externalSystemId==\"{uin}\")&limit=2";
        String url = String.format(path, properties.getBaseOkapiUrl(), uin);
        String message = "user using external system id";

        logger.debug("Asking for User from: {}", url);

        JsonNode response = okapiRequestJsonNode(url, HttpMethod.GET, message, uin);
        JsonNode users = response.at("/compositeUsers");

        if (users.isArray()) {
            int numOfUsers = ((ArrayNode) users).size();

            if (numOfUsers == 1) {
                return users.get(0).at("/users");
            } else if (numOfUsers == 0) {
                HttpStatus status = HttpStatus.NOT_FOUND;
                throw new HttpClientErrorException(status, String.format("%s: Unable to find user.", status.getReasonPhrase()));
            } else if (numOfUsers > 1) {
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Found multiple users with the same external system id");
            }
        }

        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Unable to get user by external system id");
    }

    /**
     * Get FOLIO automated block status via OKAPI.
     *
     * This method does not take into consideration the type of block.
     * A block can be for any combination of borrowing, renewals, and requests.
     *
     * @param userId The user's UIN.
     * @return automated block status
     */
    private boolean getAutomatedBlockStatus(String userId) {
        String path = "%s/automated-patron-blocks/%s";
        String url = String.format(path, properties.getBaseOkapiUrl(), userId);
        String message = String.format("automated block status for the user id");

        logger.debug("Asking for Automated Block Status from: {}", url);

        JsonNode response = okapiRequestJsonNode(url, HttpMethod.GET, message);

        JsonNode blocks = response.at("/automatedPatronBlocks");

        return blocks.isArray() && ((ArrayNode) blocks).size() > 0;
    }

    /**
     * Get FOLIO manual block status via OKAPI.
     *
     * This method does not take into consideration the type of block.
     * A block can be for any combination of borrowing, renewals, and requests.
     *
     * @param userId The user's UIN.
     * @return manual block status
     */
    private boolean getManualBlockStatus(String userId) {
        String path = "%s/manualblocks?query=userId==%s";
        String url = String.format(path, properties.getBaseOkapiUrl(), userId);
        String message = String.format("automated block status for the user id");

        logger.debug("Asking for Manual Block Status from: {}", url);

        JsonNode response = okapiRequestJsonNode(url, HttpMethod.GET, message);

        JsonNode blocks = response.at("/manualblocks");

        return blocks.isArray() && ((ArrayNode) blocks).size() > 0;
    }

    /**
     * Process the Holdings.
     *
     * @param instanceId The instance ID.
     * @param holdingId The holdings ID.
     *
     * @return list of holdings records.
     *
     * @throws HoldingsRequestError
     * @throws RemoteServerError
     */
    private List<HoldingsRecord> requestHoldings(String instanceId, String holdingId) throws HoldingsRequestError, RemoteServerError {
        List<HoldingsRecord> finalHoldings = new ArrayList<>();

        try {
            String apiKey = properties.getEdgeApiKey();
            String repositoryBaseUrl = properties.getRepositoryBaseUrl();
            String tenant = properties.getTenant();

            String identifier = String.format("%s:%s:%s/%s", NODE_OAI, repositoryBaseUrl, tenant, instanceId);
            String queryString = String.format("verb=%s&metadataPrefix=%s&apikey=%s&identifier=%s", VERB_GET_RECORD, METADATA_PREFIX, apiKey, identifier);

            String oaiPath = "oai";

            String url = String.format("%s/%s?%s", properties.getBaseEdgeUrl(), oaiPath, queryString);

            logger.debug("Asking for edge holdings from: {}", url);

            String result = restGet(url, String.class);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new StringReader(result)));

            doc.getDocumentElement().normalize();

            NodeList errorNodes = doc.getElementsByTagName(NODE_ERROR);

            if (errorNodes != null && errorNodes.getLength() > 0) {
                throw new HoldingsRequestError(errorNodes, getName());
            }

            NodeList verbNodes = doc.getElementsByTagName(VERB_GET_RECORD);

            List<HoldingsRecord> marcHoldings = new ArrayList<>();

            if (verbNodes.getLength() > 0) {

                // there should only be a single getRecord element, only get the first one even if more than one exist.
                NodeList recordNodes = verbNodes.item(0).getChildNodes();

                for (int i = 0; i < recordNodes.getLength(); i++) {
                    Node metadataNode = Marc21Xml.getFirstNamedChildNode(recordNodes.item(i), NODE_METADATA);
                    List<HoldingsRecord> recordHoldings = processMetadata(instanceId, metadataNode);

                    if (recordHoldings.size() > 0) {
                        if (holdingId == null) {
                            marcHoldings.addAll(recordHoldings);
                        } else {
                            for (int j = 0; j < recordHoldings.size(); j++) {
                                if (recordHoldings.get(j).getMfhd().equalsIgnoreCase(holdingId)) {
                                    marcHoldings.add(recordHoldings.get(j));
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            JsonNode okapiHoldings = getOkapiHoldings(instanceId);

            if (okapiHoldings != null) {
              okapiHoldings.forEach(holding -> {
                  String hrid = holding.at("/hrid").asText();
                  JsonNode holdingLocationNode = getLocation(holding.at("/permanentLocationId").asText());
                  String fallbackLocationCode = holdingLocationNode.at("/code").asText();
                  String holdingLocationName = holdingLocationNode.at("/discoveryDisplayName").asText();
                  String holdingCallNumber = holding.at("/callNumber").asText();
                  String holdingCallNumberPrefix = holding.at("/callNumberPrefix").asText();

                  List<Note> holdingNotes = new ArrayList<Note>();
                  holding.at("/notes").forEach(note -> {
                      holdingNotes.add(Note.builder().note(note.at("/note").asText()).isStaffOnly(note.at("/staffOnly").asBoolean())
                          .noteTypeId(note.at("/holdingsNoteTypeId").asText()).build());
                  });

                  List<String> holdingStatements = new ArrayList<String>();

                  ArrayNode holdingsStatements = (ArrayNode) holding.at("/holdingsStatements");
                  logger.info("{} {} is still empty", holdingsStatements, holdingsStatements.isEmpty());

                  holdingsStatements.forEach(statementNode -> {
                      if (statementNode.has("statement")) {
                          holdingStatements.add(statementNode.get("statement").asText());
                      } else {
                          logger.info("Missing statement on holdings statement.");
                      }
                  });

                  //get items for holding from okapi
                  Map<String, Map<String,String>> okapiItems = getOkapiItems(holding.at("/id").asText());

                  //combine marc based holding data and direct okapi data
                  HoldingsRecord recordValues = marcHoldings.get(0);
                  HoldingsRecord currentHolding = HoldingsRecord.builder()
                                  .recordId(recordValues.getRecordId())
                                  .marcRecordLeader(recordValues.getMarcRecordLeader())
                                  .mfhd(hrid)
                                  .issn(recordValues.getIssn())
                                  .isbn(recordValues.getIsbn())
                                  .title(recordValues.getTitle())
                                  .author(recordValues.getAuthor())
                                  .publisher(recordValues.getPublisher())
                                  .place(recordValues.getPlace())
                                  .year(recordValues.getYear())
                                  .genre(recordValues.getGenre())
                                  .fallbackLocationCode(fallbackLocationCode)
                                  .holdingLocation(holdingLocationName)
                                  .edition(recordValues.getEdition())
                                  .oclc(recordValues.getOclc())
                                  .recordId(recordValues.getRecordId())
                                  .callNumber(holdingCallNumber)
                                  .callNumberPrefix(holdingCallNumberPrefix)
                                  .largeVolume(recordValues.isLargeVolume())
                                  .catalogItems(okapiItems.size() > 0 ? okapiItems:recordValues.getCatalogItems())
                                  .holdingNotes(holdingNotes)
                                  .holdingStatements(holdingStatements)
                                  .build();

                  finalHoldings.add(currentHolding);

                  logger.debug("Record ID: {}", currentHolding.getRecordId());
                  logger.debug("Marc record leader: {}", currentHolding.getMarcRecordLeader());
                  logger.debug("MFHD: {}", currentHolding.getMfhd());
                  logger.debug("ISBN: {}", currentHolding.getIsbn());
                  logger.debug("Fallback location: {}", currentHolding.getFallbackLocationCode());
                  logger.debug("Call number: {} {}", holdingCallNumberPrefix, holdingCallNumber);
                  logger.debug("Valid large volume: {}", currentHolding.isLargeVolume());
              });
            }

        } catch (DOMException | IOException | ParserConfigurationException | SAXException e) {
            // TODO: consider throwing all of these so that caller can handle more appropriately.
            e.printStackTrace();
        }

        return finalHoldings;
    }

    private JsonNode getOkapiLoanType(String loanTypeId) {
        String url = String.format("%s/loan-types/%s", properties.getBaseOkapiUrl(), loanTypeId);
        logger.debug("Asking for loan type from: {}", url);
        JsonNode response = okapiRequestJsonNode(url, HttpMethod.GET, "loan type from okapi");
        if (response.isObject()) {
            return response;
        }
        return null;
    }

    private JsonNode getOkapiHoldings(String instanceId) {
        String url = String.format("%s/holdings-storage/holdings", properties.getBaseOkapiUrl());
        String query = String.format("(instanceId==\"%s\" NOT discoverySuppress==true)", instanceId);
        url += String.format("?query={query}&offset={offset}&limit={limit}");
        String message = String.format("holdings from okapi with instanceId \"%s\"", instanceId);
        String offset = "0";
        String limit = "1000";
        logger.debug("Asking for okapi holdings from: {}", url);
        JsonNode response = okapiRequestJsonNode(url, HttpMethod.GET, message, query, offset, limit);
        if (response.isObject()) {
            return response.at("/holdingsRecords");
        }
        return null;
    }

    private Map<String, Map<String, String>> getOkapiItems(String holdingsRecordId) {
        String itemsUrl = String.format("%s/item-storage/items", properties.getBaseOkapiUrl());
        String itemsQuery = String.format("(holdingsRecordId==\"%s\" NOT discoverySuppress==true)", holdingsRecordId);
        itemsUrl += String.format("?query={itemsQuery}&offset={itemsOffset}&limit={itemsLimit}");
        String itemsMessage = String.format("items from okapi with holdingsRecordId \"%s\"", holdingsRecordId);

        String itemsOffset = "0";
        String itemsLimit = "2000";

        logger.debug("Asking for items from: {}", itemsUrl);
        JsonNode itemsResponse = okapiRequestJsonNode(itemsUrl, HttpMethod.GET, itemsMessage, itemsQuery, itemsOffset, itemsLimit);
        Map<String, Map<String, String>> okapiItems = new HashMap<String, Map<String, String>>();
        if (itemsResponse.isObject()) {
            itemsResponse.at("/items").forEach(i -> {
                JsonNode itemLocationNode = getLocation(i.at("/effectiveLocationId").asText());
                JsonNode loanType = getOkapiLoanType(i.at("/permanentLoanTypeId").asText());
                Map<String, String> itemData = new HashMap<String, String>();
                itemData.put("hrid", i.at("/hrid").asText());
                itemData.put("barcode", i.at("/barcode").asText());
                itemData.put("locationCode", itemLocationNode.at("/code").asText());
                itemData.put("location", itemLocationNode.at("/discoveryDisplayName").asText());
                itemData.put("enumeration", i.at("/enumeration").asText());
                itemData.put("chron", i.at("/chronology").asText());
                itemData.put("status", i.at("/status/name").asText());
                itemData.put("typeDesc", loanType.at("/name").asText());
                itemData.put("callNumber", i.at("/effectiveCallNumberComponents/callNumber").asText());
                itemData.put("callNumberPrefix", i.at("/effectiveCallNumberComponents/prefix").asText());
                okapiItems.put(i.at("/hrid").asText(), itemData);
            });
        }
        return okapiItems;
    }

    /**
     * Process the metadata.
     *
     * @param instanceId String
     * @param metadataNode Node
     *
     * @return list of holdings records
     */
    private List<HoldingsRecord> processMetadata(String instanceId, Node metadataNode) {
        List<HoldingsRecord> holdings = new ArrayList<HoldingsRecord>();

        if (metadataNode != null) {
            NodeList childNodes = metadataNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                if (nodeNameMatches(childNodes.item(i).getNodeName(), NODE_RECORD)) {
                    holdings.add(processMarcRecord(instanceId, childNodes.item(i)));
                }
            }
        }

        return holdings;
    }

    /**
     * Process the Marc Record.
     *
     * @param instanceId String
     * @param marcRecord Node
     *
     * @return holdings record
     */
    private HoldingsRecord processMarcRecord(String instanceId, Node marcRecord) {
        Map<String, String> recordValues = new HashMap<>();
        Map<String, String> recordBackupValues = new HashMap<>();

        NodeList marcList = marcRecord.getChildNodes();
        int marcListCount = marcList.getLength();


        for (int i = 0; i < marcList.getLength(); i++) {
            Node node = marcList.item(i);
            if (nodeNameMatches(node.getNodeName(), NODE_LEADER)) {
                Marc21Xml.addMapValue(recordValues, NODE_MARC_RECORD_LEADER, node.getTextContent());
                break;
            }
        }

        for (int i = 0; i < marcList.getLength(); i++) {
            Node node = marcList.item(i);

            if (nodeNameMatches(node.getNodeName(), NODE_CONTROL_FIELD)) {
                Marc21Xml.addControlFieldRecord(node, recordValues);
            }
        }

        for (int i = 0; i < marcList.getLength(); i++) {
            Node node = marcList.item(i);

            if (nodeNameMatches(node.getNodeName(), NODE_DATA_FIELD)) {
                Marc21Xml.addDataFieldRecord(node, recordValues, recordBackupValues);
            }
        }

        // apply backup values if needed and available
        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);

        // TODO: the current implementation of buildCoreHolding() expects a slightly
        // different nesting structure in the XML.
        Map<String, String> holdingValues = Marc21Xml.buildCoreHolding(NODE_PREFIX, marcRecord);
        Map<String, Map<String, String>> catalogItems = new HashMap<String, Map<String, String>>();

        for (int i = 0; i < marcListCount; i++) {
            if (nodeNameMatches(marcList.item(i).getNodeName(), NODE_DATA_FIELD) &&
                Marc21Xml.attributeTagMatches(marcList.item(i), "952")) {
                NodeList childNodes = marcList.item(i).getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    if (Marc21Xml.attributeCodeMatches(childNodes.item(j), "e")) {
                        holdingValues.put(RECORD_CALL_NUMBER, childNodes.item(j).getTextContent());
                    } else if (Marc21Xml.attributeCodeMatches(childNodes.item(j), "m")) {
                        buildCoreItem(instanceId, childNodes.item(j).getTextContent(), childNodes, catalogItems);
                        break;
                    }
                }
            }
        }

        Boolean validLargeVolume = Boolean.valueOf(holdingValues.get(RECORD_VALID_LARGE_VOLUME));

        return HoldingsRecord.builder()
            .recordId(recordValues.get(RECORD_RECORD_ID))
            .marcRecordLeader(recordValues.get(RECORD_MARC_RECORD_LEADER))
            .mfhd(holdingValues.get(RECORD_MFHD))
            .issn(recordValues.get(RECORD_ISSN))
            .isbn(recordValues.get(RECORD_ISBN))
            .title(recordValues.get(RECORD_TITLE))
            .author(recordValues.get(RECORD_AUTHOR))
            .publisher(recordValues.get(RECORD_PUBLISHER))
            .place(recordValues.get(RECORD_PLACE))
            .year(recordValues.get(RECORD_YEAR))
            .genre(recordValues.get(RECORD_GENRE))
            .fallbackLocationCode(holdingValues.get(RECORD_FALLBACK_LOCATION_CODE))
            .edition(recordValues.get(RECORD_EDITION))
            .oclc(recordValues.get(RECORD_OCLC))
            .callNumber(holdingValues.get(RECORD_CALL_NUMBER))
            .largeVolume(validLargeVolume)
            .catalogItems(catalogItems)
            .build();
    }

    /**
     * Attempt to (case-insensitively) match the tag name (nodeName) against the
     * desired match with the marc prefix.
     *
     * @param nodeName String
     * @param matchName String
     *
     * @return whether node name matches
     */
    private boolean nodeNameMatches(String nodeName, String matchName) {
        return nodeName.equalsIgnoreCase(NODE_PREFIX + matchName);
    }

    /**
     * Build the core item, based on the current information we can get from folio.
     *
     * @param instanceId String
     * @param barcode String
     * @param nodes NodeList
     * @param catalogItems Map<String, Map<String, String>>
     */
    private void buildCoreItem(String instanceId, String barcode, NodeList nodes, Map<String, Map<String, String>> catalogItems) {
        Map<String, String> itemData = new HashMap<String, String>();

        itemData.put("bibId", instanceId);
        itemData.put("itemBarcode", barcode);

        for (int i = 0; i < nodes.getLength(); i++) {
            if (Marc21Xml.attributeCodeMatches(nodes.item(i), "d")) {
                itemData.put("locationName", nodes.item(i).getTextContent());
            } else if (Marc21Xml.attributeCodeMatches(nodes.item(i), "k")) {
                itemData.put("enumeration", nodes.item(i).getTextContent());
            }
        }

        catalogItems.put(barcode, itemData);
    }

    /**
     * Get instance hrid by instance id.
     *
     * @param instanceId
     * @return instance hrid or null
     * @throws Exception
     */
    private String getInstanceHrid(String instanceId) throws Exception {
        JsonNode instance = getInstance(instanceId);
        JsonNode hrid = instance.at("/hrid");
        if (hrid.isValueNode()) {
            return hrid.asText();
        }

        return null;
    }

    /**
     * Get loans from a set of ids.
     *
     * @param loanIds
     * @return ArrayNode of loans
     * @throws Exception
     */
    private JsonNode getLoans(Set<String> loanIds) throws Exception {
        ArrayNode loans = objectMapper.createArrayNode();
        AtomicInteger counter = new AtomicInteger();
        Collection<List<String>> loanIdsPartitions = loanIds.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / MAX_BATCH_SIZE))
            .values();
        for (List<String> loanIdsBatch : loanIdsPartitions) {
            loans.addAll((ArrayNode) fetchLoans(new HashSet<String>(loanIdsBatch)));
        }

       return loans;
    }

    /**
     * Fetch batch of loans.
     *
     * @param loanIdsPartitions Set<String>
     * @return ArrayNode of loans
     * @throws Exception
     */
    private JsonNode fetchLoans(Set<String> loanIdsPartitions) throws Exception {
        String baseOkapiUrl = properties.getBaseOkapiUrl();
        Integer limit = loanIdsPartitions.size();
        String ids = String.join(" OR ", loanIdsPartitions);
        String url = String.format("%s/circulation/loans?limit={limit}&query=id==({ids})", baseOkapiUrl);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers(properties.getTenant(), getOkapiToken()));

        ResponseEntity<JsonNode> response = okapiRequest(url, HttpMethod.GET, requestEntity, JsonNode.class, limit, ids);

        if (response != null) {
          JsonNode node = response.getBody();

          if (node != null) {
            JsonNode loansNode = node.get("loans");

            if (loansNode != null && loansNode.isArray()) {
              return loansNode;
            }
          }
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get instances from a set of ids.
     *
     * @param instanceIds
     * @return ArrayNode of instances
     * @throws Exception
     */
    private JsonNode getInstances(Set<String> instanceIds) throws Exception {
        ArrayNode instances = objectMapper.createArrayNode();
        AtomicInteger counter = new AtomicInteger();
        Collection<List<String>> instanceIdsPartitions = instanceIds.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / MAX_BATCH_SIZE))
            .values();
        for (List<String> instanceIdsBatch : instanceIdsPartitions) {
            instances.addAll((ArrayNode) fetchInstances(new HashSet<String>(instanceIdsBatch)));
        }

       return instances;
    }

    /**
     * Fetch batch of instances.
     *
     * @param instanceIdsBatch Set<String>
     * @return ArrayNode of instances
     * @throws Exception
     */
    private JsonNode fetchInstances(Set<String> instanceIdsBatch) throws Exception {
        String baseOkapiUrl = properties.getBaseOkapiUrl();
        Integer limit = instanceIdsBatch.size();
        String ids = String.join(" OR ", instanceIdsBatch);
        String url = String.format("%s/instance-storage/instances?limit={limit}&query=id==({ids})", baseOkapiUrl);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers(properties.getTenant(), getOkapiToken()));

        ResponseEntity<JsonNode> response = okapiRequest(url, HttpMethod.GET, requestEntity, JsonNode.class, limit, ids);

        if (response != null) {
          JsonNode node = response.getBody();

          if (node != null) {
            JsonNode instancesNode = node.get("instances");

            if (instancesNode != null && instancesNode.isArray()) {
              return instancesNode;
            }
          }
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get items from a set of ids.
     *
     * @param itemIds
     * @return ArrayNode of items
     * @throws Exception
     */
    private JsonNode getItems(Set<String> itemIds) throws Exception {
        ArrayNode items = objectMapper.createArrayNode();
        AtomicInteger counter = new AtomicInteger();
        Collection<List<String>> itemIdsPartitions = itemIds.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / MAX_BATCH_SIZE))
            .values();
        for (List<String> itemIdsBatch : itemIdsPartitions) {
            items.addAll((ArrayNode) fetchItems(new HashSet<String>(itemIdsBatch)));
        }

       return items;
    }

    /**
     * Fetch batch of items.
     *
     * @param itemIdsBatch Set<String>
     * @return ArrayNode of items
     * @throws Exception
     */
    private JsonNode fetchItems(Set<String> itemIdsBatch) throws Exception {
        String baseOkapiUrl = properties.getBaseOkapiUrl();
        Integer limit = itemIdsBatch.size();
        String ids = String.join(" OR ", itemIdsBatch);
        String url = String.format("%s/inventory/items?limit={limit}&query=id==({ids})", baseOkapiUrl);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers(properties.getTenant(), getOkapiToken()));

        ResponseEntity<JsonNode> response = okapiRequest(url, HttpMethod.GET, requestEntity, JsonNode.class, limit, ids);

        if (response != null) {
          JsonNode node = response.getBody();

          if (node != null) {
            JsonNode itemsNode = node.get("items");

            if (itemsNode != null && itemsNode.isArray()) {
              return itemsNode;
            }
          }
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get instance by hrid.
     *
     * @param hrid String
     * @return instance
     */
    private JsonNode getInstanceByHrid(String hrid) throws Exception {
        String url = String.format("%s/instance-storage/instances?query=hrid==\"%s\"", properties.getBaseOkapiUrl(), hrid);
        String message = String.format("instance with hrid \"%s\"", hrid);

        logger.debug("Asking for instance from: {}", url);

        JsonNode instance = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (instance.isContainerNode()) {
            return instance;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get instance by id.
     *
     * @param instanceId String
     * @return instance
     */
    private JsonNode getInstance(String instanceId) throws Exception {
        String url = String.format("%s/instance-storage/instances/%s", properties.getBaseOkapiUrl(), instanceId);
        String message = String.format("user with instanceId \"%s\"", instanceId);

        logger.debug("Asking for instance from: {}", url);

        JsonNode instance = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (instance.isContainerNode()) {
            return instance;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get item by id.
     *
     * @param itemId String
     * @return item
     */
    private JsonNode getItem(String itemId) throws Exception {
        String url = String.format("%s/inventory/items/%s", properties.getBaseOkapiUrl(), itemId);
        String message = String.format("user with itemId \"%s\"", itemId);

        logger.debug("Asking for item from: {}", url);

        JsonNode item = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (item.isContainerNode()) {
            return item;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get loan by id.
     *
     * @param loanId String
     * @return loan
     */
    private JsonNode getLoan(String loanId) {
        String url = String.format("%s/circulation/loans/%s", properties.getBaseOkapiUrl(), loanId);
        String message = String.format("loan with id \"%s\"", loanId);

        logger.debug("Asking for loan from: {}", url);

        JsonNode loan = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (loan.isContainerNode()) {
            return loan;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get request by id.
     *
     * @param requestId String
     * @return request
     */
    private JsonNode getRequest(String requestId) {
        String url = String.format("%s/circulation/requests/%s", properties.getBaseOkapiUrl(), requestId);
        String message = String.format("hold request with id \"%s\"", requestId);

        logger.debug("Asking for request from: {}", url);

        JsonNode request = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (request.isContainerNode()) {
            return request;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get and cache location by id.
     *
     * @param locationId String
     * @return location
     */
    private JsonNode getLocation(String locationId) {
        if (LOCATION_CACHE.containsKey(locationId)) {
            return LOCATION_CACHE.get(locationId);
        }
        String url = String.format("%s/locations/%s", properties.getBaseOkapiUrl(), locationId);
        String message = String.format("location with id \"%s\"", locationId);

        logger.debug("Asking for location from: {}", url);

        JsonNode location = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (location.isContainerNode()) {
            LOCATION_CACHE.put(locationId, location);
            return location;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get and cache service point by id.
     *
     * @param servicePointId String
     * @return service point
     */
    private JsonNode getServicePoint(String servicePointId) {
        if (SERVICE_POINT_CACHE.containsKey(servicePointId)) {
            return SERVICE_POINT_CACHE.get(servicePointId);
        }
        String url = String.format("%s/service-points/%s", properties.getBaseOkapiUrl(), servicePointId);
        String message = String.format("service point with id \"%s\"", servicePointId);

        logger.debug("Asking for service point from: {}", url);

        JsonNode servicePoint = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (servicePoint.isContainerNode()) {
            SERVICE_POINT_CACHE.put(servicePointId, servicePoint);
            return servicePoint;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get and cache loan policy by name.
     *
     * @param loanPolicyName String
     * @return loan policy
     */
    private JsonNode getLoanPolicy(String loanPolicyName) {
        if (LOAN_POLICY_CACHE.containsKey(loanPolicyName)) {
            return LOAN_POLICY_CACHE.get(loanPolicyName);
        }
        String url = String.format("%s/loan-policy-storage/loan-policies?query=name=={loanPolicyName}", properties.getBaseOkapiUrl());
        String message = String.format("loan policy with name \"%s\"", loanPolicyName);

        logger.debug("Asking for loan policy from: {}", url);

        JsonNode loanPolicyCollection = okapiRequestJsonNode(url, HttpMethod.GET, message, loanPolicyName);

        if (loanPolicyCollection.isContainerNode()) {
            JsonNode totalRecords = loanPolicyCollection.at("/totalRecords");
            if (totalRecords.isValueNode() && totalRecords.intValue() == 1) {
                JsonNode loanPolicy = ((ArrayNode) loanPolicyCollection.at("/loanPolicies")).get(0);
                LOAN_POLICY_CACHE.put(loanPolicyName, loanPolicy);
                return loanPolicy;
            }
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get parsed data time from JsonNode at path expression. Return null if value not found.
     *
     * @param input JsonNode
     * @param jsonPtrExpr String
     * @return date time value
     * @throws Exception
     */
    private Date getDate(JsonNode input, String jsonPtrExpr) throws Exception {
        if (input == null) return null;

        JsonNode property = input.at(jsonPtrExpr);
        return property.isValueNode() ? FolioDateTime.parse(property.asText()) : null;
    }

    /**
     * Okapi request method to attempt one token refresh and retry if request unauthorized with retry support.
     *
     * Do not pass "{baseOkapiUrl}".
     * Instead, convert it before sending it to this function because restTemplate.exchange() is always forcing a leading '/'.
     * If "{baseOkapiUrl}" is not pre-converted, then you might get something like "/http://example.com/".
     *
     * @param <T> generic class for response body type
     * @param attempt int
     * @param url String
     * @param method HttpMethod
     * @param requestEntity HttpEntity<T>
     * @param responseType Class<T>
     * @param uriVariables Object... uri variables to be expanded into url
     *
     * @return response entity with response type as body
     */
    private <T> ResponseEntity<T> okapiRequestRetry(int attempt, String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        try {
          return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        } catch(RestClientResponseException e) {
            if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value() && attempt == 1) {
                requestEntity = new HttpEntity<>(requestEntity.getBody(), headers(properties.getTenant(), getOkapiToken()));

                return okapiRequestRetry(++attempt, url, method, requestEntity, responseType, uriVariables);
            }

            throw e;
        }
    }

    /**
     * Retrieve the FOLIO tokens, which may be cached.
     *
     * This performs a login request if the tokens are either not cached or expired.
     *
     * @return The FOLIO tokens.
     */
    private FolioTokens getToken() {
        final FolioTokens tokens = FolioTokenUtility.getTokens(getName());
        final ZonedDateTime offsetTime = ZonedDateTime.now().plusSeconds(tokenConfig.getExpireOffset());

        if (tokens == null || tokens.getAccess().getExpire().isBefore(offsetTime)) {
            return okapiLogin();
        }

        return tokens;
    }

    /**
     * Get the Access token as the OKAPI token.
     *
     * @return The FOLIO tokens.
     */
    private String getOkapiToken() {
        return getToken().getAccess().getToken();
    }

    /**
     * Log into Okapi.
     *
     * @return The FOLIO tokens.
     *
     * @throws HttpServerErrorException on Login failure.
     */
    private FolioTokens okapiLogin() {
        String url = properties.getBaseOkapiUrl() + tokenConfig.getLoginPath();

        try {
          HttpEntity<Credentials> entity = new HttpEntity<>(properties.getCredentials(), headers(properties.getTenant()));
          ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);

          if (response.getStatusCode().equals(HttpStatus.CREATED)) {
              FolioTokens folioTokens = null;

              for (Map.Entry<String, List<String>> map : response.getHeaders().entrySet()) {
                  if (SET_COOKIE_HEADER.equalsIgnoreCase(map.getKey())) {
                      folioTokens = extractFolioTokensByName(map.getValue());

                      if (folioTokens != null) {
                          FolioTokenUtility.setTokens(getName(), folioTokens);

                          return folioTokens;
                      }
                  }
              }

              logger.error("Failed to login, missing/invalid token headers: {} and {}.", tokenConfig.getAccessCookieName(),
                  tokenConfig.getRefreshCookieName());
          } else {
              logger.error("Failed to login {}: {}", response.getStatusCodeValue(), response.getBody());
          }
        }
        catch (Exception e) {
          logger.error("Catalog service failed to login into Okapi: " + e.getMessage() + "!");
        }

        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Catalog service failed to login into Okapi!");
    }

    /**
     * Build the headers containing the Okapi token.
     *
     * @param tenant The tenant name.
     * @param token The token associated with the tenant.
     *
     * @return the headers.
     */
    private HttpHeaders headers(String tenant, String token) {
        HttpHeaders headers = headers(tenant);
        headers.set(tokenConfig.getHeaderName(), token);
        return headers;
    }

    /**
     * Build the headers containing the Okapi tenant.
     * This assumes all accept and content type will be application/json.
     *
     * @param tenant The tenant name.
     *
     * @return the headers.
     */
    private HttpHeaders headers(String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(tenantConfig.getHeaderName(), tenant);
        return headers;
    }

    /**
     * Extract the FOLIO tokens from the list of headers.
     *
     * This is provided because java.net.HttpCookie fails to provide access to the "Expires" cookie field.
     *
     * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
     *
     * @return The FOLIO tokens, if found, or NULL otherwise.
     */
    private FolioTokens extractFolioTokensByName(List<String> headers) {
        if (headers != null) {
            FolioToken access = null;
            FolioToken refresh = null;

            for (String header : headers) {
                Boolean isAccess = null;
                String token = null;
                String expires = null;

                for (String field : header.split(";")) {
                    String[] parts = field.split("=", 2);

                    if (parts.length > 1) {
                        if (tokenConfig.getAccessCookieName().equalsIgnoreCase(parts[0].trim())) {
                            isAccess = true;
                            token = parts[1].trim();
                        } else if (tokenConfig.getRefreshCookieName().equalsIgnoreCase(parts[0].trim())) {
                            isAccess = false;
                            token = parts[1].trim();
                        } else if (EXPIRES.equalsIgnoreCase(parts[0].trim())) {
                            expires = parts[1].trim();
                        }
                    }
                }

                if (isAccess != null && token != null && expires != null) {
                    ZonedDateTime expireDate = FolioDateTime.parseZonedDateTime(expires);

                    if (isAccess) {
                        access = new FolioToken(token, expireDate);
                    } else {
                        refresh = new FolioToken(token, expireDate);
                    }
                }
            }

            if (access != null && refresh != null) {
                return new FolioTokens(access, refresh);
            }
        }

        return null;
    }

    /**
     * Perform the request request, catching the exceptions.
     *
     * Catch the remote server client and server exceptions and throw a more controlled one.
     * This ensures that a proper JSON error response is returned with more detailed system logging. 
     *
     * @param <T> The response type.
     * @param url The request URL.
     * @param responseType The response type class.
     * @param uriVariables Any additional variables.
     *
     * @return The response result.
     * @throws RemoteServerError
     */
    private <T> T restGet(String url, Class<T> responseType, Object... uriVariables) throws RemoteServerError {
        try {
            return restTemplate.getForObject(url, responseType, uriVariables);
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            throw new RemoteServerError("GET", url, getName(), ex.getStatusCode(), ex.getMessage());
        }
    }

}
