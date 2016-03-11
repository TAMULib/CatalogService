package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class GetIt2DaysDocDelButton extends AbstractGetItForMeButton {

	public GetIt2DaysDocDelButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("callNumber");
		this.templateParameterKeys.add("location");
		this.templateParameterKeys.add("title");
		this.templateParameterKeys.add("author");
		this.templateParameterKeys.add("isbn");
		this.templateParameterKeys.add("publisher");

		setLinkText("Get It: 2 days");
		setSID("libcat:DocDel");
		setCssClasses(this.getCssClasses()+" button-docdel");
	}

	@Override
	public boolean fitsLocation(String locationCode) {
		return (this.locationCodes != null) ? Arrays.asList(this.locationCodes).contains(locationCode):super.fitsLocation(locationCode);
	}

	@Override
	public boolean fitsItemType(String itemTypeCode) {
		return (this.itemTypeCodes != null) ? Arrays.asList(this.itemTypeCodes).contains(itemTypeCode):super.fitsItemType(itemTypeCode);
	}

	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		return (this.itemStatusCodes != null) ? Arrays.asList(this.itemStatusCodes).contains(itemStatusCode):super.fitsItemStatus(itemStatusCode);
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		return "getitforme.library.tamu.edu/illiad/EVANSLocal/openurl.asp?Action=10&Form=30&sid="+this.getSID()+
				"&title="+templateParameters.get("title")+"&author="+templateParameters.get("author")+"&isbn="+templateParameters.get("isbn")+
				"&publisher="+templateParameters.get("publisher")+"&rfe_dat="+templateParameters.get("callNumber")+":"+templateParameters.get("location");
	}
}
