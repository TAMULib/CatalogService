package edu.tamu.catalog.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ExtendWith(SpringExtension.class)
public class Marc21XmlTest {

    @Value("classpath:mock/response/holdings/basic.xml")
    private Resource basicHolding;

    @Value("classpath:mock/response/holdings/voyager.xml")
    private Resource voyagerHolding;

    private Map<String, String> recordValues;
    private Map<String, String> recordBackupValues;

    private Document basicDocument;
    private Document voyagerDocument;

    private DocumentBuilderFactory dbFactory;

    private DocumentBuilder dBuilder;

    @BeforeEach
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

    @ParameterizedTest
    @MethodSource("argumentsAddControlFieldRecord")
    public void testAddControlFieldRecord(String field, int size, String msg) {
        Marc21Xml.addControlFieldRecord(firstNamedNodeBasic(field), recordValues);
        assertEquals(size, recordValues.size(), msg + " for field " + field);
    }

    @ParameterizedTest
    @MethodSource("argumentsAddDataFieldRecord")
    public void testAddDataFieldRecord(String field, int size, int sizeBackup, String msg, String msgBackup) {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic(field), recordValues, recordBackupValues);
        assertEquals(size, recordValues.size(), msg + " for field " + field);
        assertEquals(sizeBackup, recordBackupValues.size(), msgBackup);
    }

    @ParameterizedTest
    @MethodSource("argumentsAddDataFieldRecord245")
    public void testAddDataFieldRecordAddsRecord245(String field, boolean exists, String msg) {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic(field), recordValues, recordBackupValues);
        assertEquals(1, recordValues.size(), "Did not populate recordValues");
        assertEquals(0, recordBackupValues.size(), "Did populate recordBackupValues");
        assertEquals(exists, recordValues.get(Marc21Xml.RECORD_TITLE).contains("record 245 two"), msg);
    }

    @ParameterizedTest
    @MethodSource("argumentsAddDataFieldRecordBackupExists")
    public void testAddDataFieldRecordAddsRecordBackupExists(String field) {
        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic(field), recordValues, recordBackupValues);
        assertEquals(0, recordValues.size(), "Did populate recordValues for field " + field);
        assertEquals(1, recordBackupValues.size(), "Did not populate recordBackupValues for field " + field);

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic(field), recordValues, recordBackupValues);
        assertEquals(0, recordValues.size(), "Did populate recordValues for field " + field);
        assertEquals(1, recordBackupValues.size(), "Did populate recordBackupValues when author already exists for field " + field);
    }

    @ParameterizedTest
    @MethodSource("argumentsAddDataFieldRecord264")
    public void testAddDataFieldRecordAddsRecord264(String year) {
        recordValues.put(Marc21Xml.RECORD_YEAR, year);

        Marc21Xml.addDataFieldRecord(firstNamedNodeBasic("hasRecord264"), recordValues, recordBackupValues);
        assertEquals(3, recordValues.size(), "Did not populate recordValues for year " + year);
        assertEquals(0, recordBackupValues.size(), "Did populate recordBackupValues for year " + year);
    }

    @ParameterizedTest
    @MethodSource("argumentsAddDataFieldRecordValuesAddsRecord")
    public void testApplyBackupRecordValuesAddsRecord(boolean addRecord, String key, String value) {
        recordBackupValues.put(key, value);

        if (addRecord) {
            recordValues.put(key, value);
        }

        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);
        assertEquals(1, recordValues.size(), "Did populate recordValues");
        assertEquals(1, recordBackupValues.size(), "Did not change recordBackupValues");
    }

    @Test
    public void testApplyBackupRecordValuesDoesNothing() {
        recordValues.put("foo", "bar");
        recordBackupValues.put("foo", "bar");

        Marc21Xml.applyBackupRecordValues(recordValues, recordBackupValues);
        assertEquals(1, recordValues.size(), "Did not change recordValues");
        assertEquals(1, recordBackupValues.size(), "Did not change recordBackupValues");
    }

    @Test
    public void testbuildCoreHoldingWhenEmpty() {
        Map<String, String> coreHolding = Marc21Xml.buildCoreHolding(firstNamedNodeBasic("hasNoChildNodes"));
        assertEquals(4, coreHolding.size(), "Populated coreHolding");

        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_MFHD), "Does not have key " + Marc21Xml.RECORD_MFHD);
        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_FALLBACK_LOCATION_CODE), "Does not have key " + Marc21Xml.RECORD_FALLBACK_LOCATION_CODE);
        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_CALL_NUMBER), "Does not have key " + Marc21Xml.RECORD_CALL_NUMBER);
        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_VALID_LARGE_VOLUME), "Does not have key " + Marc21Xml.RECORD_VALID_LARGE_VOLUME);

        assertTrue(StringUtils.isEmpty(coreHolding.get(Marc21Xml.RECORD_MFHD)), "Key " + Marc21Xml.RECORD_MFHD + " should be empty");
    }

    @Test
    public void testbuildCoreHolding() {
        Map<String, String> coreHolding = Marc21Xml.buildCoreHolding(getNamedNodeVoyager("holdings", 0));
        assertEquals(4, coreHolding.size(), "Populated coreHolding");

        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_MFHD), "Does not have key " + Marc21Xml.RECORD_MFHD);
        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_FALLBACK_LOCATION_CODE), "Does not have key " + Marc21Xml.RECORD_FALLBACK_LOCATION_CODE);
        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_CALL_NUMBER), "Does not have key " + Marc21Xml.RECORD_CALL_NUMBER);
        assertTrue(coreHolding.containsKey(Marc21Xml.RECORD_VALID_LARGE_VOLUME), "Does not have key " + Marc21Xml.RECORD_VALID_LARGE_VOLUME);

        assertTrue(StringUtils.isEmpty(coreHolding.get(Marc21Xml.RECORD_MFHD)), "Key " + Marc21Xml.RECORD_MFHD + " should be empty");

        // TODO: this test needs to be more complete and correct.
    }

    // TODO: this needs a Marc21Xml.buildCoreItem() to do a hasAttributes() and similar check.
    /*@Test
    public void testbuildCoreItem() {
        Map<String, String> coreItem = Marc21Xml.buildCoreItem(getNamedNodeVoyager("item", 0));
        assertEquals(20, coreItem.size(), "Populated coreItem");

        // TODO: this test needs to be more complete and correct.
    }*/

    @Test
    public void testGetFirstNamedChildNode() {
        Node parentNode = firstNamedNodeBasic("hasNamedChildren");
        Node childNode = Marc21Xml.getFirstNamedChildNode(parentNode, "first");
        assertTrue(childNode.getTextContent().equals("first"), "Did not get the correct first child node.");
    }

    @Test
    public void testGetFirstNamedChildNodeWithMissingName() {
        Node parentNode = firstNamedNodeBasic("hasNamedChildren");
        Node childNode = Marc21Xml.getFirstNamedChildNode(parentNode, "doesntexist");
        assertNull(childNode, "A child node should not have been found.");
    }

    @Test
    public void testGetFirstNamedChildNodeWithoutChildNodes() {
        Node parentNode = firstNamedNodeBasic("hasNoChildNodes");
        Node childNode = Marc21Xml.getFirstNamedChildNode(parentNode, "doesntexist");
        assertNull(childNode, "A child node should not have been found.");
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

    private static Stream<? extends Arguments> argumentsAddControlFieldRecord() {
        return Stream.of(
          Arguments.of("hasTag001", 1, "Did not populate recordValues."),
          Arguments.of("hasTag", 0, "Did populate recordValues."),
          Arguments.of("hasNoChildNodes", 0, "Did populate recordValues.")
        );
    }

    private static Stream<? extends Arguments> argumentsAddDataFieldRecord() {
        final String record = "Did populate recordValues.";
        final String notRecord = "Did not populate recordValues.";
        final String backup = "Did populate recordBackupValues.";
        final String notBackup = "Did not populate recordBackupValues.";

        return Stream.of(
          Arguments.of("hasRecord022a", 2, 0, notRecord, backup),
          Arguments.of("hasRecord022b", 1, 0, notRecord, backup),
          Arguments.of("hasRecord020a", 2, 0, notRecord, backup),
          Arguments.of("hasRecord020b", 1, 0, notRecord, backup),
          Arguments.of("hasRecord100", 1, 0, notRecord, backup),
          Arguments.of("hasRecord110", 0, 1, record, notBackup),
          Arguments.of("hasRecord264", 3, 0, notRecord, backup),
          Arguments.of("hasRecord260", 0, 3, record, notBackup),
          Arguments.of("hasRecord250", 1, 0, notRecord, backup),
          Arguments.of("hasRecord035a", 1, 0, notRecord, backup),
          Arguments.of("hasRecord035b", 0, 0, record, backup)
        );
    }

    private static Stream<? extends Arguments> argumentsAddDataFieldRecord245() {
        return Stream.of(
          Arguments.of("hasRecord245one", false, "Should not contain second child."),
          Arguments.of("hasRecord245two", true, "Should contain second child.")
        );
    }

    private static Stream<? extends Arguments> argumentsAddDataFieldRecordBackupExists() {
        return Stream.of(
          Arguments.of("hasRecord111"),
          Arguments.of("hasRecord130")
        );
    }

    private static Stream<? extends Arguments> argumentsAddDataFieldRecord264() {
        return Stream.of(
          Arguments.of("2020"),
          Arguments.of((String) null),
          Arguments.of(""),
          Arguments.of("stub")
        );
    }

    private static Stream<? extends Arguments> argumentsAddDataFieldRecordValuesAddsRecord() {
        return Stream.of(
          Arguments.of(false, (String) null, (String) null),
          Arguments.of(true, "foo", (String) null),
          Arguments.of(true, "foo", "")
        );
    }

}
