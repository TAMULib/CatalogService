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

import java.io.IOException;
import java.io.StringReader;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

import edu.tamu.catalog.domain.model.FeeFine;
import edu.tamu.catalog.domain.model.HoldRequest;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.exception.RenewFailureException;
import edu.tamu.catalog.model.FolioHoldCancellation;
import edu.tamu.catalog.properties.CatalogServiceProperties;
import edu.tamu.catalog.properties.Credentials;
import edu.tamu.catalog.properties.FolioProperties;
import edu.tamu.catalog.utility.FolioDateTime;
import edu.tamu.catalog.utility.Marc21Xml;
import edu.tamu.catalog.utility.TokenUtility;

public class FolioCatalogService implements CatalogService {

    private static final String RENEWAL_WOULD_NOT_CHANGE_THE_DUE_DATE = "renewal would not change the due date";

    private static final Logger logger = LoggerFactory.getLogger(FolioCatalogService.class);

    private static final String VERB_GET_RECORD = "GetRecord";
    private static final String METADATA_PREFIX = "marc21_withholdings";
    private static final String ERROR_ATTR_CODE = "code";

    private static final String NODE_PREFIX = "marc:";
    private static final String NODE_CONTROL_FIELD = "controlfield";
    private static final String NODE_DATA_FIELD = "datafield";
    private static final String NODE_ERROR = "error";
    private static final String NODE_LEADER = "leader";
    private static final String NODE_MARC_RECORD_LEADER = "marcRecordLeader";
    private static final String NODE_METADATA = "metadata";
    private static final String NODE_OAI = "oai";
    private static final String NODE_RECORD = "record";

    private static final String OKAPI_TENANT_HEADER = "X-Okapi-Tenant";
    private static final String OKAPI_TOKEN_HEADER = "X-Okapi-Token";

    private static final int MAX_BATCH_SIZE = 90;

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

    @Override
    public List<HoldingsRecord> getHoldingsByBibId(String instanceId) {
        return requestHoldings(instanceId, null);
    }

    @Override
    public HoldingsRecord getHolding(String instanceId, String holdingId) {
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

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

        List<FeeFine> list = new ArrayList<>();

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

        return list;
    }

