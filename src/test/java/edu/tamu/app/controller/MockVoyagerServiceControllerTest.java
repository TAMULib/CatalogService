package edu.tamu.app.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.tamu.app.mock.controller.MockVoyagerServiceController;

@RunWith(SpringRunner.class)
public class MockVoyagerServiceControllerTest {
    private static final String JACK_DANIELS = "Jack Daniels";
    private static final String JACK_DANIELS_UIN = "123456789";
    
    private static final String BOB_BORING = "Bob Boring";
    private static final String BOB_BORING_UIN = "987654321";

    private static final String ITEM_NOT_FOUND = "item|1";
    private static final String ITEM_FOUND = "item|4159197";
    private static final String ITEM_EXCEPTION = "item|35392";

    private static final String REPLY_ERROR = "<reply-text>error</reply-text>";

    @InjectMocks
    private MockVoyagerServiceController mockVoyagerServiceController;

    private MockHttpServletRequest request;

    @Before
    public void setup() throws IOException {
        request = new MockHttpServletRequest();
    }

    @Test
    public void testAuthenticateReturnsMockedStrings() throws IOException {
        request.setContent(mockXmlString(JACK_DANIELS_UIN).getBytes());

        String response = mockVoyagerServiceController.authenticate(request);
        assertTrue("Expected response to contain string " + JACK_DANIELS, response.contains(JACK_DANIELS));
        
        request.setContent(mockXmlString(BOB_BORING_UIN).getBytes());
        response = mockVoyagerServiceController.authenticate(request);
        assertTrue("Expected response to contain string " + BOB_BORING, response.contains(BOB_BORING));
    }

    @Test
    public void testFinesReturnsMockedString() throws IOException {
        String response = mockVoyagerServiceController.fines(request);
        assertFalse("Expected response to contain a populated string", StringUtils.isEmpty(response));
    }

    @Test
    public void testRequestsReturnsMockedString() throws IOException {
        String response = mockVoyagerServiceController.requests(request);
        assertFalse("Expected response to contain a populated string", StringUtils.isEmpty(response));
    }

    @Test
    public void testCancelReturnsMockedString() throws IOException {
        String response = mockVoyagerServiceController.cancel(request);
        assertFalse("Expected response to contain a populated string", StringUtils.isEmpty(response));
    }

    @Test
    public void testLoansReturnsMockedString() throws JAXBException {
        String response = mockVoyagerServiceController.loans(request);
        assertFalse("Expected response to contain a populated string", StringUtils.isEmpty(response));
    }

    @Test
    public void testRenewReturnsErrorString() throws Exception {
        mockVoyagerServiceController.loans(request);

        String response = mockVoyagerServiceController.renew(ITEM_NOT_FOUND, request);
        assertTrue("Expected response to contain string " + REPLY_ERROR, response.contains(REPLY_ERROR));
    }

    @Test
    public void testRenewReturnsMockedString() throws Exception {
        mockVoyagerServiceController.loans(request);

        String response = mockVoyagerServiceController.renew(ITEM_FOUND, request);
        assertFalse("Expected response to contain a populated string", StringUtils.isEmpty(response));
    }

    @Test(expected = Exception.class)
    public void testRenewThrowsException() throws Exception {
        mockVoyagerServiceController.renew(ITEM_EXCEPTION, request);
    }

    @Test
    public void testRenwAllReturnsMockedString() throws JAXBException {
        String response = mockVoyagerServiceController.renewAll(request);
        assertFalse("Expected response to contain a populated string", StringUtils.isEmpty(response));
    }

    private String mockXmlString(String uin) {
        String mockXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ser:authFactor type=\"I\">";
        mockXml += uin + "</ser:authFactor>";

        return mockXml;
    }
}
