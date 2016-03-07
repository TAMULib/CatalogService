package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Map;

public final class GetIt2DaysButton extends AbstractGetItForMeButton {
	
	public GetIt2DaysButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("callNumber");
		this.templateParameterKeys.add("location");
		this.templateParameterKeys.add("itemBarcode");
		setLinkText("Get it: 2 days");
		setSID("libcat:InProcess");
	}

	//button shows for item status of 22 only
	@Override
	public boolean fitsItemStatus(int itemStatusCode) {
		if (itemStatusCode == 22) {
			return true;
		}
		return false;
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		return "getitforme.library.tamu.edu/illiad/EVANSLocal/openurl.asp?Action=10&Form=30&sid="+this.getSID()+
				"&title="+templateParameters.get("title")+"&author="+templateParameters.get("author")+"&isbn="+templateParameters.get("isbn")+
				"&publisher="+templateParameters.get("publisher")+"&rfe_dat="+templateParameters.get("callNumber")+":"+templateParameters.get("location")+"&Notes="+templateParameters.get("itemBarcode");
	}
}
