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

        final LoanItem loanItem = new LoanItem("loanId", "itemId", "instanceId", now, due, false, "title", "author");

        assertNotNull(loanItem);
        assertNotNull(loanItem.getLoanDate());
        assertNotNull(loanItem.getLoanDueDate());

        assertEquals("loanId", loanItem.getLoanId());
        assertEquals("itemId", loanItem.getItemId());
        assertEquals("instanceId", loanItem.getInstanceId());
        assertEquals(now.toString(), loanItem.getLoanDate().toString());
        assertEquals(due.toString(), loanItem.getLoanDueDate().toString());
        assertEquals(false, loanItem.isOverdue());
        assertEquals("title", loanItem.getTitle());
        assertEquals("author", loanItem.getAuthor());
    }

    @Test
    public void testCreateNoArgs() {
        final LoanItem loanItem = new LoanItem();

        assertNotNull(loanItem);

        assertNull(loanItem.getLoanId());
        assertNull(loanItem.getItemId());
        assertNull(loanItem.getInstanceId());
        assertNull(loanItem.getLoanDate());
        assertNull(loanItem.getLoanDueDate());
        assertFalse(loanItem.isOverdue());
        assertNull(loanItem.getTitle());
        assertNull(loanItem.getAuthor());
    }

    @Test
    public void testBuilder() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));
        final LoanItem loanItem = new LoanItem.LoanItemBuilder()
            .loanId("loanId")
            .itemId("itemId")
            .instanceId("instanceId")
            .loanDate(now)
            .loanDueDate(due)
            .overdue(false)
            .title("title")
            .author("author")
            .build();

        assertNotNull(loanItem);
        assertNotNull(loanItem.getLoanDate());
        assertNotNull(loanItem.getLoanDueDate());

        assertEquals("loanId", loanItem.getLoanId());
        assertEquals("itemId", loanItem.getItemId());
        assertEquals("instanceId", loanItem.getInstanceId());
        assertEquals(now.toString(), loanItem.getLoanDate().toString());
        assertEquals(due.toString(), loanItem.getLoanDueDate().toString());
        assertEquals(false, loanItem.isOverdue());
        assertEquals("title", loanItem.getTitle());
        assertEquals("author", loanItem.getAuthor());
    }

    @Test
    public void testUpdate() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));
        final Date later = Date.from(due.toInstant().plusSeconds(100));
        final Date dueLater = Date.from(later.toInstant().plusSeconds(100));

        final LoanItem loanItem = new LoanItem("loanId", "itemId", "instanceId", now, due, false, "title", "author");

        loanItem.setLoanId("updatedLoanId");
        loanItem.setItemId("updatedItemId");
        loanItem.setInstanceId("updatedInstanceId");
        loanItem.setLoanDate(later);
        loanItem.setLoanDueDate(dueLater);
        loanItem.setOverdue(true);
        loanItem.setTitle("updatedTitle");
        loanItem.setAuthor("updatedAuthor");

        assertNotNull(loanItem);
        assertNotNull(loanItem.getLoanDate());
        assertNotNull(loanItem.getLoanDueDate());

        assertEquals("updatedLoanId", loanItem.getLoanId());
        assertEquals("updatedItemId", loanItem.getItemId());
        assertEquals("updatedInstanceId", loanItem.getInstanceId());
        assertEquals(later.toString(), loanItem.getLoanDate().toString());
        assertEquals(dueLater.toString(), loanItem.getLoanDueDate().toString());
        assertEquals(true, loanItem.isOverdue());
        assertEquals("updatedTitle", loanItem.getTitle());
        assertEquals("updatedAuthor", loanItem.getAuthor());
    }

    @Test
    public void testEquals() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));

        final LoanItem loanItem1 = new LoanItem("loanId", "itemId", "instanceId", now, due, false, "title", "author");
        final LoanItem loanItem2 = new LoanItem("loanId", "itemId", "instanceId", now, due, false, "title", "author");
        final LoanItem loanItem3 = new LoanItem();

        assertTrue(loanItem1.equals(loanItem2));
        assertFalse(loanItem1.equals(loanItem3));
    }

}
