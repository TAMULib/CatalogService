package edu.tamu.catalog.domain.model;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class FeeFineTest {

    @Test
    public void testCreate() {
        final Date now = new Date();
        final FeeFine feeFine = new FeeFine(1.0, "fineId", "fineType", now, "itemTitle");

        Assert.assertNotNull(feeFine);
        Assert.assertNotNull(feeFine.getFineDate());

        Assert.assertEquals(1.0, feeFine.getAmount(), 0);
        Assert.assertEquals("fineId", feeFine.getFineId());
        Assert.assertEquals("fineType", feeFine.getFineType());
        Assert.assertEquals(now.toString(), feeFine.getFineDate().toString());
        Assert.assertEquals("itemTitle", feeFine.getItemTitle());
    }

    @Test
    public void testUpdate() {
        final Date now = new Date();
        final Date later = Date.from(now.toInstant().plusSeconds(100));
        final FeeFine feeFine = new FeeFine(1.0, "fineId", "fineType", now, "itemTitle");

        feeFine.setAmount(2.0);
        feeFine.setFineId("updatedId");
        feeFine.setFineType("updatedType");
        feeFine.setFineDate(later);
        feeFine.setItemTitle("updatedTitle");

        Assert.assertNotNull(feeFine);
        Assert.assertNotNull(feeFine.getFineDate());

        Assert.assertEquals(2.0, feeFine.getAmount(), 0);
        Assert.assertEquals("updatedId", feeFine.getFineId());
        Assert.assertEquals("updatedType", feeFine.getFineType());
        Assert.assertEquals(later.toString(), feeFine.getFineDate().toString());
        Assert.assertEquals("updatedTitle", feeFine.getItemTitle());
    }

}