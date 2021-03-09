package edu.tamu.catalog.service;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.fasterxml.jackson.databind.JsonNode;

import edu.tamu.catalog.domain.model.FeeFine;
import edu.tamu.catalog.domain.model.HoldRequest;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.model.FolioHoldCancellation;
import edu.tamu.catalog.properties.CatalogServiceProperties;
import edu.tamu.catalog.properties.Credentials;
import edu.tamu.catalog.properties.FolioProperties;
import edu.tamu.catalog.utility.FolioDatetime;
import edu.tamu.catalog.utility.Marc21Xml;
import edu.tamu.catalog.utility.TokenUtility;

public class FolioCatalogService implements CatalogService {

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

    @Autowired
    private RestTemplate restTemplate;

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
    public List<FeeFine> getFeesFines(String uin) throws ParseException {
        String path = "patron/account";
        String queryString = "apikey={apikey}&includeLoans=false&includeCharges=true&includeHolds=false";
        String url = String.format("%s/%s/%s?%s", properties.getBaseEdgeUrl(), path, uin, queryString);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for fines from: {}", url);

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

        List<FeeFine> list = new ArrayList<>();

        if (node.has("charges")) {
            Iterator<JsonNode> iter = node.get("charges").elements();

            while (iter.hasNext()) {
                JsonNode charge = iter.next();

                double amount = 0;
                if (charge.has("chargeAmount") && charge.get("chargeAmount").has("amount")) {
                    amount = charge.get("chargeAmount").get("amount").asDouble();
                }

                String fineId = charge.has("feeFineId") ? charge.get("feeFineId").asText() : null;
                String type = charge.has("reason") ? charge.get("reason").asText() : null;
                Date date = charge.has("accrualDate") ? FolioDatetime.parse(charge.get("accrualDate").asText()) : null;

                String title = null;
                if (charge.has("item") && charge.get("item").has("title")) {
                    title = charge.get("item").get("title").asText();
                }

                list.add(new FeeFine(amount, fineId, type, date, title));
            }
        }

        return list;
    }

    @Override
    public List<LoanItem> getLoanItems(String uin) throws ParseException {
        String path = "patron/account";
        String additional = "&includeLoans=true&includeCharges=false&includeHolds=false";
        String url = String.format("%s/%s/%s?apikey={apikey}%s", properties.getBaseEdgeUrl(), path, uin, additional);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for patron loans from: {}", url);

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

        List<LoanItem> list = new ArrayList<>();

        if (node.has("loans")) {
            Iterator<JsonNode> iter = node.get("loans").elements();

            while (iter.hasNext()) {
                JsonNode loan = iter.next();
                LoanItem loanItem = buildLoanItem(loan);
                if (loanItem.getItemId() != null) {
                    list.add(loanItem);
                }
            }
        }
        return list;
    }

