package edu.tamu.app.service;

import static edu.tamu.app.utility.Marc21Xml.RECORD_AUTHOR;
import static edu.tamu.app.utility.Marc21Xml.RECORD_CALL_NUMBER;
import static edu.tamu.app.utility.Marc21Xml.RECORD_EDITION;
import static edu.tamu.app.utility.Marc21Xml.RECORD_FALLBACK_LOCATION_CODE;
import static edu.tamu.app.utility.Marc21Xml.RECORD_GENRE;
import static edu.tamu.app.utility.Marc21Xml.RECORD_ISBN;
import static edu.tamu.app.utility.Marc21Xml.RECORD_ISSN;
import static edu.tamu.app.utility.Marc21Xml.RECORD_MARC_RECORD_LEADER;
import static edu.tamu.app.utility.Marc21Xml.RECORD_MFHD;
import static edu.tamu.app.utility.Marc21Xml.RECORD_OCLC;
import static edu.tamu.app.utility.Marc21Xml.RECORD_PLACE;
import static edu.tamu.app.utility.Marc21Xml.RECORD_PUBLISHER;
import static edu.tamu.app.utility.Marc21Xml.RECORD_RECORD_ID;
import static edu.tamu.app.utility.Marc21Xml.RECORD_TITLE;
import static edu.tamu.app.utility.Marc21Xml.RECORD_VALID_LARGE_VOLUME;
import static edu.tamu.app.utility.Marc21Xml.RECORD_YEAR;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.tamu.app.model.CatalogHolding;
import edu.tamu.app.utility.Marc21Xml;

class FolioCatalogService extends AbstractCatalogService {
    private static final String VERB_GET_RECORD = "GetRecord";
    private static final String METADATA_PREFIX = "marc21_withholdings";
    private static final String ERROR_ATTR_CODE = "code";

    private static final String NODE_PREFIX = "marc:";
    private static final String NODE_CONTROL_FIELD = "controlfield";
    private static final String NODE_DATA_FIELD = "datafield";
    private static final String NODE_ERROR = "error";
    private static final String NODE_HOLDING= "holding";
    private static final String NODE_LEADER = "leader";
    private static final String NODE_MARC_RECORD_LEADER = "marcRecordLeader";
    private static final String NODE_METADATA = "metadata";
    private static final String NODE_OAI = "oai";
    private static final String NODE_RECORD = "record";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RestTemplate restTemplate;

    public FolioCatalogService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CatalogHolding> getHoldingsByBibId(String instanceId) {
        return requestHoldings(instanceId, null);
    }

    @Override
    public CatalogHolding getHolding(String instanceId, String holdingId) {
        List<CatalogHolding> holdings = requestHoldings(instanceId, holdingId);

        if (holdings.size() > 0) {
            return holdings.get(0);
        }

        return null;
    }

    private String httpRequest(String instanceId) throws IOException {
        Map<String, String> authentication = getAuthentication();
        String apiKey = authentication.get(CatalogServiceFactory.FIELD_APIKEY);
        String repositoryBaseUrl = authentication.get(CatalogServiceFactory.FIELD_REPOSITORY_BASE_URL);
        String tenant = authentication.get(CatalogServiceFactory.FIELD_TENANT);

        String identifier = String.format("%s:%s:%s/%s", NODE_OAI, repositoryBaseUrl, tenant, instanceId);
        String queryString = String.format("verb=%s&metadataPrefix=%s&apikey=%s&identifier=%s",
          VERB_GET_RECORD, METADATA_PREFIX, apiKey, identifier);

        String url = String.format("%s?%s", getAPIBase(), queryString);

        logger.debug("Asking for holdings from: " + url);

        return restTemplate.getForObject(url, String.class);
    }

    private List<CatalogHolding> requestHoldings(String instanceId, String holdingId) {
        List<CatalogHolding> holdings = new ArrayList<>();

        try {
            String result = httpRequest(instanceId);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new StringReader(result)));

            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName(NODE_ERROR);