    @Override
    public List<LoanItem> getLoanItems(String uin) throws Exception {
        String path = "patron/account";
        String queryString = "apikey={apikey}&includeLoans=true&includeCharges=false&includeHolds=false";
        String url = String.format("%s/%s/%s?%s", properties.getBaseEdgeUrl(), path, uin, queryString);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for patron loans from: {}", url);

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

        List<LoanItem> list = new ArrayList<>();

        JsonNode loans = node.at("/loans");

        if (loans.isContainerNode() && loans.isArray()) {
            Iterator<JsonNode> iter = loans.elements();
            Map<String, JsonNode> instanceIdToLoan = new HashMap<>();
            Map<String, JsonNode> itemIdToLoan = new HashMap<>();

            while (iter.hasNext()) {
                JsonNode loan = iter.next();
                String instanceId = getText(loan, "/item/instanceId");
                String itemId = getText(loan, "/item/itemId");
                instanceIdToLoan.put(instanceId, loan);
                itemIdToLoan.put(itemId, loan);
            }

            Map<String, JsonNode> instanceIdToInstance = new HashMap<>();
            Map<String, JsonNode> itemIdToItem = new HashMap<>();

            for (JsonNode instance : getInstances(instanceIdToLoan.keySet())) {
                String instanceId = getText(instance, "/id");
                instanceIdToInstance.put(instanceId, instance);
            }

            for (JsonNode item : getItems(itemIdToLoan.keySet())) {
                String itemId = getText(item, "/id");
                itemIdToItem.put(itemId, item);
            }

            for (JsonNode loan : instanceIdToLoan.values()) {
                String instanceId = getText(loan, "/item/instanceId");
                String itemId = getText(loan, "/item/itemId");

                JsonNode instance = instanceIdToInstance.get(instanceId);
                JsonNode item = itemIdToItem.get(itemId);

                String locationId = getText(item, "/effectiveLocation/id");

                LoanItem.LoanItemBuilder builder = LoanItem.builder()
                    .loanId(getText(loan, "/id"))
                    .itemId(itemId)
                    .instanceId(instanceId)
                    .instanceHrid(getText(instance, "/hrid"))
                    .itemType(getText(item, "/permanentLoanType/name"))
                    .loanDate(getDate(loan, "/loanDate"))
                    .loanDueDate(getDate(loan, "/dueDate"))
                    .overdue(getBoolean(loan, "/overdue", false))
                    .title(getText(loan, "/item/title"))
                    .author(getText(loan, "/item/author"))
                    .canRenew(true);

                if (StringUtils.isNotEmpty(locationId)) {
                    JsonNode location = getLocation(locationId);
                    builder.location(getText(location, "/name"))
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

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

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

        JsonNode loan;

        try {
            loan = restTemplate.postForObject(url, null, JsonNode.class, apiKey);
        } catch (RestClientResponseException e) {
            e.printStackTrace();
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
    
        String instanceId = getText(loan, "/item/instanceId");
        String instanceHrid = getInstanceHrid(instanceId);

        LoanItem.LoanItemBuilder builder = LoanItem.builder()
            .loanId(getText(loan, "/id"))
            .itemId(itemId)
            .instanceId(instanceId)
            .instanceHrid(instanceHrid)
            .itemType(getText(item, "/permanentLoanType/name"))
            .loanDate(getDate(loan, "/loanDate"))
            .loanDueDate(getDate(loan, "/dueDate"))
            .overdue(getBoolean(loan, "/overdue", false))
            .title(getText(loan, "/item/title"))
            .author(getText(loan, "/item/author"))
            .canRenew(true);

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
     * @param <T> generic class for response body type.
     * @param url String the URL to retrieve.
     *
     * @return response entity with response type as body.
     */
    JsonNode okapiRequestJsonNode(String url, HttpMethod method, String message) {
        try {
            ResponseEntity<JsonNode> response = okapiRequest(url, method, JsonNode.class);
            if (Objects.nonNull(response) && response.getBody().isContainerNode()) {
                return response.getBody();
            }
        }
        catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s.", e.getStatusText(), message));
        }
        catch (HttpServerErrorException e) {
            throw new HttpServerErrorException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s.", e.getStatusText(), message));
        }
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        throw new HttpServerErrorException(status,
            String.format("%s: Catalog service failed to find %s.", status.getReasonPhrase(), message));
    }

    /**
     * Use OKAPI to retrieve the JsonNode, throwing a customized exception on client or server errors.
     *
     * @param <T> generic class for response body type.
     * @param url String the URL to retrieve.
     *
     * @return response entity with response type as body.
     */
    JsonNode okapiRequestJsonNode(String url, HttpMethod method, String message, Object... uriVariables) {
        try {
            ResponseEntity<JsonNode> response = okapiRequest(url, method, JsonNode.class, uriVariables);
            if (Objects.nonNull(response) && response.getBody().isContainerNode()) {
                return response.getBody();
            }
        }
        catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s.", e.getStatusText(), message));
        }
        catch (HttpServerErrorException e) {
            throw new HttpServerErrorException(e.getStatusCode(),
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
     * @param responseType Class<T>
     * @param uriVariables Object... uri variables to be expanded into url.
     *
     * @return response entity with response type as body
     */
    <T> ResponseEntity<T> okapiRequest(String url, HttpMethod method, Class<T> responseType, Object... uriVariables) {
        HttpEntity<?> requestEntity = new HttpEntity<>(headers(properties.getTenant(), getToken()));

        return okapiRequest(1, url, method, requestEntity, responseType, uriVariables);
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
        HttpEntity<B> requestEntity = new HttpEntity<>(body, headers(properties.getTenant(), getToken()));

        return okapiRequest(1, url, method, requestEntity, responseType, uriVariables);
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
     * @param instanceId String
     * @param holdingId String
     *
     * @return list of holdings records
     */
    private List<HoldingsRecord> requestHoldings(String instanceId, String holdingId) {
        List<HoldingsRecord> holdings = new ArrayList<>();

        try {
            String apiKey = properties.getEdgeApiKey();
            String repositoryBaseUrl = properties.getRepositoryBaseUrl();
            String tenant = properties.getTenant();

            String identifier = String.format("%s:%s:%s/%s", NODE_OAI, repositoryBaseUrl, tenant, instanceId);
            String queryString = String.format("verb=%s&metadataPrefix=%s&apikey=%s&identifier=%s", VERB_GET_RECORD, METADATA_PREFIX, apiKey, identifier);

            String oaiPath = "oai";

            String url = String.format("%s/%s?%s", properties.getBaseEdgeUrl(), oaiPath, queryString);

            logger.debug("Asking for holdings from: {}", url);

            String result = restTemplate.getForObject(url, String.class);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new StringReader(result)));

            doc.getDocumentElement().normalize();

            NodeList errorNodes = doc.getElementsByTagName(NODE_ERROR);

            // TODO: this potentially has one or more errors, be sure to determine how to handle the "or more" part.
            if (errorNodes.getLength() > 0) {
                Node node = errorNodes.item(0);
                Node code = node.getAttributes().getNamedItem(ERROR_ATTR_CODE);

                String codeValue = code == null ? "" : code.getTextContent();
                String nodeValue = node == null ? "" : node.getTextContent();

                // http://www.openarchives.org/OAI/openarchivesprotocol.html#ErrorConditions
                throw new IOException(String.format("Error '%s': %s", codeValue, nodeValue));
            }

            NodeList verbNodes = doc.getElementsByTagName(VERB_GET_RECORD);

            if (verbNodes.getLength() > 0) {

                // there should only be a single getRecord element, only get the first one even if more than one exist.
                NodeList recordNodes = verbNodes.item(0).getChildNodes();

                for (int i = 0; i < recordNodes.getLength(); i++) {
                    Node metadataNode = Marc21Xml.getFirstNamedChildNode(recordNodes.item(i), NODE_METADATA);
                    List<HoldingsRecord> recordHoldings = processMetadata(instanceId, metadataNode);

                    if (recordHoldings.size() > 0) {
                        if (holdingId == null) {
                            holdings.addAll(recordHoldings);
                        } else {
                            for (int j = 0; j < recordHoldings.size(); j++) {
                                if (recordHoldings.get(j).getMfhd().equalsIgnoreCase(holdingId)) {
                                    holdings.add(recordHoldings.get(j));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (DOMException | IOException | ParserConfigurationException | SAXException e) {
            // TODO: consider throwing all of these so that caller can handle more appropriately.
            e.printStackTrace();
        }

        return holdings;
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

        logger.debug("Marc record leader: {}", recordValues.get(RECORD_MARC_RECORD_LEADER));
        logger.debug("MFHD: {}", holdingValues.get(RECORD_MFHD));
        logger.debug("ISBN: {}", recordValues.get(RECORD_ISBN));
        logger.debug("Fallback location: {}", holdingValues.get(RECORD_FALLBACK_LOCATION_CODE));
        logger.debug("Call number: {}", holdingValues.get(RECORD_CALL_NUMBER));

        Boolean validLargeVolume = Boolean.valueOf(holdingValues.get(RECORD_VALID_LARGE_VOLUME));

        logger.debug("Valid large volume: {}", validLargeVolume);

        Map<String, Map<String, String>> catalogItems = new HashMap<String, Map<String, String>>();

        for (int i = 0; i < marcListCount; i++) {
            if (nodeNameMatches(marcList.item(i).getNodeName().toString(), NODE_DATA_FIELD) &&
                Marc21Xml.attributeTagMatches(marcList.item(i), "952")) {

                NodeList childNodes = marcList.item(i).getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    if (Marc21Xml.attributeCodeMatches(childNodes.item(j), "m")) {
                        buildCoreItem(instanceId, childNodes.item(j).getTextContent(), childNodes, catalogItems);
                        break;
                    }
                }
            }
        }

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
            if (Marc21Xml.attributeCodeMatches(nodes.item(i), "c")) {
                itemData.put("location", nodes.item(i).getTextContent());
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
     * @return array of instances
     * @throws Exception
     */
    private JsonNode fetchInstances(Set<String> instanceIdsBatch) throws Exception {
        String baseOkapiUrl = properties.getBaseOkapiUrl();
        Integer limit = instanceIdsBatch.size();
        String ids = String.join(" OR ", instanceIdsBatch);
        String url = "{baseOkapiUrl}/instance-storage/instances?limit={limit}&query=id==({ids})";
        ResponseEntity<JsonNode> response = okapiRequest(url, HttpMethod.GET, JsonNode.class, baseOkapiUrl, limit, ids);
        if (response.hasBody()) {
            JsonNode instancesNode = response.getBody().get("instances");
            if (instancesNode.isArray()) {
                return instancesNode;
            }
        }

        return objectMapper.createObjectNode();
    }

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
     * @return array of items
     * @throws Exception
     */
    private JsonNode fetchItems(Set<String> itemIdsBatch) throws Exception {
        String baseOkapiUrl = properties.getBaseOkapiUrl();
        Integer limit = itemIdsBatch.size();
        String ids = String.join(" OR ", itemIdsBatch);
        String url = "{baseOkapiUrl}/inventory/items?limit={limit}&query=id==({ids})";
        ResponseEntity<JsonNode> response = okapiRequest(url, HttpMethod.GET, JsonNode.class, baseOkapiUrl, limit, ids);
        if (response.hasBody()) {
            JsonNode itemsNode = response.getBody().get("items");
            if (itemsNode.isArray()) {
                return itemsNode;
            }
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Get and cache instance by id.
     *
     * @param instanceId String
     * @return instance
     */
    @Cacheable(value = "instanceCache", unless = "#result.isMissingNode()")
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
     * Get and cache item by id.
     *
     * @param itemId String
     * @return item
     */
    @Cacheable(value = "itemCache", unless = "#result.isMissingNode()")
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
     * Get and cache request by id.
     *
     * @param requestId String
     * @return request
     */
    @Cacheable(value = "requestCache", unless = "#result.isMissingNode()")
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
    @Cacheable(value = "locationCache", unless = "#result.isMissingNode()")
    private JsonNode getLocation(String locationId) {
        String url = String.format("%s/locations/%s", properties.getBaseOkapiUrl(), locationId);
        String message = String.format("location with id \"%s\"", locationId);

        logger.debug("Asking for location from: {}", url);

        JsonNode location = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (location.isContainerNode()) {
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
    @Cacheable(value = "servicePointCache", unless = "#result.isMissingNode()")
    private JsonNode getServicePoint(String servicePointId) {
        String url = String.format("%s/service-points/%s", properties.getBaseOkapiUrl(), servicePointId);
        String message = String.format("service point with id \"%s\"", servicePointId);

        logger.debug("Asking for service point from: {}", url);

        JsonNode servicePoint = okapiRequestJsonNode(url, HttpMethod.GET, message);
        if (servicePoint.isContainerNode()) {
            return servicePoint;
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
        JsonNode property = input.at(jsonPtrExpr);
        return property.isValueNode() ? FolioDateTime.parse(property.asText()) : null;
    }

    /**
     * Okapi request method to attempt one token refresh and retry if request unauthorized.
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
    private <T> ResponseEntity<T> okapiRequest(int attempt, String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        try {
            return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        } catch(RestClientResponseException e) {
            if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value() && attempt == 1) {
                requestEntity = new HttpEntity<>(requestEntity.getBody(), headers(properties.getTenant(), okapiLogin()));
                return okapiRequest(++attempt, url, method, requestEntity, responseType, uriVariables);
            }
            throw e;
        }
    }

    /**
     * Retrieve the Okapi token, which may be cached.
     *
     * @return the authentication token.
     */
    private String getToken() {
        Optional<String> token = TokenUtility.getToken(getName());
        if (token.isPresent()) {
            return token.get();
        }
        return okapiLogin();
    }

    /**
     * Login to Okapi.
     *
     * @return the authentication token.
     */
    private String okapiLogin() {
        String url = properties.getBaseOkapiUrl() + "/authn/login";
        HttpEntity<Credentials> entity = new HttpEntity<>(properties.getCredentials(), headers(properties.getTenant()));
        ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);

        if (response.getStatusCode().equals(HttpStatus.CREATED)) {
            String token = response.getHeaders().getFirst(OKAPI_TOKEN_HEADER);
            TokenUtility.setToken(getName(), token);
            return token;
        } else {
            Integer statusCode = response.getStatusCodeValue();
            Object body = response.getBody();
            logger.error("Failed to login {}: {}", statusCode, body);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Catalog service failed to login into Okapi!");
        }
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
        headers.set(OKAPI_TOKEN_HEADER, token);
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
        headers.set(OKAPI_TENANT_HEADER, tenant);
        return headers;
    }

}