    @Override
    public List<HoldRequest> getHoldRequests(String uin) throws Exception {
        String path = "patron/account";
        String additional = "&includeLoans=false&includeCharges=false&includeHolds=true";
        String url = String.format("%s/%s/%s?apikey={apikey}%s", properties.getBaseEdgeUrl(), path, uin, additional);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for patron hold requests from: {}", url);

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

        List<HoldRequest> list = new ArrayList<>();

        if (node.has("holds")) {
            Iterator<JsonNode> iter = node.get("holds").elements();

            while (iter.hasNext()) {
                JsonNode hold = iter.next();

                String requestId = getNodeValue(hold, "requestId");
                String itemId = getNodeNestedValue(hold, "item", "itemId");
                String title = getNodeNestedValue(hold, "item", "title");
                String status = getNodeValue(hold, "status");
                Integer queuePosition = hold.has("queuePosition") ? hold.get("queuePosition").asInt() : null;
                Date date = getNodeDateValue(hold, "expirationDate");

                // Note: the "pickupLocationId" holds the "servicePointId" UUID.
                String servicePointId = getNodeValue(hold, "pickupLocationId");

                String requestUrl = requestId == null ? null : String.format("%s/circulation/requests/%s", properties.getBaseOkapiUrl(), requestId);
                String type = null;

                logger.debug("Asking for hold request from: {}", requestUrl);
                String message = String.format("hold request with id \"%s\"", requestId);
                JsonNode requestNode = okapiRequestJsonNode(requestUrl, HttpMethod.GET, message);
                if (requestNode != null) {
                    type = getNodeValue(requestNode, "requestType");
                }

                String sercicePointUrl = servicePointId == null ? null : String.format("%s/service-points/%s", properties.getBaseOkapiUrl(), servicePointId);
                String pickupServicePoint = null;

                logger.debug("Asking for service-point from: {}", sercicePointUrl);
                message = String.format("service point with id \"%s\"", servicePointId);
                JsonNode servicePointNode = okapiRequestJsonNode(sercicePointUrl, HttpMethod.GET, message);
                if (servicePointNode != null) {
                    pickupServicePoint = getNodeValue(servicePointNode, "discoveryDisplayName");
                }

                list.add(new HoldRequest(requestId, itemId, type, title, status, pickupServicePoint, queuePosition, date));
            }
        }

        return list;
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
            return okapiRequest(url, method, JsonNode.class).getBody();
        }
        catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s.", e.getStatusText(), message));
        }
        catch (HttpServerErrorException e) {
            throw new HttpServerErrorException(e.getStatusCode(),
                String.format("%s: Catalog service failed to find %s.", e.getStatusText(), message));
        }
    }

    @Override
    public void cancelHoldRequest(String uin, String requestId) throws Exception {
        String path = "%s/patron/account/%s/holds/%s/cancel?apikey={apikey}";
        String url = String.format(path, properties.getBaseEdgeUrl(), uin, requestId);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Cancelling hold request via: {}", url);

        FolioHoldCancellation folioCancellation = new FolioHoldCancellation();
        folioCancellation.setHoldId(requestId);
        folioCancellation.setCancellationReasonId(properties.getCancelHoldReasonId());
        folioCancellation.setCanceledDate(FolioDatetime.convert(new Date()));

        restTemplate.postForObject(url, folioCancellation, Object.class, apiKey);
    }

    @Override
    public LoanItem renewItem(String uin, String itemId) throws ParseException {
        String path = "/patron/account/";
        String itemPath = "/item/";
        String renewPath = "/renew";
        String url = String.format("%s%s%s%s%s%s?apikey={apikey}", properties.getBaseEdgeUrl(), path, uin, itemPath, itemId, renewPath);
        String apiKey = properties.getEdgeApiKey();

        return buildLoanItem(restTemplate.postForObject(url, null, JsonNode.class, apiKey));
    }

    @Override
    public Boolean getBlockStatus(String uin) throws Exception {
        //for now, no FOLIO user will be blocked
        //leave the exception throw so the interface is future stable
        return false;
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
     * Process the Holdings.
     *
     * @param instanceId
     * @param holdingId
     *
     * @return
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
            // TODO consider throwing all of these so that caller can handle more appropriately.
            e.printStackTrace();
        }

        return holdings;
    }

    /**
     * Process the metadata.
     *
     * @param instanceId
     * @param metadataNode
     *
     * @return
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
     * @param instanceId
     * @param marcRecord
     *
     * @return
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

        return new HoldingsRecord(recordValues.get(RECORD_MARC_RECORD_LEADER), holdingValues.get(RECORD_MFHD),
            recordValues.get(RECORD_ISSN), recordValues.get(RECORD_ISBN), recordValues.get(RECORD_TITLE),
            recordValues.get(RECORD_AUTHOR), recordValues.get(RECORD_PUBLISHER), recordValues.get(RECORD_PLACE),
            recordValues.get(RECORD_YEAR), recordValues.get(RECORD_GENRE), recordValues.get(RECORD_EDITION),
            holdingValues.get(RECORD_FALLBACK_LOCATION_CODE), recordValues.get(RECORD_OCLC),
            recordValues.get(RECORD_RECORD_ID), holdingValues.get(RECORD_CALL_NUMBER), validLargeVolume,
            new HashMap<String, Map<String, String>>(catalogItems));
    }

    /**
     * Attempt to (case-insensitively) match the tag name (nodeName) against the
     * desired match with the marc prefix.
     *
     * @param nodeName
     * @param matchName
     *
     * @return
     */
    private boolean nodeNameMatches(String nodeName, String matchName) {
        return nodeName.equalsIgnoreCase(NODE_PREFIX + matchName);
    }

    /**
     * Build the core item, based on the current information we can get from folio.
     *
     * @param instanceId
     * @param barcode
     * @param nodes
     * @param catalogItems
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
     * Build the loan item, based on the current information we can get from folio.
     *
     * @param loanJson
     */
    private LoanItem buildLoanItem(JsonNode loanJson) throws ParseException {
        Date loanDate = getNodeDateValue(loanJson, "loanDate");
        Date loanDueDate = getNodeDateValue(loanJson, "dueDate");
        String overDueString = getNodeValue(loanJson, "overdue");
        boolean overDue = StringUtils.isNotEmpty(overDueString)
            ? Boolean.valueOf(overDueString)
            : false;

        String loanId = getNodeValue(loanJson, "id");

        String itemId = null;
        String instanceId = null;
        String title = null;
        String author = null;

        if (loanJson.has("item")) {
            JsonNode item = loanJson.get("item");

            itemId = getNodeValue(item, "itemId");
            instanceId = getNodeValue(item, "instanceId");
            title = getNodeValue(item, "title");
            author = getNodeValue(item, "author");
        }

        return new LoanItem(loanId, itemId, instanceId, loanDate, loanDueDate, overDue, title, author);
    }

    /**
     * Get the nested node value as a string.
     *
     * @param node the node.
     * @param parentField the parent field that should contain the child field.
     * @param childField the field within the parent field to get the value of.
     *
     * @return The retrieved string or NULL if the field is not found.
     * @throws ParseException
     */
    private String getNodeNestedValue(JsonNode node, String parentField, String childField) {
        if (node.has(parentField) && node.get(parentField).has(childField)) {
            return node.get(parentField).get(childField).asText();
        }

        return null;
    }

    /**
     * Get the node value as a string.
     *
     * @param node the node.
     * @param fieldName the field to get the value of.
     *
     * @return The retrieved string or NULL if the field is not found.
     * @throws ParseException
     */
    private String getNodeValue(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : null;
    }

    /**
     * Get the node date value as a date.
     *
     * @param node the node.
     * @param fieldName the field to get the value of.
     * @return The retrieved date or NULL if the field is not found.
     *
     * @throws ParseException
     */
    private Date getNodeDateValue(JsonNode node, String fieldName) throws ParseException {
        return node.has(fieldName) ? FolioDatetime.parse(node.get(fieldName).asText()) : null;
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

        if (response != null && response.getStatusCode().equals(HttpStatus.CREATED)) {
            String token = response.getHeaders().getFirst(OKAPI_TOKEN_HEADER);
            TokenUtility.setToken(getName(), token);
            return token;
        } else {
            Integer statusCode = response == null ? null : response.getStatusCodeValue();
            Object body = response == null ? null : response.getBody();
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
