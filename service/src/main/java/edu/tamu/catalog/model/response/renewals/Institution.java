/* 
 * Institution.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.catalog.model.response.renewals;

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

/**
 * Class Institution.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Institution implements java.io.Serializable {

	/**
     * Field _id.
     */
    private java.lang.String _id;

    /**
     * Field _instName.
     */
    private java.lang.String _instName;

    /**
     * Field _loanList.
     */
    private java.util.Vector<Loan> _loanList;

    /**
     * Constructor Institution
     * 
     */
    public Institution() {
        super();
        this._loanList = new java.util.Vector<Loan>();
    }

    /**
     * Adds loan object to the loan list at the given index
     * 
     * @param       vLoan           final Loan
     * 
     * @throws      IndexOutOfBoundsException
     * 
     */
    public void addLoan(final Loan vLoan)
    throws java.lang.IndexOutOfBoundsException {
        this._loanList.addElement(vLoan);
    }

    /**
     * Adds loan objects to the loan list at the given index
     * 
     * @param       index           final int index
     * @param       vLoan           final Loan
     * 
     * @throws      IndexOutOfBoundsException
     * 
     */
    public void addLoan(final int index, final Loan vLoan) throws java.lang.IndexOutOfBoundsException {
        this._loanList.add(index, vLoan);
    }

    /**
     * Returns an Enumeration over all Loan elements
     * 
     */
    public java.util.Enumeration<? extends Loan> enumerateLoan() {
        return this._loanList.elements();
    }

    /**
     * Returns the value of field 'id'.
     * 
     * @return      id
     * 
     */
    public java.lang.String getId() {
        return this._id;
    }

    /**
     * Returns the value of field 'instName'.
     * 
     * @return      instName
     * 
     */
    public java.lang.String getInstName() {
        return this._instName;
    }

    /**
     * Returns the value of the Loan at the given index
     * 
     * @param       index           final int
     * 
     * @throws      IndexOutOfBoundsException
     * 
     */
    public Loan getLoan(final int index) throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._loanList.size()) {
            throw new IndexOutOfBoundsException("getLoan: Index value '" + index + "' not in range [0.." + (this._loanList.size() - 1) + "]");
        }

        return (Loan) _loanList.get(index);
    }

    /**
     * Returns this collection as an Array
     * 
     */
    public Loan[] getLoan() {
        Loan[] array = new Loan[0];
        return (Loan[]) this._loanList.toArray(array);
    }

    /**
     * Returns the size of this collection 
     */
    public int getLoanCount() {
        return this._loanList.size();
    }

    /**
     * Removes all loans from the loan list
     */
    public void removeAllLoan() {
        this._loanList.clear();
    }

    /**
     * Returns true if the object was removed from the collection.
     * 
     * @param       vLoan           final Loan
     * 
     */
    public boolean removeLoan(final Loan vLoan) {
        boolean removed = _loanList.remove(vLoan);
        return removed;
    }

    /**
     * Returns the element removed from the collection
     * 
     * @param       index           final int
     * 
     */
    public Loan removeLoanAt(final int index) {
        java.lang.Object obj = this._loanList.remove(index);
        return (Loan) obj;
    }

    /**
     * Sets the value of field 'id'.
     * 
     * @param       id              String
     * 
     */
    public void setId(final java.lang.String id) {
        this._id = id;
    }

    /**
     * Sets the value of field 'instName'.
     * 
     * @param       instName        String
     * 
     */
    public void setInstName(final java.lang.String instName) {
        this._instName = instName;
    }

    /**
     * Sets the loan object at the index passed
     * 
     * @param       index           final int
     * @param       vLoan           final Loan
     * 
     * @throws      IndexOutOfBoundsException
     *  
     */
    public void setLoan(final int index, final Loan vLoan) throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._loanList.size()) {
            throw new IndexOutOfBoundsException("setLoan: Index value '" + index + "' not in range [0.." + (this._loanList.size() - 1) + "]");
        }

        this._loanList.set(index, vLoan);
    }

    /**
     * Adds the loan array to the loan list
     * 
     * @param       vLoanArray      final Loan[]
     * 
     */
    public void setLoan(final Loan[] vLoanArray) {
        //-- copy array
        _loanList.clear();

        for (int i = 0; i < vLoanArray.length; i++) {
                this._loanList.add(vLoanArray[i]);
        }
    }

}
