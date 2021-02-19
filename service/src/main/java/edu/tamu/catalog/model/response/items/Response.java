/* 
 * Response.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.catalog.model.response.items;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

/**
 * Class Response.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
@XmlRootElement(name="response")
public class Response implements java.io.Serializable {
	
    /**
     * Field _replyText.
     */
    private java.lang.String _replyText;

    /**
     * Field _replyCode.
     */
    private int _replyCode;

    /**
     * keeps track of state for field: _replyCode
     */
    private boolean _has_replyCode;

    /**
     * Field _loans.
     */
    private Loans _loans;
    
    /**
     * Response Constructor
     */
    public Response() {
        super();
    }

    /**
     * Sets the reply code to false
     * 
     */
    public void deleteReplyCode() {
        this._has_replyCode= false;
    }

    /**
     * Returns the value of field 'loans'.
     * 
     * @return      loans
     * 
     */
    public Loans getLoans() {
        return this._loans;
    }

    /**
     * Returns the value of field 'replyCode'.
     * 
     * @return      replyCode
     * 
     */
    public int getReplyCode() {
        return this._replyCode;
    }

    /**
     * Returns the value of field 'replyText'.
     * 
     * @return      replyText
     * 
     */
    public java.lang.String getReplyText() {
        return this._replyText;
    }

    /**
     * Returns true if at least one ReplyCode has been added
     * 
     */
    public boolean hasReplyCode() {
        return this._has_replyCode;
    }

    /**
     * Sets the value of field 'loans'.
     * 
     * @param       loans           final Loans
     * 
     */
    public void setLoans(final Loans loans) {
        this._loans = loans;
    }

    /**
     * Sets the value of field 'replyCode'.
     * 
     * @param       replyCode       final int
     * 
     */
    public void setReplyCode(final int replyCode) {
        this._replyCode = replyCode;
        this._has_replyCode = true;
    }

    /**
     * Sets the value of field 'replyText'.
     * 
     * @param       replyText       String
     * 
     */
    public void setReplyText(final java.lang.String replyText) {
        this._replyText = replyText;
    }

}

