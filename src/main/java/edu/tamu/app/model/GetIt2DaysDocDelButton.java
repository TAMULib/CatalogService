package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class GetIt2DaysDocDelButton extends AbstractGetItForMeButton {
	
	public GetIt2DaysDocDelButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("callNumber");
		this.templateParameterKeys.add("location");
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
		String[] locationCodes = {"base", "bsc", "curr", "curr,text", "nbs","stk", "stk,mov1", "tdoc", "udoc", "wein", "psel,stk", "west,audio", "west,nbs", "west,stk", "west,udoc"};
		return Arrays.asList(locationCodes).contains(locationCode);
	}

	//button shows for curr, normal, 14d, and newbook item types
	@Override
	public boolean fitsItemType(String itemTypeCode) {
		String[] itemTypeCodes = {"curr", "normal", "14d", "newbook","ser"};
		return Arrays.asList(itemTypeCodes).contains(itemTypeCode);
	}

	//button shows for item status 1 and 11
	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		Integer[] itemStatuses = {1,11};
		return Arrays.asList(itemStatuses).contains(itemStatusCode);
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		return "getitforme.library.tamu.edu/illiad/EVANSLocal/openurl.asp?Action=10&Form=30&sid="+this.getSID()+
				"&title="+templateParameters.get("title")+"&author="+templateParameters.get("author")+"&isbn="+templateParameters.get("isbn")+
				"&publisher="+templateParameters.get("publisher")+"&rfe_dat="+templateParameters.get("callNumber")+":"+templateParameters.get("location");
	}
}
