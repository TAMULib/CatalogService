package edu.tamu.app.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An abstract implementation of the GetItForMeButton with
 * sensible defaults.
 * 
 * It's a good idea to extend this when creating a new GetItForMeButton.
 * 
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 */

public abstract class AbstractGetItForMeButton implements GetItForMeButton {
	protected List<String> templateParameterKeys;
	
	protected String[] locationCodes;
	protected String[] itemTypeCodes;
	protected Integer[] itemStatusCodes;
	
	private String linkText="Default Link Text";
	private String SID="libcat:InProcess";
	private String cssClasses = "button-gifm";
	
	@Override
	public boolean fitsRecordType(String marcRecord) {
		return true;
	}

	@Override
	public boolean fitsLocation(String locationCode) {
		return (this.locationCodes != null) ? Arrays.asList(this.locationCodes).contains(locationCode):true;
	}

	@Override
	public boolean fitsItemType(String itemTypeCode) {
		return (this.itemTypeCodes != null) ? Arrays.asList(this.itemTypeCodes).contains(itemTypeCode):true;
	}

	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		return (this.itemStatusCodes != null) ? Arrays.asList(this.itemStatusCodes).contains(itemStatusCode):true;
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		return "default template";
	}
	
	@Override
	public List<String> getTemplateParameterKeys() {
		return this.templateParameterKeys;
	}

	@Override
	public String getLinkText() {
		return linkText;
	}

	@Override
	public void setLinkText(String linkText) {
		this.linkText = linkText;		
	}

	@Override
	public String getSID() {
		return SID;
	}

	@Override
	public void setSID(String SID) {
		this.SID = SID;
	}

	@Override
	public String getCssClasses() {
		return this.cssClasses;
	}

	@Override
	public void setCssClasses(String cssClasses) {
		this.cssClasses = cssClasses;
	}
	
	public void setLocationCodes(String[] locationCodes) {
		this.locationCodes = locationCodes;
	}
	
	public void setItemTypeCodes(String[] itemTypeCodes) {
		this.itemTypeCodes = itemTypeCodes;
	}

	public void setItemStatusCodes(Integer[] itemStatusCodes) {
		this.itemStatusCodes = itemStatusCodes;
	}
}
