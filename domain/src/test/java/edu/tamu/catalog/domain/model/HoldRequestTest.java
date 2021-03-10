package edu.tamu.catalog.domain.model;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class HoldRequestTest {

    @Test
    public void testCreate() {
        final Date now = new Date();
        final Integer one = 1;

        final HoldRequest holdRequest = new HoldRequest("requestId", "itemId", "requestType", "itemTitle", "statusText", "pickupServicePoint", one, now);

        Assert.assertNotNull(holdRequest);
        Assert.assertNotNull(holdRequest.getExpirationDate());

        Assert.assertEquals("requestId", holdRequest.getRequestId());
        Assert.assertEquals("itemId", holdRequest.getItemId());
        Assert.assertEquals("requestType", holdRequest.getRequestType());
        Assert.assertEquals("itemTitle", holdRequest.getItemTitle());
        Assert.assertEquals("statusText", holdRequest.getStatusText());
        Assert.assertEquals("pickupServicePoint", holdRequest.getPickupServicePoint());
        Assert.assertEquals(one, holdRequest.getQueuePosition());
        Assert.assertEquals(now.toString(), holdRequest.getExpirationDate().toString());
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

        Assert.assertNotNull(holdRequest);
        Assert.assertNotNull(holdRequest.getExpirationDate());

        Assert.assertEquals("updatedRequestId", holdRequest.getRequestId());
        Assert.assertEquals("updatedItemId", holdRequest.getItemId());
        Assert.assertEquals("updatedRequestType", holdRequest.getRequestType());
        Assert.assertEquals("updatedItemTitle", holdRequest.getItemTitle());
        Assert.assertEquals("updatedStatusText", holdRequest.getStatusText());
        Assert.assertEquals("updatedPickupServicePoint", holdRequest.getPickupServicePoint());
        Assert.assertEquals(two, holdRequest.getQueuePosition());
        Assert.assertEquals(later.toString(), holdRequest.getExpirationDate().toString());
    }

}