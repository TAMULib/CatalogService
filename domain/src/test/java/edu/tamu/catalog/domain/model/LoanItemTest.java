package edu.tamu.catalog.domain.model;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class LoanItemTest {

    @Test
    public void testCreate() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));

        final LoanItem loanItem = new LoanItem("loanId", "itemId", "instanceId", now, due, false, "title", "author");

        Assert.assertNotNull(loanItem);
        Assert.assertNotNull(loanItem.getLoanDate());
        Assert.assertNotNull(loanItem.getLoanDueDate());

        Assert.assertEquals("loanId", loanItem.getLoanId());
        Assert.assertEquals("itemId", loanItem.getItemId());
        Assert.assertEquals("instanceId", loanItem.getInstanceId());
        Assert.assertEquals(now.toString(), loanItem.getLoanDate().toString());
        Assert.assertEquals(due.toString(), loanItem.getLoanDueDate().toString());
        Assert.assertEquals(false, loanItem.isOverdue());
        Assert.assertEquals("title", loanItem.getTitle());
        Assert.assertEquals("author", loanItem.getAuthor());
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

        Assert.assertNotNull(loanItem);
        Assert.assertNotNull(loanItem.getLoanDate());
        Assert.assertNotNull(loanItem.getLoanDueDate());

        Assert.assertEquals("updatedLoanId", loanItem.getLoanId());
        Assert.assertEquals("updatedItemId", loanItem.getItemId());
        Assert.assertEquals("updatedInstanceId", loanItem.getInstanceId());
        Assert.assertEquals(later.toString(), loanItem.getLoanDate().toString());
        Assert.assertEquals(dueLater.toString(), loanItem.getLoanDueDate().toString());
        Assert.assertEquals(true, loanItem.isOverdue());
        Assert.assertEquals("updatedTitle", loanItem.getTitle());
        Assert.assertEquals("updatedAuthor", loanItem.getAuthor());
    }

    @Test
    public void testEquals() {
        final Date now = new Date();
        final Date due = Date.from(now.toInstant().plusSeconds(100));

        final LoanItem loanItem1 = new LoanItem("loanId", "itemId", "instanceId", now, due, false, "title", "author");
        final LoanItem loanItem2 = new LoanItem("loanId", "itemId", "instanceId", now, due, false, "title", "author");
        final LoanItem loanItem3 = new LoanItem("different", "itemId", "instanceId", now, due, false, "title", "author");

        Assert.assertTrue(loanItem1.equals(loanItem2));
        Assert.assertFalse(loanItem1.equals(loanItem3));
    }

}