            // TODO: this potentially has one or more errors, be sure to determine how to handle the "or more" part.
            if (nodes.getLength() > 0) {
                Node node = nodes.item(0);
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
                    if (nodeNameMatches(recordNodes.item(i).getNodeName(), NODE_RECORD)) {
                        Node metadataNode = Marc21Xml.getFirstNamedChildNode(recordNodes.item(i), NODE_METADATA);
                        List<CatalogHolding> recordHoldings = processMetadata(metadataNode);

                        if (recordHoldings.size() > 0) {
                            if (holdingId == null) {
                                holdings.addAll(recordHoldings);
                            }
                            else {
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
            }
        } catch (DOMException | IOException | ParserConfigurationException | SAXException e) {
            // TODO Auto-generated catch block
            // TODO consider throwing all of these so that caller can handle more appropriately.
            e.printStackTrace();
        }

        return holdings;
    }

    private List<CatalogHolding> processMetadata(Node metadataNode) {

        if (metadataNode != null) {
            NodeList childNodes = metadataNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                if (nodeNameMatches(childNodes.item(i).getNodeName(), NODE_RECORD)) {
                    return processMarc(childNodes.item(i).getChildNodes());
                }
            }
        }

        return new ArrayList<CatalogHolding>();
    }

    private List<CatalogHolding> processMarc(NodeList marcList) {
        List<CatalogHolding> holdings = new ArrayList<>();

        Map<String, String> recordValues = new HashMap<>();
        Map<String, String> recordBackupValues = new HashMap<>();
        List<Node> holdingNodes = new ArrayList<>();

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
            else if (nodeNameMatches(node.getNodeName(), NODE_HOLDING)) {
                holdingNodes.add(node);
            }
        }

        //apply backup values if needed and available
        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);

        logger.debug("\n\nThe Holding Count: " + holdingNodes.size());

        for (int i = 0; i < holdingNodes.size(); i++) {
            Node holdingNode = holdingNodes.get(i);

            logger.debug("Current Holding: " + holdingNode.getAttributes().getNamedItem("href").getTextContent());
            Map<String, String> holdingValues = Marc21Xml.buildCoreHolding(NODE_PREFIX, holdingNode);

            logger.debug("MarcRecordLeader: " + recordValues.get(RECORD_MARC_RECORD_LEADER));
            logger.debug("MFHD: " + holdingValues.get(RECORD_MFHD));
            logger.debug("ISBN: " + recordValues.get(RECORD_ISBN));
            logger.debug("Fallback Location: " + holdingValues.get(RECORD_FALLBACK_LOCATION_CODE));
            logger.debug("Call Number: " + holdingValues.get(RECORD_CALL_NUMBER));

            Boolean validLargeVolume = Boolean.valueOf(holdingValues.get(RECORD_VALID_LARGE_VOLUME));

            logger.debug("Valid Large Volume: "+ validLargeVolume);

            Map<String, Map<String, String>> catalogItems = new HashMap<String, Map<String, String>>();

            NodeList childNodes = holdingNode.getChildNodes();
            int childCount = childNodes.getLength();

            for (int j = 0; j < childCount; j++) {
                if (childNodes.item(j) != null && childNodes.item(j).getNodeName() == "item") {
                    catalogItems.put(childNodes.item(j).getAttributes().getNamedItem("href").getTextContent(), Marc21Xml.buildCoreItem(childNodes.item(j)));
                }
            }

            holdings.add(new CatalogHolding(recordValues.get(RECORD_MARC_RECORD_LEADER), holdingValues.get(RECORD_MFHD),
                recordValues.get(RECORD_ISSN), recordValues.get(RECORD_ISBN), recordValues.get(RECORD_TITLE),
                recordValues.get(RECORD_AUTHOR), recordValues.get(RECORD_PUBLISHER), recordValues.get(RECORD_PLACE),
                recordValues.get(RECORD_YEAR), recordValues.get(RECORD_GENRE), recordValues.get(RECORD_EDITION),
                holdingValues.get(RECORD_FALLBACK_LOCATION_CODE), recordValues.get(RECORD_OCLC), recordValues.get(RECORD_RECORD_ID),
                holdingValues.get(RECORD_CALL_NUMBER), validLargeVolume, new HashMap<String, Map<String, String>>(catalogItems)));

            catalogItems.clear();
        }

        return holdings;
    }

    /**
     * Attempt to (case-insensitively) match the tag name (nodeName) against the desired match with the marc prefix.
     */
    private boolean nodeNameMatches(String nodeName, String matchName) {
      return nodeName.equalsIgnoreCase(NODE_PREFIX + matchName);
    }

}
