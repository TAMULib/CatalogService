/* 
 * Loan.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.catalog.model.response.renewals;

import javax.xml.bind.annotation.XmlAttribute;

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

/**
 * Class Loan.
 * 
 * @version $Revision$ $Date$
 */
/**
 * @author rmathew
 *
 */
@SuppressWarnings("serial")
public class Loan implements java.io.Serializable {

    /**
     * Field _canRenew.
     */
	@XmlAttribute(name="canRenew")
    private java.lang.String _canRenew;

    /**
     * Field _loanid.
     */
	@XmlAttribute(name="loanId")
    private java.lang.String _loanid;

    /**
     * Field _itemId.
     */
    private int _itemId;

    /**
     * keeps track of state for field: _itemId
     */
    private boolean _has_itemId;

    /**
     * Field _itemBarcode.
     */
    private java.lang.String _itemBarcode;

    /**
     * Field _dueDate.
     */
    private java.lang.String _dueDate;

    /**
     * Field _origDueDate.
     */
    private java.lang.String _origDueDate;

    /**
     * Field _todaysDate.
     */
    private java.lang.String _todaysDate;

    /**
     * Field _title.
     */
    private java.lang.String _title;

    /**
     * Field _author.
     */
    private Author _author;

    /**
     * Field _location.
     */
    private java.lang.String _location;

    /**
     * Field _locationCode.
     */
    private java.lang.String _locationCode;

    /**
     * Field _callNumber.
     */
    private java.lang.String _callNumber;

    /**
     * Field _statusCode.
     */
    private int _statusCode;

    /**
     * keeps track of state for field: _statusCode
     */
    private boolean _has_statusCode;

    /**
     * Field _statusText.
     */
    private java.lang.String _statusText;

    /**
     * Field _itemtype.
     */
    private java.lang.String _itemtype;

    /**
     * Field _dbKey.
     */
    private java.lang.String _dbKey;

    /**
     * Field _dbName.
     */
    private java.lang.String _dbName;

    /**
     * Field _renewalStatus.
     */
    private java.lang.String _renewalStatus;

    /**
     * Field _renewalStatusCode.
     */
    private java.lang.String _renewalStatusCode;

    /**
     * Loan Constructor
     * 
     */
    public Loan() {
        super();
    }

    /**
     * Sets the boolean value of _has_itemId to false 
     */
    public void deleteItemId() {
        this._has_itemId= false;
    }


    /**
     * Sets the boolean value of _has_statusCode to false 
     */
    public void deleteStatusCode() {
        this._has_statusCode= false;
    }

    /**
     * Returns the value of field 'author'.
     * 
     * @return      author
     * 
     */
    public Author getAuthor() {
        return this._author;
    }

    /**
     * Returns the value of field 'callNumber'.
     * 
     * @return      callNumber
     * 
     */
    public java.lang.String getCallNumber() {
        return this._callNumber;
    }

    /**
     * Returns the value of field 'canRenew'.
     * 
     * @return      canRenew
     * 
     */
    public java.lang.String getCanRenew() {
        return this._canRenew;
    }

    /**
     * Returns the value of field 'dbKey'.
     * 
     * @return      dbKey
     * 
     */
    public java.lang.String getDbKey() {
        return this._dbKey;
    }

    /**
     * Returns the value of field 'dbName'.
     * 
     * @return      dbName
     * 
     */
    public java.lang.String getDbName() {
        return this._dbName;
    }

    /**
     * Returns the value of field 'dueDate'.
     * 
     * @return      dueDate
     * 
     */
    public java.lang.String getDueDate() {
        return this._dueDate;
    }

    /**
     * Returns the value of field 'itemBarcode'.
     * 
     * @return      itemBarcode
     * 
     */
    public java.lang.String getItemBarcode() {
        return this._itemBarcode;
    }

    /**
     * Returns the value of field 'itemId'.
     * 
     * @return      itemId
     * 
     */
    public int getItemId() {
        return this._itemId;
    }

    /**
     * Returns the value of field 'itemtype'.
     * 
     * @return      itemtype
     * 
     */
    public java.lang.String getItemtype() {
        return this._itemtype;
    }

    /**
     * Returns the value of field 'loanid'.
     * 
     * @return      loanid
     * 
     */
    public java.lang.String getLoanid() {
        return this._loanid;
    }

    /**
     * Returns the value of field 'location'.
     * 
     * @return      location
     * 
     */
    public java.lang.String getLocation() {
        return this._location;
    }

    /**
     * Returns the value of field 'locationCode'.
     * 
     * @return      locationCode
     * 
     */
    public java.lang.String getLocationCode() {
        return this._locationCode;
    }

    /**
     * Returns the value of field 'origDueDate'.
     * 
     * @return      origDueDate
     * 
     */
    public java.lang.String getOrigDueDate() {
        return this._origDueDate;
    }

    /**
     * Returns the value of field 'renewalStatus'.
     * 
     * @return      renewalStatus
     * 
     */
    public java.lang.String getRenewalStatus() {
        return this._renewalStatus;
    }

    /**
     * Returns the value of field 'renewalStatusCode'.
     * 
     * @return      renewalStatusCode
     * 
     */
    public java.lang.String getRenewalStatusCode() {
        return this._renewalStatusCode;
    }

