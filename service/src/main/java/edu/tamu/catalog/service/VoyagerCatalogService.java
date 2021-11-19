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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.tamu.catalog.domain.model.FeeFine;
import edu.tamu.catalog.domain.model.HoldRequest;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;
import edu.tamu.catalog.properties.CatalogServiceProperties;
import edu.tamu.catalog.properties.VoyagerProperties;
import edu.tamu.catalog.utility.Marc21Xml;
import edu.tamu.weaver.utility.HttpUtility;

/**
 * A CatalogService implementation for interfacing with the Voyager REST VXWS api
 *
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */
public class VoyagerCatalogService implements CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(VoyagerCatalogService.class);

    private static final int REQUEST_TIMEOUT = 120000;

    // TODO: use RestTemplate instead of HttpUtility

    private VoyagerProperties properties;

    public VoyagerCatalogService(CatalogServiceProperties properties) {
        this.properties = (VoyagerProperties) properties;
        this.properties.setBaseUrl(StringUtils.removeEnd(this.properties.getBaseUrl(), "/"));
    }

    @Override
    public String getName() {
        return properties.getName();
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

            String url = properties.getBaseUrl() + "/record/" + bibId + "/holdings?view=items";

            logger.debug("Asking for holdings from: {}", url);
            String result = HttpUtility.makeHttpRequest(url, "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new StringReader(result)));

            doc.getDocumentElement().normalize();
            NodeList holdings = doc.getElementsByTagName("holding");
            int holdingCount = holdings.getLength();

            List<HoldingsRecord> catalogHoldings = new ArrayList<HoldingsRecord>();
            logger.debug("The holdings count: {}", holdingCount);

            for (int i = 0; i < holdingCount; i++) {
                logger.debug("Current holdings: {}", holdings.item(i).getAttributes().getNamedItem("href").getTextContent());
                Map<String, String> holdingValues = Marc21Xml.buildCoreHolding(holdings.item(i));

                logger.debug("Marc record leader: {}", recordValues.get(RECORD_MARC_RECORD_LEADER));
                logger.debug("MFHD: {}", holdingValues.get(RECORD_MFHD));
                logger.debug("ISBN: {}", recordValues.get(RECORD_ISBN));
                logger.debug("Fallback location: {}", holdingValues.get(RECORD_FALLBACK_LOCATION_CODE));
                logger.debug("Call number: {}", holdingValues.get(RECORD_CALL_NUMBER));

                Boolean validLargeVolume = Boolean.valueOf(holdingValues.get(RECORD_VALID_LARGE_VOLUME));

                logger.debug("Valid large volume: {}", validLargeVolume);

                Map<String, Map<String, String>> catalogItems = new HashMap<String, Map<String, String>>();

                NodeList childNodes = holdings.item(i).getChildNodes();
                int childCount = childNodes.getLength();

                if (validLargeVolume) {
                    // when we have a lot of items and it's a large volume candidate, just use the
                    // item data that came with the holding response, even though it's incomplete data
                    for (int j = 0; j < childCount; j++) {
                        if (childNodes.item(j) != null && childNodes.item(j).getNodeName() == "item") {
                            String itemUrl = childNodes.item(j).getAttributes().getNamedItem("href").getTextContent();
                            catalogItems.put(itemUrl, Marc21Xml.buildCoreItem(childNodes.item(j)));
                        }
                    }
                } else {
                    if (childNodes.item(1) != null) {
                        logger.debug("Item URL: {}", childNodes.item(1).getAttributes().getNamedItem("href").getTextContent());
                    }

                    for (int j = 0; j < childCount; j++) {
                        if (childNodes.item(j) != null && childNodes.item(j).getNodeName() == "item") {
                            String itemUrl = childNodes.item(j).getAttributes().getNamedItem("href").getTextContent();
                            String itemResult = HttpUtility.makeHttpRequest(itemUrl, "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);

                            logger.debug("Got item details from: {}", url);
                            doc = dBuilder.parse(new InputSource(new StringReader(itemResult)));
                            doc.getDocumentElement().normalize();
                            NodeList itemNodes = doc.getElementsByTagName("item");

                            int itemNodesCount = itemNodes.getLength();
                            for (int l = 0; l < itemNodesCount; l++) {
                                catalogItems.put(itemUrl, Marc21Xml.buildCoreItem(itemNodes.item(l)));
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

                HoldingsRecord holdingsRecord = HoldingsRecord.builder()
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
                    .holdingLocation(holdingValues.get(RECORD_FALLBACK_LOCATION_CODE))
                    .largeVolume(validLargeVolume)
                    .catalogItems(catalogItems)
                    .build();

                catalogHoldings.add(holdingsRecord);
            }
            return catalogHoldings;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HoldingsRecord getHolding(String bibId, String holdingId) {
        String url = properties.getBaseUrl() + "/record/" + bibId + "/holdings?view=items";
        logger.debug("Asking for holdings from: {}", url);
        try {
            Map<String, String> recordValues = buildCoreRecord(bibId);
            String result = HttpUtility.makeHttpRequest(url, "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);

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
                    String itemUrl = childNodes.item(j).getAttributes().getNamedItem("href").getTextContent();
                    catalogItems.put(itemUrl, Marc21Xml.buildCoreItem(childNodes.item(j)));
                }
            }

            return new HoldingsRecord(recordValues.get(RECORD_MARC_RECORD_LEADER), holdingValues.get(RECORD_MFHD),
                recordValues.get(RECORD_ISSN), recordValues.get(RECORD_ISBN), recordValues.get(RECORD_TITLE),
                recordValues.get(RECORD_AUTHOR), recordValues.get(RECORD_PUBLISHER), recordValues.get(RECORD_PLACE),
                recordValues.get(RECORD_YEAR), recordValues.get(RECORD_GENRE), recordValues.get(RECORD_EDITION),
                holdingValues.get(RECORD_FALLBACK_LOCATION_CODE), recordValues.get(RECORD_OCLC),
                recordValues.get(RECORD_RECORD_ID), holdingValues.get(RECORD_CALL_NUMBER), holdingValues.get(RECORD_FALLBACK_LOCATION_CODE),
                Boolean.valueOf(holdingValues.get(RECORD_VALID_LARGE_VOLUME)),
                new HashMap<String, Map<String, String>>(catalogItems));

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<FeeFine> getFeesFines(String uin) throws Exception {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

    @Override
    public List<LoanItem> getLoanItems(String uin) throws Exception {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

    @Override
    public List<HoldRequest> getHoldRequests(String uin) throws Exception {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

    @Override
    public void cancelHoldRequest(String uin, String requestId) throws Exception {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

    @Override
    public LoanItem renewItem(String uin, String itemId) {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

    @Override
    public Boolean getBlockStatus(String uin) throws Exception {
        throw new UnsupportedOperationException("Not supported by the requested catalog.");
    }

    private Map<String, String> buildCoreRecord(String bibId) {
        String url = properties.getBaseUrl() + "/record/" + bibId + "/?view=full";
        logger.debug("Asking for Record from: {}", url);
        try {
            String recordResult = HttpUtility.makeHttpRequest(url, "GET", Optional.empty(), Optional.empty(), REQUEST_TIMEOUT);
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

    private void addMapValue(Map<String, String> map, String key, String newValue) {
        map.put(key, (newValue != null ? newValue : ""));
    }

}
