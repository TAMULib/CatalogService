package edu.tamu.catalog.exception;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ExtendWith(MockitoExtension.class)
public class HoldingsRequestErrorTest {

    private static final String CATALOG = "Some Catalog";
    private static final String FIRST_CODE = "first code";
    private static final String FIRST_DATA = "first data";
    private static final String SECOND_CODE = "second code";
    private static final String SECOND_DATA = "second data";

    @Mock
    NodeList mockList;

    @Mock
    Node firstCode;

    @Mock
    Node firstNode;

    @Mock
    Node secondCode;

    @Mock
    Node secondNode;

    @Mock
    NamedNodeMap firstMap;

    @Mock
    NamedNodeMap secondMap;

    @Test
    void holdingsRequestErrorWorksTest() {
        when(mockList.getLength()).thenReturn(2);

        when(mockList.item(0)).thenReturn(firstNode);
        when(firstNode.getAttributes()).thenReturn(firstMap);
        when(firstMap.getNamedItem(anyString())).thenReturn(firstCode);
        when(firstCode.getTextContent()).thenReturn(FIRST_CODE);
        when(firstNode.getTextContent()).thenReturn(FIRST_DATA);

        when(mockList.item(1)).thenReturn(secondNode);
        when(secondNode.getAttributes()).thenReturn(secondMap);
        when(secondMap.getNamedItem(anyString())).thenReturn(secondCode);
        when(secondCode.getTextContent()).thenReturn(SECOND_CODE);
        when(secondNode.getTextContent()).thenReturn(SECOND_DATA);

        HoldingsRequestError exception = Assertions.assertThrows(HoldingsRequestError.class, () -> {
            throw new HoldingsRequestError(mockList, CATALOG);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(CATALOG));
        assertTrue(exception.getMessage().contains(FIRST_CODE));
        assertTrue(exception.getMessage().contains(FIRST_DATA));
        assertTrue(exception.getMessage().contains(SECOND_CODE));
        assertTrue(exception.getMessage().contains(SECOND_DATA));
    }

}
