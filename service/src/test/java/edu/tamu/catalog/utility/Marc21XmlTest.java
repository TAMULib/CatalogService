package edu.tamu.catalog.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.tamu.catalog.utility.Marc21Xml;

@RunWith(SpringRunner.class)
public class Marc21XmlTest {

    @Value("classpath:mock/holdings/basic.xml")
    private Resource basicHolding;

    @Value("classpath:mock/holdings/voyager.xml")
    private Resource voyagerHolding;

    private Map<String, String> recordValues;
    private Map<String, String> recordBackupValues;

    private Document basicDocument;
    private Document voyagerDocument;

    private DocumentBuilderFactory dbFactory;

    private DocumentBuilder dBuilder;

    @Before
    public void setup() throws ParserConfigurationException, SAXException, IOException {
        recordValues = new HashMap<>();
        recordBackupValues = new HashMap<>();

        dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();

        basicDocument = dBuilder.parse(new InputSource(new StringReader(loadBasicXml())));
        voyagerDocument = dBuilder.parse(new InputSource(new StringReader(loadVoyagerXml())));

        basicDocument.getDocumentElement().normalize();
        voyagerDocument.getDocumentElement().normalize();
    }

    @Test
    public void testAddControlFieldRecordAddsRecord() {
        Marc21Xml.addControlFieldRecord(firstNamedNodeBasic("hasTag001"), recordValues);
        assertEquals("Did not populate recordValues", 1, recordValues.size());
    }

    @Test
    public void testAddControlFieldRecordNoRecordAdded() {
        Marc21Xml.addControlFieldRecord(firstNamedNodeBasic("hasTag"), recordValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());

        Marc21Xml.addControlFieldRecord(firstNamedNodeBasic("hasNoChildNodes"), recordValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord022() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord022a"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 2, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());

        recordValues.clear();
        recordBackupValues.clear();

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord022b"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 1, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord020() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord020a"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 2, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());

        recordValues.clear();
        recordBackupValues.clear();

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord020b"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 1, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord245() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord245one"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 1, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());
        assertFalse("Should not contain second child", recordValues.get(Marc21Xml.RECORD_TITLE).contains("record 245 two"));

