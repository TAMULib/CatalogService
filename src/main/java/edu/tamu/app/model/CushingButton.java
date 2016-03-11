package edu.tamu.app.model;

import java.util.ArrayList;
import java.util.Map;

/**
 * The Cushing Button represents a request for anything in the Cushing Library.
 * 
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author Michael Nichols <mnichols@library.tamu.edu>
 *
 */

public final class CushingButton extends AbstractGetItForMeButton {
	
	public CushingButton() {
		this.templateParameterKeys = new ArrayList<String>();
		this.templateParameterKeys.add("callNumber");
		this.templateParameterKeys.add("location");
		setLinkText("Request from Cushing");
		setSID("libcat:cushing");
		setCssClasses(this.getCssClasses()+" button-cushing");
	}

	@Override
	public boolean fitsLocation(String locationCode) {
		return locationCode.contains("cush");
	}
	
	@Override
	public String getLinkTemplate(Map<String,String> templateParameters) {
		//String callNumber, String locationName
		return "aeon.library.tamu.edu/aeonnew/openurl.asp?sid="+this.getSID()+
				"&callnumber="+templateParameters.get("callNumber")+"&location="+templateParameters.get("location");
	}
}
