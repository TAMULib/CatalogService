package edu.tamu.app.model;

import java.util.List;
import java.util.Map;

public interface GetItForMeButton {
	boolean fitsRecordType(String marcRecord);
	boolean fitsLocation(String locationCode);
	boolean fitsItemType(String typeCode);
	boolean fitsItemStatus(int itemStatusCode);
	String getSID();
	void setSID(String SID);
	String getLinkTemplate(Map<String,String> templateParameters);
	List<String> getTemplateParameterKeys();
	String getLinkText();
	void setLinkText(String linkText);
	String getCssClasses();
	void setCssClasses(String cssClasses);
	void setLocationCodes(String[] locationCodes);
	void setItemTypeCodes(String[] itemTypeCodes);
	void setItemStatusCodes(Integer[] itemStatusCodes);
}
