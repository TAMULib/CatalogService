/* 
 * Loans.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.catalog.model.response.items;

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

/**
 * Class Loans.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Loans implements java.io.Serializable {
	
    /**
     * Field _institution.
     */
    private Institution _institution;

    /**
     * Loans constructor
     * 
     */
    public Loans() {
        super();
    }

    /**
     * Returns the value of field 'institution'.
     * 
     * @return      institution
     * 
     */
    public Institution getInstitution() {
        return this._institution;
    }

    /**
     * Sets the value of field 'institution'.
     * 
     * @param       institution     final Institution
     * 
     */
    public void setInstitution(final Institution institution) {
        this._institution = institution;
    }

}

