package edu.tamu.app.model;

import java.util.List;
import java.util.Map;

/**
 * The essence of a GetItForMe Button
 * 
 * @author Jason Savell <jsavell@library.tamu.edu>
 *
 */
public interface GetItForMeButton {
	/**
	 * Button eligibility test based on an item's record type
	 * @param String marcRecord
	 * @return boolean
	 */
	boolean fitsRecordType(String marcRecord);
	
	/**
	 * Button eligibility test based on an item's location
	 * @param itemLocationCode
	 * @return boolean
	 */
	boolean fitsLocation(String itemLocationCode);

	/**
	 * Button eligibility test based on an item's type
	 * @param itemTypeCode
	 * @return boolean
	 */
	boolean fitsItemType(String itemTypeCode);

	/**
	 * Button eligibility test based on an item's status
	 * @param itemStatusCode
	 * @return boolean
	 */
	boolean fitsItemStatus(int itemStatusCode);
	
	/**
	 * Get the button's SID
	 * A button's SID is a unique string used to identify the button's type to the service it links to
	 * @return String SID
	 */
	String getSID();

	/**
	 * Get the button's SID
	 * A button's SID is a unique string used to identify the button's type to the service it links to
	 * @return void
	 */
	void setSID(String SID);
	
	/**
	 * Get the button's LinkTemplate
	 * A button's LinkTemplate is the HTML link that can be used in conjunction with the button's ParameterKeys to generate a unique button link for a given item 
	 * @return String
	 */
	String getLinkTemplate(Map<String,String> templateParameters);

	/**
	 * Get the button's TemplateParameterKeys
	 * A button's TemplateParameterKeys are used to populate the button's LinkTemplate with real data from a particular item
	 * @return List<String>
	 */
	List<String> getTemplateParameterKeys();
	
	/**
	 * Gets the user facing text for the button's link
	 * @return String linkText
	 */
	
	String getLinkText();
	/**
	 * Sets the user facing text for the button's link
	 * @return void
	 */
	void setLinkText(String linkText);
	
	/**
	 * Gets the CSS classes for a button. These are used to provide predictable, unique CSS classes for each button
	 * @return String cssClasses
	 */
	String getCssClasses();

	/**
	 * Sets the CSS classes for a button. These are used to provide predictable, unique CSS classes for each button
	 * @return void
	 */
	void setCssClasses(String cssClasses);
	
	/**
	 * Sets the valid location codes for a button to be used in the fitsLocation() test
	 */
	void setLocationCodes(String[] locationCodes);

	/**
	 * Sets the valid item types for a button to be used in the fitsItemType() test
	 */
	void setItemTypeCodes(String[] itemTypeCodes);
	
	/**
	 * Sets the valid item status codes for a button to be used in the fitsItemStatus() test
	 */
	void setItemStatusCodes(Integer[] itemStatusCodes);
}