        recordValues.clear();
        recordBackupValues.clear();

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord245two"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 1, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());
        assertTrue("Should contain second child", recordValues.get(Marc21Xml.RECORD_TITLE).contains("record 245 two"));
    }

    @Test
    public void testAddDataFieldRecordAddsRecord100() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord100"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 1, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord110() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord110"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());
        assertEquals("Did not populate recordBackupValues", 1, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord111() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord111"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());
        assertEquals("Did not populate recordBackupValues", 1, recordBackupValues.size());

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord111"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());
        assertEquals("Did populate recordBackupValues when author already exists", 1, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord130() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord130"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());
        assertEquals("Did not populate recordBackupValues", 1, recordBackupValues.size());

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord130"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());
        assertEquals("Did populate recordBackupValues when author already exists", 1, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord264() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord264"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 3, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());

        recordValues.clear();
        recordValues.put(Marc21Xml.RECORD_YEAR, null);

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord264"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 3, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());

        recordValues.clear();
        recordValues.put(Marc21Xml.RECORD_YEAR, "");

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord264"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 3, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());

        recordValues.clear();
        recordValues.put(Marc21Xml.RECORD_YEAR, "stub");

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord264"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 3, recordValues.size());
        assertEquals("Did populate recordBackupValues", 0, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord260() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord260"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 0, recordValues.size());
        assertEquals("Did not populate recordBackupValues", 3, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord250() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord250"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 1, recordValues.size());
        assertEquals("Did not populate recordBackupValues", 0, recordBackupValues.size());
    }

    @Test
    public void testAddDataFieldRecordAddsRecord035() {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord035a"), recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 1, recordValues.size());
        assertEquals("Did not populate recordBackupValues", 0, recordBackupValues.size());

        recordValues.clear();

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord035b"), recordValues, recordBackupValues);
        assertEquals("Did not populate recordValues", 0, recordValues.size());
        assertEquals("Did not populate recordBackupValues", 0, recordBackupValues.size());
    }

    @Test
    public void testApplyBackupRecordValuesAddsRecord() {
        recordBackupValues.put("foo", "bar");

        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 1, recordValues.size());
        assertEquals("Did not change recordBackupValues", 1, recordBackupValues.size());

        recordValues.clear();
        recordValues.put("foo", "");

        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 1, recordValues.size());
        assertEquals("Did not change recordBackupValues", 1, recordBackupValues.size());

        recordValues.clear();
        recordValues.put("foo", null);

        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);
        assertEquals("Did populate recordValues", 1, recordValues.size());
        assertEquals("Did not change recordBackupValues", 1, recordBackupValues.size());
    }

    @Test
    public void testApplyBackupRecordValuesDoesNothing() {
        recordValues.put("foo", "bar");
        recordBackupValues.put("foo", "bar");

        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);
        assertEquals("Did not change recordValues", 1, recordValues.size());
        assertEquals("Did not change recordBackupValues", 1, recordBackupValues.size());
    }

    @Test
    public void testbuildCoreHoldingWhenEmpty() {
        Map<String, String> coreHolding = Marc21Xml.buildCoreHolding(firstNamedNodeBasic("hasNoChildNodes"));
        assertEquals("Populated coreHolding", 4, coreHolding.size());

        assertTrue("Does not have key " + Marc21Xml.RECORD_MFHD, coreHolding.containsKey(Marc21Xml.RECORD_MFHD));
        assertTrue("Does not have key " + Marc21Xml.RECORD_FALLBACK_LOCATION_CODE, coreHolding.containsKey(Marc21Xml.RECORD_FALLBACK_LOCATION_CODE));
        assertTrue("Does not have key " + Marc21Xml.RECORD_CALL_NUMBER, coreHolding.containsKey(Marc21Xml.RECORD_CALL_NUMBER));
        assertTrue("Does not have key " + Marc21Xml.RECORD_VALID_LARGE_VOLUME, coreHolding.containsKey(Marc21Xml.RECORD_VALID_LARGE_VOLUME));

        assertTrue("Key " + Marc21Xml.RECORD_MFHD + " should be empty", StringUtils.isEmpty(coreHolding.get(Marc21Xml.RECORD_MFHD)));
    }

    @Test
    public void testbuildCoreHolding() {
        Map<String, String> coreHolding = Marc21Xml.buildCoreHolding(getNamedNodeVoyager("holdings", 0));
        assertEquals("Populated coreHolding", 4, coreHolding.size());

        assertTrue("Does not have key " + Marc21Xml.RECORD_MFHD, coreHolding.containsKey(Marc21Xml.RECORD_MFHD));
        assertTrue("Does not have key " + Marc21Xml.RECORD_FALLBACK_LOCATION_CODE, coreHolding.containsKey(Marc21Xml.RECORD_FALLBACK_LOCATION_CODE));
        assertTrue("Does not have key " + Marc21Xml.RECORD_CALL_NUMBER, coreHolding.containsKey(Marc21Xml.RECORD_CALL_NUMBER));
        assertTrue("Does not have key " + Marc21Xml.RECORD_VALID_LARGE_VOLUME, coreHolding.containsKey(Marc21Xml.RECORD_VALID_LARGE_VOLUME));

        assertTrue("Key " + Marc21Xml.RECORD_MFHD + " should be empty", StringUtils.isEmpty(coreHolding.get(Marc21Xml.RECORD_MFHD)));

        // TODO: this test needs to be more complete and correct.
    }

    // TODO: this needs a Marc21Xml.buildCoreItem() to do a hasAttributes() and similar check.
    /*@Test
    public void testbuildCoreItem() {
        Map<String, String> coreItem = Marc21Xml.buildCoreItem(getNamedNodeVoyager("item", 0));
        assertEquals("Populated coreItem", 20, coreItem.size());

        // TODO: this test needs to be more complete and correct.
    }*/

    @Test
    public void testGetFirstNamedChildNode() {
        Node parentNode = firstNamedNodeBasic("hasNamedChildren");
        Node childNode = Marc21Xml.getFirstNamedChildNode(parentNode, "first");
        assertTrue("Did not get the correct first child node.", childNode.getTextContent().equals("first"));
    }

    @Test
    public void testGetFirstNamedChildNodeWithMissingName() {
        Node parentNode = firstNamedNodeBasic("hasNamedChildren");
        Node childNode = Marc21Xml.getFirstNamedChildNode(parentNode, "doesntexist");
        assertNull("A child node should not have been found.", childNode);
    }

    @Test
    public void testGetFirstNamedChildNodeWithoutChildNodes() {
        Node parentNode = firstNamedNodeBasic("hasNoChildNodes");
        Node childNode = Marc21Xml.getFirstNamedChildNode(parentNode, "doesntexist");
        assertNull("A child node should not have been found.", childNode);
    }

    private Node firstNamedNodeBasic(String name) {
        final NodeList nodeList = basicDocument.getElementsByTagName(name);

        if (nodeList.getLength() == 0) {
            return null;
        }

        return nodeList.item(0);
    }

    private Node getNamedNodeVoyager(String name, int index) {
        final NodeList nodeList = voyagerDocument.getElementsByTagName(name);

        if (nodeList.getLength() == 0) {
            return null;
        }

        return nodeList.item(index);
    }

    private String loadBasicXml() throws IOException {
        InputStreamReader inputReader = new InputStreamReader(basicHolding.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputReader);

        return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private String loadVoyagerXml() throws IOException {
        InputStreamReader inputReader = new InputStreamReader(voyagerHolding.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputReader);

        return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
