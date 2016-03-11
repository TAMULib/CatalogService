package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class GetIt1WeekButton extends AbstractGetItForMeButton {
	
	public GetIt1WeekButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("callNumber");
		this.templateParameterKeys.add("location");
		setLinkText("Get it: 1 week");
		setSID("libcat:Borrow");
	}

	@Override
	public boolean fitsLocation(String locationCode) {
		return (this.locationCodes != null) ? Arrays.asList(this.locationCodes).contains(locationCode):super.fitsLocation(locationCode);
	}

	//button shows for curr, normal, 14d, and newbook item types
	@Override
	public boolean fitsItemType(String itemTypeCode) {
		return (this.itemTypeCodes != null) ? Arrays.asList(this.itemTypeCodes).contains(itemTypeCode):super.fitsItemType(itemTypeCode);
	}

	//button shows for all item status
	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		return (this.itemStatusCodes != null) ? Arrays.asList(this.itemStatusCodes).contains(itemStatusCode):super.fitsItemStatus(itemStatusCode);
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		//String callNumber, String locationName
		return "getitforme.library.tamu.edu/illiad/EVANSLocal/openurl.asp?Action=10&Form=30&sid="+this.getSID()+
				"&rfe_dat="+templateParameters.get("callNumber")+":"+templateParameters.get("location");
	}
}
