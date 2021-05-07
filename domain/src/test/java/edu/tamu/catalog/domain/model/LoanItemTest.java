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
public class LoanItemTest {

    @Test
    public void testCreateAllArgs() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));

        final LoanItem loanItem = new LoanItem("loanId", "itemId", "instanceId", "instanceHrid", "itemType", now, due, false, "title", "author", "location", "locationCode", true);

        assertNotNull(loanItem);
        assertNotNull(loanItem.getLoanDate());
        assertNotNull(loanItem.getLoanDueDate());

        assertEquals("loanId", loanItem.getLoanId());
        assertEquals("itemId", loanItem.getItemId());
        assertEquals("instanceId", loanItem.getInstanceId());
        assertEquals("instanceHrid", loanItem.getInstanceHrid());
        assertEquals("itemType", loanItem.getItemType());
        assertEquals(now.toString(), loanItem.getLoanDate().toString());
        assertEquals(due.toString(), loanItem.getLoanDueDate().toString());
        assertEquals(false, loanItem.isOverdue());
        assertEquals("title", loanItem.getTitle());
        assertEquals("author", loanItem.getAuthor());
        assertEquals("location", loanItem.getLocation());
        assertEquals("locationCode", loanItem.getLocationCode());
        assertEquals(true, loanItem.getCanRenew());
    }

    @Test
    public void testCreateNoArgs() {
        final LoanItem loanItem = new LoanItem();

        assertNotNull(loanItem);

        assertNull(loanItem.getLoanId());
        assertNull(loanItem.getItemId());
        assertNull(loanItem.getInstanceId());
        assertNull(loanItem.getInstanceHrid());
        assertNull(loanItem.getItemType());
        assertNull(loanItem.getLoanDate());
        assertNull(loanItem.getLoanDueDate());
        assertFalse(loanItem.isOverdue());
        assertNull(loanItem.getTitle());
        assertNull(loanItem.getAuthor());
        assertNull(loanItem.getLocation());
        assertNull(loanItem.getLocationCode());
        assertNull(loanItem.getCanRenew());
    }

    @Test
    public void testBuilder() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));
        final LoanItem loanItem = new LoanItem.LoanItemBuilder()
            .loanId("loanId")
            .itemId("itemId")
            .instanceId("instanceId")
            .instanceHrid("instanceHrid")
            .itemType("itemType")
            .loanDate(now)
            .loanDueDate(due)
            .overdue(false)
            .title("title")
            .author("author")
            .location("location")
            .locationCode("locationCode")
            .canRenew(true)
            .build();

        assertNotNull(loanItem);
        assertNotNull(loanItem.getLoanDate());
        assertNotNull(loanItem.getLoanDueDate());

        assertEquals("loanId", loanItem.getLoanId());
        assertEquals("itemId", loanItem.getItemId());
        assertEquals("instanceId", loanItem.getInstanceId());
        assertEquals("instanceHrid", loanItem.getInstanceHrid());
        assertEquals("itemType", loanItem.getItemType());
        assertEquals(now.toString(), loanItem.getLoanDate().toString());
        assertEquals(due.toString(), loanItem.getLoanDueDate().toString());
        assertEquals(false, loanItem.isOverdue());
        assertEquals("title", loanItem.getTitle());
        assertEquals("author", loanItem.getAuthor());
        assertEquals("location", loanItem.getLocation());
        assertEquals("locationCode", loanItem.getLocationCode());
        assertEquals(true, loanItem.getCanRenew());
    }

    @Test
    public void testUpdate() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));
        final Date later = Date.from(due.toInstant().plusSeconds(100));
        final Date dueLater = Date.from(later.toInstant().plusSeconds(100));

        final LoanItem loanItem = new LoanItem("loanId", "itemId", "instanceId", "instanceHrid", "itemType", now, due, false, "title", "author", "location", "locationCode", true);

        loanItem.setLoanId("updatedLoanId");
        loanItem.setItemId("updatedItemId");
        loanItem.setInstanceId("updatedInstanceId");
        loanItem.setInstanceHrid("updatedInstanceHrid");
        loanItem.setItemType("updatedItemType");
        loanItem.setLoanDate(later);
        loanItem.setLoanDueDate(dueLater);
        loanItem.setOverdue(true);
        loanItem.setTitle("updatedTitle");
        loanItem.setAuthor("updatedAuthor");
        loanItem.setLocation("updatedLocation");
        loanItem.setLocationCode("updatedLocationCode");
        loanItem.setCanRenew(false);

        assertNotNull(loanItem);
        assertNotNull(loanItem.getLoanDate());
        assertNotNull(loanItem.getLoanDueDate());

        assertEquals("updatedLoanId", loanItem.getLoanId());
        assertEquals("updatedItemId", loanItem.getItemId());
        assertEquals("updatedInstanceId", loanItem.getInstanceId());
        assertEquals("updatedInstanceHrid", loanItem.getInstanceHrid());
        assertEquals("updatedItemType", loanItem.getItemType());
        assertEquals(later.toString(), loanItem.getLoanDate().toString());
        assertEquals(dueLater.toString(), loanItem.getLoanDueDate().toString());
        assertEquals(true, loanItem.isOverdue());
        assertEquals("updatedTitle", loanItem.getTitle());
        assertEquals("updatedAuthor", loanItem.getAuthor());
        assertEquals("updatedLocation", loanItem.getLocation());
        assertEquals("updatedLocationCode", loanItem.getLocationCode());
        assertEquals(false, loanItem.getCanRenew());
    }

    @Test
    public void testEquals() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));

        final LoanItem loanItem1 = new LoanItem("loanId", "itemId", "instanceId", "instanceHrid", "itemType", now, due, false, "title", "author", "location", "locationCode", true);
        final LoanItem loanItem2 = new LoanItem("loanId", "itemId", "instanceId", "instanceHrid", "itemType", now, due, false, "title", "author", "location", "locationCode", true);
        final LoanItem loanItem3 = new LoanItem();

        assertTrue(loanItem1.equals(loanItem2));
        assertFalse(loanItem1.equals(loanItem3));
    }

}