    /**
     * Returns the value of field 'statusCode'.
     * 
     * @return      statusCode
     * 
     */
    public int getStatusCode() {
        return this._statusCode;
    }

    /**
     * Returns the value of field 'statusText'.
     * 
     * @return      statusText
     * 
     */
    public java.lang.String getStatusText() {
        return this._statusText;
    }

    /**
     * Returns the value of field 'title'.
     * 
     * @return      title
     * 
     */
    public java.lang.String getTitle() {
        return this._title;
    }

    /**
     * Returns the value of field 'todaysDate'.
     * 
     * @return      todaysDate
     * 
     */
    public java.lang.String getTodaysDate() {
        return this._todaysDate;
    }

    /**
     * Returns true if at least one ItemId has been added
     *
     */
    public boolean hasItemId() {
        return this._has_itemId;
    }

    /**
     * Returns true if at least one StatusCode has been added
     * 
     */
    public boolean hasStatusCode() {
        return this._has_statusCode;
    }

    /**
     * Sets the value of field 'author'.
     * 
     * @param       author          final Author
     * 
     */
    public void setAuthor(final Author author) {
        this._author = author;
    }

    /**
     * Sets the value of field 'callNumber'.
     * 
     * @param       callNumber      String
     * 
     */
    public void setCallNumber(final java.lang.String callNumber) {
        this._callNumber = callNumber;
    }

    /**
     * Sets the value of field 'canRenew'.
     * 
     * @param       canRenew        String
     * 
     */
    public void setCanRenew(final java.lang.String canRenew) {
        this._canRenew = canRenew;
    }

    /**
     * Sets the value of field 'dbKey'.
     * 
     * @param       dbKey           String
     * 
     */
    public void setDbKey(final java.lang.String dbKey) {
        this._dbKey = dbKey;
    }

    /**
     * Sets the value of field 'dbName'.
     * 
     * @param       dbName          String
     * 
     */
    public void setDbName(final java.lang.String dbName) {
        this._dbName = dbName;
    }

    /**
     * Sets the value of field 'dueDate'.
     * 
     * @param       dueDate         String
     * 
     */
    public void setDueDate(final java.lang.String dueDate) {
        this._dueDate = dueDate;
    }

    /**
     * Sets the value of field 'itemBarcode'.
     * 
     * @param       itemBarcode     String
     * 
     */
    public void setItemBarcode(final java.lang.String itemBarcode) {
        this._itemBarcode = itemBarcode;
    }

    /**
     * Sets the value of field 'itemId'.
     * 
     * @param       itemId          final int
     * 
     */
    public void setItemId(final int itemId) {
        this._itemId = itemId;
        this._has_itemId = true;
    }

    /**
     * Sets the value of field 'itemtype'.
     * 
     * @param       itemtype        String
     * 
     */
    public void setItemtype(final java.lang.String itemtype) {
        this._itemtype = itemtype;
    }

    /**
     * Sets the value of field 'loanid'.
     * 
     * @param       loanid          String
     * 
     */
    public void setLoanid(final java.lang.String loanid) {
        this._loanid = loanid;
    }

    /**
     * Sets the value of field 'location'.
     * 
     * @param       location        String
     * 
     */
    public void setLocation(final java.lang.String location) {
        this._location = location;
    }

    /**
     * Sets the value of field 'locationCode'.
     * 
     * @param       locationCode    String
     * 
     */
    public void setLocationCode(final java.lang.String locationCode) {
        this._locationCode = locationCode;
    }

    /**
     * Sets the value of field 'origDueDate'.
     * 
     * @param       origDueDate     String
     * 
     */
    public void setOrigDueDate(final java.lang.String origDueDate) {
        this._origDueDate = origDueDate;
    }

    /**
     * Sets the value of field 'renewalStatus'.
     * 
     * @param       renewalStatus   String
     * 
     */
    public void setRenewalStatus(final java.lang.String renewalStatus) {
        this._renewalStatus = renewalStatus;
    }

    /**
     * Sets the value of field 'renewalStatusCode'.
     * 
     * @param       renewalStatusCode	String
     *  
     */
    public void setRenewalStatusCode(final java.lang.String renewalStatusCode) {
        this._renewalStatusCode = renewalStatusCode;
    }

    /**
     * Sets the value of field 'statusCode'.
     * 
     * @param       statusCode      final int
     * 
     */
    public void setStatusCode(final int statusCode) {
        this._statusCode = statusCode;
        this._has_statusCode = true;
    }

    /**
     * Sets the value of field 'statusText'.
     * 
     * @param       statusText      String
     * 
     */
    public void setStatusText(final java.lang.String statusText) {
        this._statusText = statusText;
    }

    /**
     * Sets the value of field 'title'.
     * 
     * @param       title           String
     * 
     */
    public void setTitle(final java.lang.String title) {
        this._title = title;
    }

    /**
     * Sets the value of field 'todaysDate'.
     * 
     * @param       todaysDate      String
     * 
     */
    public void setTodaysDate(final java.lang.String todaysDate) {
        this._todaysDate = todaysDate;
    }

}
