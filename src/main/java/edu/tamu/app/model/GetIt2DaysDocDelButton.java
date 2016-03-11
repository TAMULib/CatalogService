package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Map;

/**
 * The GetIt2DaysDocDel Button represents a request to get and hold an item.
 * 
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author Michael Nichols <mnichols@library.tamu.edu>
 *
 */

public final class GetIt2DaysDocDelButton extends AbstractGetItForMeButton {

	public GetIt2DaysDocDelButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("callNumber");
		this.templateParameterKeys.add("location");
		this.templateParameterKeys.add("title");
		this.templateParameterKeys.add("author");
		this.templateParameterKeys.add("isbn");
		this.templateParameterKeys.add("publisher");
		this.templateParameterKeys.add("itemBarcode");

		setLinkText("Get It: 2 days");
		setSID("libcat:DocDel");
		setCssClasses(this.getCssClasses()+" button-docdel");
	}

	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		return "getitforme.library.tamu.edu/illiad/EVANSLocal/openurl.asp?Action=10&Form=30&sid="+this.getSID()+
				"&title="+templateParameters.get("title")+"&author="+templateParameters.get("author")+"&isbn="+templateParameters.get("isbn")+
				"&publisher="+templateParameters.get("publisher")+"&rfe_dat="+templateParameters.get("callNumber")+":"+templateParameters.get("location")+
				"&Notes="+templateParameters.get("itemBarcode");
	}
}
