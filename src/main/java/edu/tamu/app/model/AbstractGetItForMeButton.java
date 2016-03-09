package edu.tamu.app.model;

import java.util.List;
import java.util.Map;

public abstract class AbstractGetItForMeButton implements GetItForMeButton {
	protected List<String> templateParameterKeys;
	
	protected String[] locationCodes;
	protected String[] itemTypeCodes;
	protected Integer[] itemStatusCodes;
	
	private String linkText="Default Link Text";
	private String SID="libcat:InProcess";
	private String cssClasses = "button-gifm";
	
	//button shows for all record types
	@Override
	public boolean fitsRecordType(String marcRecord) {
		return true;
	}

	//button shows for all record locations
	@Override
	public boolean fitsLocation(String locationCode) {
		return true;
	}

	//button shows for all item types
	@Override
	public boolean fitsItemType(String typeCode) {
		return true;
	}

	//button shows for all item statuses
	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		return true;
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
