package edu.tamu.catalog.domain.model;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class FeeFineTest {

    @Test
    public void testCreateAllArgs() {
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
    public void testCreateNoArgs() {
        final FeeFine feeFine = new FeeFine();

        Assert.assertNotNull(feeFine);
        Assert.assertNull(feeFine.getAmount());
        Assert.assertNull(feeFine.getFineId());
        Assert.assertNull(feeFine.getFineDate());
        Assert.assertNull(feeFine.getItemTitle());
    }

    @Test
    public void testBuilder() {
        final Date now = new Date();
        FeeFine.FeeFineBuilder feeFineBuilder = FeeFine.builder();

        feeFineBuilder.amount(1.0);
        feeFineBuilder.fineId("fineId");
        feeFineBuilder.fineType("fineType");
        feeFineBuilder.fineDate(now);
        feeFineBuilder.itemTitle("itemTitle");

        final FeeFine feeFine = feeFineBuilder.build();

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

    @Test
    public void testEquals() {
        final Date now = new Date();
        final FeeFine feeFine1 = new FeeFine(1.0, "fineId", "fineType", now, "itemTitle");
        final FeeFine feeFine2 = new FeeFine(1.0, "fineId", "fineType", now, "itemTitle");
        final FeeFine feeFine3 = new FeeFine(2.0, "fineId", "fineType", now, "itemTitle");

        Assert.assertTrue(feeFine1.equals(feeFine2));
        Assert.assertFalse(feeFine1.equals(feeFine3));
    }

}
