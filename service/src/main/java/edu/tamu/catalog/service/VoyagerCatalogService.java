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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.tamu.catalog.domain.model.FeesFines;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.properties.CatalogServiceProperties;
import edu.tamu.catalog.properties.VoyagerProperties;
import edu.tamu.catalog.utility.Marc21Xml;
import edu.tamu.weaver.utility.HttpUtility;

/**
 * A CatalogService implementation for interfacing with the Voyager REST VXWS
 * api
 *
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */
public class VoyagerCatalogService implements CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(FolioCatalogService.class);

    private static final int REQUEST_TIMEOUT = 120000;

    // TODO: use RestTemplate instead of HttpUtility

    private VoyagerProperties properties;

    public VoyagerCatalogService(CatalogServiceProperties properties) {
        this.properties = (VoyagerProperties) properties;
    }

    @Override
    public String getName() {
        return properties.getName();
    }

    private void addMapValue(Map<String, String> map, String key, String newValue) {
        map.put(key, (newValue != null ? newValue : ""));
    }

    protected Map<String, String> buildCoreRecord(String bibId) {
        logger.debug("Asking for Record from: " + properties.getBaseUrl() + "/record/" + bibId + "/?view=full");
        try {
            String recordResult = HttpUtility.makeHttpRequest(properties.getBaseUrl() + "/record/" + bibId + "/?view=full",
                    "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new StringReader(recordResult)));

            doc.getDocumentElement().normalize();

            NodeList dataFields = doc.getElementsByTagName("datafield");
            int dataFieldCount = dataFields.getLength();

            Map<String, String> recordValues = new HashMap<String, String>();
            Map<String, String> recordBackupValues = new HashMap<String, String>();

            addMapValue(recordValues, RECORD_MARC_RECORD_LEADER, doc.getElementsByTagName("leader").item(0).getTextContent());
            NodeList controlFields = doc.getElementsByTagName("controlfield");
            int controlFieldsCount = controlFields.getLength();

            for (int i = 0; i < controlFieldsCount; i++) {
                Marc21Xml.addControlFieldRecord(controlFields.item(i), recordValues);
            }

            for (int i = 0; i < dataFieldCount; i++) {
                Marc21Xml.addDataFieldRecord(dataFields.item(i), recordValues, recordBackupValues);
            }

            // apply backup values if needed and available
            Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);

            return recordValues;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetches holdings from the Voyager API and translates them into
     * catalogHoldings
     *
     * @param bibId String
     *
     * @return List<CatalogHolding>
     *
     */
    @Override
    public List<HoldingsRecord> getHoldingsByBibId(String bibId) {
        try {
            Map<String, String> recordValues = buildCoreRecord(bibId);

            logger.debug("Asking for holdings from: " + properties.getBaseUrl() + "/record/" + bibId + "/holdings?view=items");
            String result = HttpUtility.makeHttpRequest(properties.getBaseUrl() + "/record/" + bibId + "/holdings?view=items",
                    "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);
            logger.debug("Received holdings from: " + properties.getBaseUrl() + "/record/" + bibId + "/holdings?view=items");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new StringReader(result)));

            doc.getDocumentElement().normalize();
            NodeList holdings = doc.getElementsByTagName("holding");
            int holdingCount = holdings.getLength();

            List<HoldingsRecord> catalogHoldings = new ArrayList<HoldingsRecord>();
            logger.debug("\n\nThe Holding Count: " + holdingCount);

            for (int i = 0; i < holdingCount; i++) {
                logger.debug("Current Holding: " + holdings.item(i).getAttributes().getNamedItem("href").getTextContent());
                Map<String, String> holdingValues = Marc21Xml.buildCoreHolding(holdings.item(i));

                logger.debug("MarcRecordLeader: " + recordValues.get(RECORD_MARC_RECORD_LEADER));
                logger.debug("MFHD: " + holdingValues.get(RECORD_MFHD));
                logger.debug("ISBN: " + recordValues.get(RECORD_ISBN));
                logger.debug("Fallback Location: " + holdingValues.get(RECORD_FALLBACK_LOCATION_CODE));
                logger.debug("Call Number: " + holdingValues.get(RECORD_CALL_NUMBER));

                Boolean validLargeVolume = Boolean.valueOf(holdingValues.get(RECORD_VALID_LARGE_VOLUME));

                logger.debug("Valid Large Volume: " + validLargeVolume);

                Map<String, Map<String, String>> catalogItems = new HashMap<String, Map<String, String>>();

                NodeList childNodes = holdings.item(i).getChildNodes();
                int childCount = childNodes.getLength();

                if (validLargeVolume) {
                    // when we have a lot of items and it's a large volume candidate, just use the
                    // item data that came with the holding response, even though it's incomplete data
                    for (int j = 0; j < childCount; j++) {
                        if (childNodes.item(j) != null && childNodes.item(j).getNodeName() == "item") {
                            catalogItems.put(childNodes.item(j).getAttributes().getNamedItem("href").getTextContent(),
                                    Marc21Xml.buildCoreItem(childNodes.item(j)));
                        }
                    }
                } else {
                    if (childNodes.item(1) != null) {
                        logger.debug("Item URL: " + childNodes.item(1).getAttributes().getNamedItem("href").getTextContent());
                    }

                    for (int j = 0; j < childCount; j++) {
                        if (childNodes.item(j) != null && childNodes.item(j).getNodeName() == "item") {
                            String itemResult = HttpUtility.makeHttpRequest(childNodes.item(j).getAttributes().getNamedItem("href").getTextContent(),
                                    "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);

                            logger.debug("Got Item details from: "
                                    + childNodes.item(j).getAttributes().getNamedItem("href").getTextContent());
                            doc = dBuilder.parse(new InputSource(new StringReader(itemResult)));
                            doc.getDocumentElement().normalize();
                            NodeList itemNodes = doc.getElementsByTagName("item");

                            int itemNodesCount = itemNodes.getLength();
                            for (int l = 0; l < itemNodesCount; l++) {
                                catalogItems.put(childNodes.item(j).getAttributes().getNamedItem("href").getTextContent(),
                                        Marc21Xml.buildCoreItem(itemNodes.item(l)));
                            }
                            // sleep for a moment between item requests to avoid triggering a 429 from the Voyager API
                            try {
                                TimeUnit.MILLISECONDS.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                catalogHoldings.add(new HoldingsRecord(recordValues.get(RECORD_MARC_RECORD_LEADER),
                        holdingValues.get(RECORD_MFHD), recordValues.get(RECORD_ISSN), recordValues.get(RECORD_ISBN),
                        recordValues.get(RECORD_TITLE), recordValues.get(RECORD_AUTHOR),
                        recordValues.get(RECORD_PUBLISHER), recordValues.get(RECORD_PLACE),
                        recordValues.get(RECORD_YEAR), recordValues.get(RECORD_GENRE), recordValues.get(RECORD_EDITION),
                        holdingValues.get(RECORD_FALLBACK_LOCATION_CODE), recordValues.get(RECORD_OCLC),
                        recordValues.get(RECORD_RECORD_ID), holdingValues.get(RECORD_CALL_NUMBER), validLargeVolume,
                        new HashMap<String, Map<String, String>>(catalogItems)));
                catalogItems.clear();
            }
            return catalogHoldings;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HoldingsRecord getHolding(String bibId, String holdingId) {
        logger.debug("Asking for holding from: " + properties.getBaseUrl() + "/record/" + bibId + "/holdings?view=items");
        try {
            Map<String, String> recordValues = buildCoreRecord(bibId);
            String result = HttpUtility.makeHttpRequest(properties.getBaseUrl() + "/record/" + bibId + "/holdings?view=items",
                    "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new StringReader(result)));

            doc.getDocumentElement().normalize();

            Node holdingNode = null;
            NodeList controlFieldNodes = doc.getElementsByTagName("controlfield");
            for (int i = 0; i < controlFieldNodes.getLength(); i++) {
                Node controlFieldNode = controlFieldNodes.item(i);
                if (controlFieldNode != null) {
                    NamedNodeMap controlFieldAttributes = controlFieldNode.getAttributes();
                    for (int j = 0; j < controlFieldAttributes.getLength(); j++) {
                        if (controlFieldAttributes.item(j) != null &&
                            controlFieldAttributes.item(j).getNodeName() == "tag" &&
                            controlFieldAttributes.item(j).getTextContent().equals("001")) {
                            if (controlFieldNode.getTextContent().equals(holdingId)) {
                                holdingNode = controlFieldNode.getParentNode().getParentNode();
                            }
                        }
                    }
                }
            }

            Map<String, String> holdingValues = Marc21Xml.buildCoreHolding(holdingNode);

            Map<String, Map<String, String>> catalogItems = new HashMap<String, Map<String, String>>();

            NodeList childNodes = holdingNode.getChildNodes();
            int childCount = childNodes.getLength();

            for (int j = 0; j < childCount; j++) {
                if (childNodes.item(j) != null && childNodes.item(j).getNodeName() == "item") {
                    catalogItems.put(childNodes.item(j).getAttributes().getNamedItem("href").getTextContent(),
                            Marc21Xml.buildCoreItem(childNodes.item(j)));
                }
            }

            return new HoldingsRecord(recordValues.get(RECORD_MARC_RECORD_LEADER), holdingValues.get(RECORD_MFHD),
                    recordValues.get(RECORD_ISSN), recordValues.get(RECORD_ISBN), recordValues.get(RECORD_TITLE),
                    recordValues.get(RECORD_AUTHOR), recordValues.get(RECORD_PUBLISHER), recordValues.get(RECORD_PLACE),
                    recordValues.get(RECORD_YEAR), recordValues.get(RECORD_GENRE), recordValues.get(RECORD_EDITION),
                    holdingValues.get(RECORD_FALLBACK_LOCATION_CODE), recordValues.get(RECORD_OCLC),
                    recordValues.get(RECORD_RECORD_ID), holdingValues.get(RECORD_CALL_NUMBER),
                    Boolean.valueOf(holdingValues.get(RECORD_VALID_LARGE_VOLUME)),
                    new HashMap<String, Map<String, String>>(catalogItems));

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public FeesFines getFeesFines(String uin) throws Exception {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

    @Override
    public List<LoanItem> getLoanItems(String uin) throws Exception {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

}
