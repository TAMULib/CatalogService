package edu.tamu.app.utilities.sort;

import java.util.Comparator;
import java.util.Map;

public class VolumeComparator implements Comparator<Map<String,String>> {

	@Override
	public int compare(Map<String, String> o1, Map<String, String> o2) {
		AlphanumComparator alphaNumComp = new AlphanumComparator();
		return alphaNumComp.compare(o1.get("linkText"),o2.get("linkText"));
	}
}
