package edu.tamu.app.utility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Marc21Xml  {
    public static final String TAG = "tag";

    public static final String RECORD_AUTHOR = "author";
    public static final String RECORD_BOOK = "book";
    public static final String RECORD_CALL_NUMBER = "callNumber";
    public static final String RECORD_CODE = "code";
    public static final String RECORD_EDITION = "edition";
    public static final String RECORD_FALLBACK_LOCATION_CODE = "fallbackLocationCode";
    public static final String RECORD_GENRE = "genre";
    public static final String RECORD_ISBN = "isbn";
    public static final String RECORD_ISSN = "issn";
    public static final String RECORD_JOURNAL = "journal";
    public static final String RECORD_MARC_RECORD_LEADER = "marcRecordLeader";
    public static final String RECORD_MFHD = "mfhd";
    public static final String RECORD_NAME = "name";
    public static final String RECORD_OCLC = "oclc";
    public static final String RECORD_PLACE = "place";
    public static final String RECORD_PUBLISHER = "publisher";
    public static final String RECORD_RECORD_ID = "recordId";
    public static final String RECORD_TITLE = "title";
    public static final String RECORD_VALID_LARGE_VOLUME = "validLargeVolume";
    public static final String RECORD_YEAR = "year";

    private static final String NODE_DATA_FIELD = "datafield";

    private static final Logger logger = LoggerFactory.getLogger(Marc21Xml.class);

    private static final List<String> LARGE_VOLUME_LOCATIONS = Arrays.asList("rs,hdr", "rs,jlf");
    private static final int LARGE_VOLUME_ITEM_LIMIT = 10;

    /**
     * Process the node, adding the appropriate control record values.
     *
     * @param node
     * @param recordValues
     */
    public static void addControlFieldRecord(Node node, Map<String, String> recordValues) {
        if (node.getAttributes().getNamedItem(TAG) != null
            && node.getAttributes().getNamedItem(TAG).getTextContent().contentEquals("001")) {

            addMapValue(recordValues, RECORD_RECORD_ID, node.getChildNodes().item(0).getTextContent());
        }
    }

    /**
     * Process the node, adding the appropriate data record values and backup record values.
     *
     * @param node
     * @param recordValues
     * @param recordBackupValues
     */
    public static void addDataFieldRecord(Node node, Map<String, String> recordValues, Map<String, String> recordBackupValues) {
        NodeList dataNodes = node.getChildNodes();
        List<String> nodeCodes = null;
        int childCount = 0;

        switch (node.getAttributes().getNamedItem(TAG).getTextContent()) {
            case "022":
                if (dataNodes.item(0).getAttributes().getNamedItem(RECORD_CODE).getTextContent().equals("a")) {
                    addMapValue(recordValues, RECORD_ISSN, dataNodes.item(0).getTextContent());
                }
                addMapValue(recordValues, RECORD_GENRE, RECORD_JOURNAL);
            break;
            case "020":
                if (dataNodes.item(0).getAttributes().getNamedItem(RECORD_CODE).getTextContent().equals("a")) {
                    addMapValue(recordValues, RECORD_ISBN, dataNodes.item(0).getTextContent().split(" ")[0]);
                }
                addMapValue(recordValues, RECORD_GENRE, RECORD_BOOK);
            break;
            //the title field may have more than one field, so combine them when it does
            case "245":
                if (node.getChildNodes().item(1) != null) {
                    addMapValue(recordValues, RECORD_TITLE, node.getChildNodes().item(0).getTextContent()
                        + node.getChildNodes().item(1).getTextContent());
                } else {
                    addMapValue(recordValues, RECORD_TITLE, node.getChildNodes().item(0).getTextContent());
                }
            break;
            //The author field is made up of many subfields identified by the xml attribute 'code'
            //  and can appear under 'tag' 100,110,111, or 130
            //We build it up into one field by combining all the node values with a matching 'code'
            //  attribute
            //The valid 'code' letter attributes vary from 'tag' to 'tag'
            case "100":
                childCount = dataNodes.getLength();
                nodeCodes = Arrays.asList("a","b","c","d","e");

                for (int x = 0; x < childCount; x++) {
                    final String currentCode = dataNodes.item(x).getAttributes().getNamedItem(RECORD_CODE).getTextContent();
                    if (nodeCodes.stream().anyMatch(c->c.equals(currentCode))) {
                        appendMapValue(recordValues, RECORD_AUTHOR, dataNodes.item(x).getTextContent());
                    }
                }
            break;
            case "110":
                childCount = dataNodes.getLength();
                nodeCodes = Arrays.asList("a","b","c","d","e");

                for (int x=0;x<childCount;x++) {
                    final String currentCode = dataNodes.item(x).getAttributes().getNamedItem(RECORD_CODE).getTextContent();
                    if (nodeCodes.stream().anyMatch(c->c.equals(currentCode))) {
                        appendMapValue(recordBackupValues, RECORD_AUTHOR, dataNodes.item(x).getTextContent());
                    }
                }
            break;
            case "111":
                if (!recordBackupValues.containsKey(RECORD_AUTHOR)) {
                    childCount = dataNodes.getLength();
                    nodeCodes = Arrays.asList("a","c","d","e");

                    for (int x=0;x<childCount;x++) {
                        final String currentCode = dataNodes.item(x).getAttributes().getNamedItem(RECORD_CODE).getTextContent();
                        if (nodeCodes.stream().anyMatch(c->c.equals(currentCode))) {
                            appendMapValue(recordBackupValues, RECORD_AUTHOR, dataNodes.item(x).getTextContent());
                        }
                    }
                }
            break;
            case "130":
                if (!recordBackupValues.containsKey(RECORD_AUTHOR)) {
                    childCount = dataNodes.getLength();
                    nodeCodes = Arrays.asList("a","d","f");

                    for (int x=0;x<childCount;x++) {
                        final String currentCode = dataNodes.item(x).getAttributes().getNamedItem(RECORD_CODE).getTextContent();
                        if (nodeCodes.stream().anyMatch(c->c.equals(currentCode))) {
                            appendMapValue(recordBackupValues, RECORD_AUTHOR, dataNodes.item(x).getTextContent());
                        }
                    }
                }
            break;
            case "264":
                childCount = dataNodes.getLength();
                for (int x=0;x<childCount;x++) {
                    switch (dataNodes.item(x).getAttributes().getNamedItem(RECORD_CODE).getTextContent()) {
                        case "a":
                            appendMapValue(recordValues, RECORD_PLACE, dataNodes.item(x).getTextContent());
                        break;
                        case "b":
                            appendMapValue(recordValues, RECORD_PUBLISHER, dataNodes.item(x).getTextContent());
                        break;
                        //IIRC, years will have multiple fields, but we're only interested in the
                        // first value. The other fields are values like '.'
                        case "c":
                            if (!recordValues.containsKey(RECORD_YEAR) || (recordValues.get(RECORD_YEAR) == null
                                || recordValues.get(RECORD_YEAR).length() == 0)) {

                                addMapValue(recordValues,RECORD_YEAR, dataNodes.item(x).getTextContent());
                            }
                        break;
                    }
                }
            break;
            //The values stored here will only be used in the case where there is no equivalent
            //value for the fields checked in the '264' tag above
            case "260":
                childCount = dataNodes.getLength();
                for (int x = 0; x < childCount; x++) {
                    switch (dataNodes.item(x).getAttributes().getNamedItem(RECORD_CODE).getTextContent()) {
                        case "a":
                            appendMapValue(recordBackupValues, RECORD_PLACE, dataNodes.item(x).getTextContent());
                        break;
                        case "b":
                            appendMapValue(recordBackupValues, RECORD_PUBLISHER, dataNodes.item(x).getTextContent());
                        break;
                        case "c":
                            addMapValue(recordBackupValues,RECORD_YEAR, recordBackupValues.get(RECORD_YEAR)
                                + dataNodes.item(x).getTextContent());
                        break;
                    }
                }
            break;
            case "250":
                addMapValue(recordValues, RECORD_EDITION, node.getChildNodes().item(0).getTextContent());
            break;
            case "035":
                if (dataNodes.item(0).getAttributes().getNamedItem(RECORD_CODE).getTextContent().equals("a")) {
                    addMapValue(recordValues, RECORD_OCLC, node.getChildNodes().item(0).getTextContent());
                }
            break;
        }
    }

    /**
     * Apply backup records but only if a given record does not exist in the record values.
     *
     * @param recordValues
     * @param recordBackupValues
     */
    public static void applyBackupRecordValues(Map<String, String> recordValues, Map<String, String> recordBackupValues) {
        Iterator<String> bpIterator = recordBackupValues.keySet().iterator();
        while (bpIterator.hasNext()) {
            String key = bpIterator.next();
            if (!recordValues.containsKey(key) || (recordValues.get(key) == null || recordValues.get(key).length() == 0)) {
                addMapValue(recordValues, key, recordBackupValues.get(key));
            }
        }
    }

    /**
     * Build the core holding, when a prefix is not needed.
     *
     * @param holdingNode
     * @return
     *   A map of the build holding values.
     */
    public static Map<String, String> buildCoreHolding(Node holdingNode) {
        return buildCoreHolding("", holdingNode);
    }

    /**
     * Build the core holding, when a prefix may be needed.
     *
     * @param prefix
     * @param holdingNode
     * @return
     *   A map of the build holding values.
     */
    public static Map<String, String> buildCoreHolding(String prefix, Node holdingNode) {
        Map<String, String> holdingValues = new HashMap<String,String>();

        NodeList childNodes = holdingNode.getChildNodes();

        String mfhd = "";
        if (childNodes.getLength() > 0 && childNodes.item(0).getChildNodes().getLength() > 1) {
          mfhd = childNodes.item(0).getChildNodes().item(1).getTextContent();
        }

        addMapValue(holdingValues, RECORD_MFHD, mfhd);

        //the fallbackLocationCode is a holding level value that will be needed if an item has no location
        String fallbackLocationCode = "";
        String callNumber = "";

        NodeList marcRecordNodes = childNodes.item(0).getChildNodes();
        int marcRecordCount = marcRecordNodes.getLength();

        //Drill into the xml to find any holding level values we need
        //Currently, that's fallbackLocationCode and callNumber
        for (int j = 0; j < marcRecordCount; j++) {
            Node marcRecordNode = marcRecordNodes.item(j);
            NodeList subfieldNodes = marcRecordNode.getChildNodes();
            int subfieldCount = subfieldNodes.getLength();
            if (marcRecordNode.getNodeName().equalsIgnoreCase(prefix + NODE_DATA_FIELD)
                && isNodeHoldingsLocation(marcRecordNode)) {

                for (int k = 0; k < subfieldCount; k++) {
                    Node subfieldNode = subfieldNodes.item(k);
                    if (subfieldNode.getAttributes().getNamedItem(RECORD_CODE) != null) {
                        if (subfieldNode.getAttributes().getNamedItem(RECORD_CODE).getTextContent().equals("b")) {
                            fallbackLocationCode = subfieldNode.getTextContent();
                        //callNumber needs to be built up from multiple subfields
                        } else if (subfieldNode.getAttributes().getNamedItem(RECORD_CODE).getTextContent().equals("h")
                            || subfieldNode.getAttributes().getNamedItem(RECORD_CODE).getTextContent().equals("i") ) {
                            callNumber += subfieldNode.getTextContent();
                        }
                    }
                }
            }
        }

        int childCount = childNodes.getLength();
        logger.debug("The Count of Children: " + childCount);

        //The validLargeVolume flag triggers special behavior for retrieving items in this class
        //And also signals clients to handle the data
        Boolean validLargeVolume = false;
        if (childCount-1 > LARGE_VOLUME_ITEM_LIMIT) {
            for (String location : LARGE_VOLUME_LOCATIONS) {
                if (fallbackLocationCode.equals(location)) {
                    validLargeVolume = true;
                    break;
                }
            }
        }

        addMapValue(holdingValues, RECORD_FALLBACK_LOCATION_CODE, fallbackLocationCode);
        addMapValue(holdingValues, RECORD_CALL_NUMBER, callNumber);
        addMapValue(holdingValues, RECORD_VALID_LARGE_VOLUME, validLargeVolume.toString());
        return holdingValues;
    }

    /**
     * Build the core item.
     *
     * @param node
     * @return
     *   A map of the built item values.
     */
    public static Map<String,String> buildCoreItem(Node node) {

        //We currently copy all the data for a given item to a map
        //If a value is stored in a 'code' attribute, we append 'Code' to the field name for a map key
        //For values stored by attribute 'name', we just use that name for the map key
        NodeList itemDataNode = node.getChildNodes();

        int itemDataCount = itemDataNode.getLength();
        Map<String, String> itemData = new HashMap<String, String>();

        for (int l = 0; l < itemDataCount; l++) {
            if (itemDataNode.item(l).getAttributes().getNamedItem(RECORD_CODE) != null) {
                itemData.put(itemDataNode.item(l).getAttributes().getNamedItem(RECORD_NAME).getTextContent()
                    + RECORD_CODE, itemDataNode.item(l).getAttributes().getNamedItem(RECORD_CODE).getTextContent());

                logger.debug(itemDataNode.item(l).getAttributes().getNamedItem(RECORD_CODE).getTextContent()
                    + RECORD_CODE, itemDataNode.item(l).getAttributes().getNamedItem(RECORD_CODE).getTextContent());
            }

            itemData.put(itemDataNode.item(l).getAttributes().getNamedItem(RECORD_NAME).getTextContent(),
                itemDataNode.item(l).getTextContent());

            logger.debug(itemDataNode.item(l).getAttributes().getNamedItem(RECORD_NAME).getTextContent(),
                itemDataNode.item(l).getAttributes().getNamedItem(RECORD_NAME).getTextContent());
        }

        return itemData;
    }

    /**
     * Insert the value into the given map at the specified key.
     *
     * @param map
     * @param key
     * @param newValue
     */
    public static void addMapValue(Map<String,String> map, String key, String newValue) {
        map.put(key, (newValue != null ? newValue:""));
    }

    /**
     * Given a map, append the value at the given key if it already exists in the map, otherwise insert the value into the map.
     *
     * @param map
     * @param key
     * @param newValue
     */
    public static void appendMapValue(Map<String,String> map, String key, String newValue) {
        if (map.containsKey(key) && map.get(key) != null && !map.get(key).isEmpty()) {
            addMapValue(map, key, map.get(key) + newValue);
        } else {
            addMapValue(map, key, newValue);
        }
    }

    /**
     * Search through all the children on the given parent node looking for an element with the requested name.
     *
     * @param parent
     * @param name
     * @return
     *   The first element found by the given name, returned as a node.
     *   NULL is returned if not found. 
     */
    public static Node getFirstNamedChildNode(Node parent, String name) {
        if (parent.hasChildNodes()) {
            NodeList childNodes = parent.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase(name)) {
                    return childNodes.item(i);
                }
            }
        }

        return null;
    }

    /**
     * Check if the node represents a holdings location.
     */
    public static boolean isNodeHoldingsLocation(Node node) {
        if (node.getAttributes().getNamedItem(TAG) == null) {
            return false;
        }

        String tag = node.getAttributes().getNamedItem(TAG).getTextContent();
        return tag.equals("852") || tag.equals("952");
    }

    /**
     * Check if the node represents holdings data.
     */
    public static boolean isHoldingsData(Node node) {
        if (node.getAttributes().getNamedItem(TAG) == null) {
          return false;
        }

        String tag = node.getAttributes().getNamedItem(TAG).getTextContent();

        int code = 0;

        try {
            code = Integer.valueOf(tag);
        }
        catch (NumberFormatException e) {
            return false;
        }

        if (code > 851 && code < 856) {
          return true;
        }

        if (code > 851 && code < 856) {
          return true;
        }

        if (code > 862 && code < 869) {
          return true;
        }

        if (code > 875 && code < 879) {
          return true;
        }

        switch (code) {
            case 880:
            case 883:
            case 884:
            case 952:
                return true;
        }

        return false;
    }
}
