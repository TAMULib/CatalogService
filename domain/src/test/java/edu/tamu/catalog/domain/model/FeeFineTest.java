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
public class FeeFineTest {

    @Test
    public void testCreateAllArgs() {
        final Date now = new Date();
        final FeeFine feeFine = new FeeFine("fineId", "itemId", "instanceId", 1.0, "fineType", now, "itemTitle");

        assertNotNull(feeFine);
        assertNotNull(feeFine.getFineDate());

        assertEquals("fineId", feeFine.getFineId());
        assertEquals("itemId", feeFine.getItemId());
        assertEquals("instanceId", feeFine.getInstanceId());
        assertEquals(1.0, feeFine.getAmount(), 0);
        assertEquals("fineType", feeFine.getFineType());
        assertEquals(now.toString(), feeFine.getFineDate().toString());
        assertEquals("itemTitle", feeFine.getItemTitle());
    }

    @Test
    public void testCreateNoArgs() {
        final FeeFine feeFine = new FeeFine();

        assertNotNull(feeFine);

        assertNull(feeFine.getFineId());
        assertNull(feeFine.getItemId());
        assertNull(feeFine.getInstanceId());
        assertNull(feeFine.getAmount());
        assertNull(feeFine.getFineDate());
        assertNull(feeFine.getItemTitle());
    }

    @Test
    public void testBuilder() {
        final Date now = new Date();
        final FeeFine feeFine = new FeeFine.FeeFineBuilder()
            .amount(1.0)
            .fineId("fineId")
            .itemId("itemId")
            .instanceId("instanceId")
            .fineType("fineType")
            .fineDate(now)
            .itemTitle("itemTitle")
            .build();

        assertNotNull(feeFine);
        assertNotNull(feeFine.getFineDate());

        assertEquals(1.0, feeFine.getAmount(), 0);
        assertEquals("fineId", feeFine.getFineId());
        assertEquals("itemId", feeFine.getItemId());
        assertEquals("instanceId", feeFine.getInstanceId());
        assertEquals("fineType", feeFine.getFineType());
        assertEquals(now.toString(), feeFine.getFineDate().toString());
        assertEquals("itemTitle", feeFine.getItemTitle());
    }

    @Test
    public void testUpdate() {
        final Date now = new Date();
        final Date later = Date.from(now.toInstant().plusSeconds(100));
        final FeeFine feeFine = new FeeFine("fineId", "itemId", "instanceId", 1.0, "fineType", now, "itemTitle");

        feeFine.setFineId("updatedId");
        feeFine.setItemId("updatedItemId");
        feeFine.setInstanceId("updatedInstanceId");
        feeFine.setAmount(2.0);
        feeFine.setFineType("updatedType");
        feeFine.setFineDate(later);
        feeFine.setItemTitle("updatedTitle");

        assertNotNull(feeFine);
        assertNotNull(feeFine.getFineDate());

        assertEquals("updatedId", feeFine.getFineId());
        assertEquals("updatedItemId", feeFine.getItemId());
        assertEquals("updatedInstanceId", feeFine.getInstanceId());
        assertEquals(2.0, feeFine.getAmount(), 0);
        assertEquals("updatedType", feeFine.getFineType());
        assertEquals(later.toString(), feeFine.getFineDate().toString());
        assertEquals("updatedTitle", feeFine.getItemTitle());
    }

    @Test
    public void testEquals() {
        final Date now = new Date();
        final FeeFine feeFine1 = new FeeFine("fineId", "itemId", "instanceId", 1.0, "fineType", now, "itemTitle");
        final FeeFine feeFine2 = new FeeFine("fineId", "itemId", "instanceId", 1.0, "fineType", now, "itemTitle");
        final FeeFine feeFine3 = new FeeFine();

        assertTrue(feeFine1.equals(feeFine2));
        assertFalse(feeFine1.equals(feeFine3));
    }

}
