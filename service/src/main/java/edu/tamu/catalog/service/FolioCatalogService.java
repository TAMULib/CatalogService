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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.tamu.catalog.domain.model.FeeFine;
import edu.tamu.catalog.domain.model.FeesFines;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.properties.CatalogServiceProperties;
import edu.tamu.catalog.properties.FolioProperties;
import edu.tamu.catalog.utility.Marc21Xml;

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
    public FeesFines getFeesFines(String uin) throws ParseException {
        String path = "patron/account";
        String queryString = "apikey={apikey}&includeLoans=false&includeCharges=true&includeHolds=false";
        String url = String.format("%s/%s/%s?%s", properties.getBaseEdgeUrl(), path, uin, queryString);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for fines from: {}", url);

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

        Double total = null;
        if (node.has("totalCharges") && node.get("totalCharges").has("amount")) {
            total = node.get("totalCharges").get("amount").asDouble();
        }

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
                Date date = charge.has("accrualDate") ? folioDateToDate(charge.get("accrualDate").asText()) : null;

                String title = null;
                if (charge.has("item") && charge.get("item").has("title")) {
                    title = charge.get("item").get("title").asText();
                }

                list.add(new FeeFine(amount, fineId, type, date, title));
            }
        }

        return new FeesFines(uin, total, list.size(), list);
    }

    @Override
    public List<LoanItem> getLoanItems(String uin) throws ParseException {
        String path = "patron/account";
        String additional = "&includeLoans=true&includeCharges=false&includeHolds=false";
        String url = String.format("%s/%s/%s?apikey={apikey}%s", properties.getBaseEdgeUrl(), path, uin, additional);
        String apiKey = properties.getEdgeApiKey();

        logger.debug("Asking for patron loans from: {}", url);

        JsonNode node = restTemplate.getForObject(url, JsonNode.class, apiKey);

        List<LoanItem> list = new ArrayList<LoanItem>();

        if (node.has("loans")) {
            Iterator<JsonNode> iter = node.get("loans").elements();

            while (iter.hasNext()) {
                JsonNode loan = iter.next();
                if (loan.has("item")) {
                    JsonNode item = loan.get("item");

                    Date loanDate = loan.has("loanDate") ? folioDateToDate(loan.get("loanDate").asText()) : null;
                    Date loanDueDate = loan.has("dueDate") ? folioDateToDate(loan.get("dueDate").asText()) : null;
                    String overDueString = getNodeValue(loan, "overdue");
                    boolean overDue = false;
                    if (overDueString != null) {
                        overDue = Boolean.valueOf(overDueString);
                    }

                    String loanId = getNodeValue(loan, "id");
                    String itemId = getNodeValue(item, "itemId");
                    String instanceId = getNodeValue(item, "instanceId");
                    String title = getNodeValue(item, "title");
                    String author = getNodeValue(item, "author");

                    list.add(new LoanItem(loanId, itemId, instanceId, loanDate, loanDueDate, overDue, title, author));
                }
            }
        }
        return list;
    }

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
     */
    private boolean nodeNameMatches(String nodeName, String matchName) {
        return nodeName.equalsIgnoreCase(NODE_PREFIX + matchName);
    }

    /**
     * Build the core item, based on the current information we can get from folio.
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

    private String getNodeValue(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : null;
    }

    /**
     * Convert from Folio dates, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", to the Java Date.
     *
     * @param folioDate
     * @return
     * @throws ParseException
     */
    private Date folioDateToDate(String folioDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return Date.from(formatter.parse(folioDate).toInstant());
    }

}
