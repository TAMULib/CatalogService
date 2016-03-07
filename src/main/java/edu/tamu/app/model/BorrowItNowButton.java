package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class BorrowItNowButton extends AbstractGetItForMeButton {
	
	public BorrowItNowButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("isbn");
		setLinkText("Borrow It Now");
		setSID("libcat:Borrow");
		setCssClasses(this.getCssClasses()+" button-borrowitnow");
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

	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		Integer[] itemStatuses = {2,3,4,5,6,7,12,13,14,17,18};
		return Arrays.asList(itemStatuses).contains(itemStatusCode);
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		return "gwla.relaisd2d.com/service-proxy/?command=mkauth&LS=TEXASAM&PI=TEXASAM&query=isbn%3D"+templateParameters.get("isbn");
	}
}
