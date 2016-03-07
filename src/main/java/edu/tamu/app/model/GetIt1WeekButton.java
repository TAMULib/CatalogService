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
		String[] locationCodes = {"base", "bsc", "curr", "curr,text", "nbs","stk", "stk,mov1", "tdoc", "udoc", "wein", "psel,stk", "west,audio", "west,nbs", "west,stk", "west,udoc"};
		return Arrays.asList(locationCodes).contains(locationCode);
	}

	//button shows for all item types
	@Override
	public boolean fitsItemType(String itemTypeCode) {
		String[] itemTypeCodes = {"curr", "normal", "14d", "newbook","ser"};
		return Arrays.asList(itemTypeCodes).contains(itemTypeCode);
	}

	//button shows for all item status
	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		Integer[] itemStatuses = {5,12,13,14,17,18};
		return Arrays.asList(itemStatuses).contains(itemStatusCode);
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		//String callNumber, String locationName
		return "getitforme.library.tamu.edu/illiad/EVANSLocal/openurl.asp?Action=10&Form=30&sid="+this.getSID()+
				"&rfe_dat="+templateParameters.get("callNumber")+":"+templateParameters.get("location");
	}
}
