package edu.tamu.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class HoldRequestTest {

    @Test
    public void testCreateAllArgs() {
        final Date now = new Date();
        final Integer one = 1;

        final HoldRequest holdRequest = new HoldRequest("requestId", "itemId", "requestType", "itemTitle", "statusText", "pickupServicePoint", one, now);

        assertNotNull(holdRequest);
        assertNotNull(holdRequest.getExpirationDate());

        assertEquals("requestId", holdRequest.getRequestId());
        assertEquals("itemId", holdRequest.getItemId());
        assertEquals("requestType", holdRequest.getRequestType());
        assertEquals("itemTitle", holdRequest.getItemTitle());
        assertEquals("statusText", holdRequest.getStatusText());
        assertEquals("pickupServicePoint", holdRequest.getPickupServicePoint());
        assertEquals(one, holdRequest.getQueuePosition());
        assertEquals(now.toString(), holdRequest.getExpirationDate().toString());
    }

    @Test
    public void testCreateNoArgs() {
        final HoldRequest holdRequest = new HoldRequest();

        assertNotNull(holdRequest);

        assertNull(holdRequest.getRequestId());
        assertNull(holdRequest.getItemId());
        assertNull(holdRequest.getRequestType());
        assertNull(holdRequest.getItemTitle());
        assertNull(holdRequest.getStatusText());
        assertNull(holdRequest.getPickupServicePoint());
        assertNull(holdRequest.getQueuePosition());
        assertNull(holdRequest.getExpirationDate());
    }

    @Test
    public void testBuilder() {
        final Date now = new Date();
        final Integer one = 1;
        final HoldRequest holdRequest = new HoldRequest.HoldRequestBuilder()
            .requestId("requestId")
            .itemId("itemId")
            .requestType("requestType")
            .itemTitle("itemTitle")
            .statusText("statusText")
            .pickupServicePoint("pickupServicePoint")
            .queuePosition(one)
            .expirationDate(now)
            .build();

        assertNotNull(holdRequest);
        assertNotNull(holdRequest.getExpirationDate());

        assertEquals("requestId", holdRequest.getRequestId());
        assertEquals("itemId", holdRequest.getItemId());
        assertEquals("requestType", holdRequest.getRequestType());
        assertEquals("itemTitle", holdRequest.getItemTitle());
        assertEquals("statusText", holdRequest.getStatusText());
        assertEquals("pickupServicePoint", holdRequest.getPickupServicePoint());
        assertEquals(one, holdRequest.getQueuePosition());
        assertEquals(now.toString(), holdRequest.getExpirationDate().toString());
    }

    @Test
    public void testUpdate() {
        final Date now = new Date();
        final Date later = Date.from(now.toInstant().plusSeconds(100));
        final Integer one = 1;
        final Integer two = 2;

        final HoldRequest holdRequest = new HoldRequest("requestId", "itemId", "requestType", "itemTitle", "statusText", "pickupServicePoint", one, now);

        holdRequest.setRequestId("updatedRequestId");
        holdRequest.setItemId("updatedItemId");
        holdRequest.setRequestType("updatedRequestType");
        holdRequest.setItemTitle("updatedItemTitle");
        holdRequest.setStatusText("updatedStatusText");
        holdRequest.setPickupServicePoint("updatedPickupServicePoint");
        holdRequest.setQueuePosition(two);
        holdRequest.setExpirationDate(later);

        assertNotNull(holdRequest);
        assertNotNull(holdRequest.getExpirationDate());

        assertEquals("updatedRequestId", holdRequest.getRequestId());
        assertEquals("updatedItemId", holdRequest.getItemId());
        assertEquals("updatedRequestType", holdRequest.getRequestType());
        assertEquals("updatedItemTitle", holdRequest.getItemTitle());
        assertEquals("updatedStatusText", holdRequest.getStatusText());
        assertEquals("updatedPickupServicePoint", holdRequest.getPickupServicePoint());
        assertEquals(two, holdRequest.getQueuePosition());
        assertEquals(later.toString(), holdRequest.getExpirationDate().toString());
    }

    @Test
    public void testEquals() {
        final Date now = new Date();
        final Integer one = 1;

        final HoldRequest holdRequest1 = new HoldRequest("requestId", "itemId", "requestType", "itemTitle", "statusText", "pickupServicePoint", one, now);
        final HoldRequest holdRequest2 = new HoldRequest("requestId", "itemId", "requestType", "itemTitle", "statusText", "pickupServicePoint", one, now);
        final HoldRequest holdRequest3 = new HoldRequest();

        assertTrue(holdRequest1.equals(holdRequest2));
        assertFalse(holdRequest1.equals(holdRequest3));
    }

}
