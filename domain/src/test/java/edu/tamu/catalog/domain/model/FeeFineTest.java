package edu.tamu.catalog.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class FeeFineTest {

    @Test
    public void testCreateAllArgs() {
        final Date now = new Date();
        final FeeFine feeFine = new FeeFine(1.0, "fineId", "fineType", now, "itemTitle");

        assertNotNull(feeFine);
        assertNotNull(feeFine.getFineDate());

        assertEquals(1.0, feeFine.getAmount(), 0);
        assertEquals("fineId", feeFine.getFineId());
        assertEquals("fineType", feeFine.getFineType());
        assertEquals(now.toString(), feeFine.getFineDate().toString());
        assertEquals("itemTitle", feeFine.getItemTitle());
    }

    @Test
    public void testCreateNoArgs() {
        final FeeFine feeFine = new FeeFine();

        assertNotNull(feeFine);

        assertNull(feeFine.getAmount());
        assertNull(feeFine.getFineId());
        assertNull(feeFine.getFineDate());
        assertNull(feeFine.getItemTitle());
    }

    @Test
    public void testBuilder() {
        final Date now = new Date();
        final FeeFine feeFine = new FeeFine.FeeFineBuilder()
            .amount(1.0)
            .fineId("fineId")
            .fineType("fineType")
            .fineDate(now)
            .itemTitle("itemTitle")
            .build();

        assertNotNull(feeFine);
        assertNotNull(feeFine.getFineDate());

        assertEquals(1.0, feeFine.getAmount(), 0);
        assertEquals("fineId", feeFine.getFineId());
        assertEquals("fineType", feeFine.getFineType());
        assertEquals(now.toString(), feeFine.getFineDate().toString());
        assertEquals("itemTitle", feeFine.getItemTitle());
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

        assertNotNull(feeFine);
        assertNotNull(feeFine.getFineDate());

        assertEquals(2.0, feeFine.getAmount(), 0);
        assertEquals("updatedId", feeFine.getFineId());
        assertEquals("updatedType", feeFine.getFineType());
        assertEquals(later.toString(), feeFine.getFineDate().toString());
        assertEquals("updatedTitle", feeFine.getItemTitle());
    }

    @Test
    public void testEquals() {
        final Date now = new Date();
        final FeeFine feeFine1 = new FeeFine(1.0, "fineId", "fineType", now, "itemTitle");
        final FeeFine feeFine2 = new FeeFine(1.0, "fineId", "fineType", now, "itemTitle");
        final FeeFine feeFine3 = new FeeFine();

        assertTrue(feeFine1.equals(feeFine2));
        assertFalse(feeFine1.equals(feeFine3));
    }

}
