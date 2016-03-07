package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class GetIt4DaysButton extends AbstractGetItForMeButton {
	
	public GetIt4DaysButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("isbn");
		this.templateParameterKeys.add("title");
		this.templateParameterKeys.add("author");
		this.templateParameterKeys.add("year");
		this.templateParameterKeys.add("publisher");
		this.templateParameterKeys.add("place");
		this.templateParameterKeys.add("itemBarcode");

		setLinkText("Get it: 4 days");
		setSID("libcat:remotestorage");		
	}

	@Override
	public boolean fitsLocation(String locationCode) {
		String[] locationCodes = {"rs,hdr", "rs,jlf"};
		return Arrays.asList(locationCodes).contains(locationCode);
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		//String callNumber, String locationName
		return "getitforme.library.tamu.edu/illiad/EVANSLocal/openurl.asp?Action=10&Form=30&sid="+this.getSID()+
				"&rfe_dat="+templateParameters.get("itemBarcode")+
				"&title="+templateParameters.get("title")+"&author="+templateParameters.get("author")+"&isbn="+templateParameters.get("isbn")+
				"&publisher="+templateParameters.get("publisher")+"&place="+templateParameters.get("place")+"&date="+templateParameters.get("year");
	}
}
