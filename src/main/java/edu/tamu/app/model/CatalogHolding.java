package edu.tamu.app.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class CatalogHolding {
	private String marcRecordLeader;
	private String mfhd;
	private String isbn;
	private String title;
	private String author;
	private String publisher;
	private String place;
	private String year;
	
	private Map<String,Map<String,String>> catalogItems = new HashMap<String,Map<String,String>>();
	
	public CatalogHolding(String marcRecordLeader,String mfhd, String isbn, String title, String author, String publisher, String place, String year, Map<String,Map<String,String>> catalogItems) {
		this.setMarcRecordLeader(marcRecordLeader);
		this.setMfhd(mfhd);
		this.setCatalogItems(catalogItems);
		this.setIsbn(isbn);
		this.setTitle(title);
		this.setAuthor(author);
		this.setPublisher(publisher);
		this.setPlace(place);
		this.setYear(year);
	}
	
	public String getMarcRecordLeader() {
		return marcRecordLeader;
	}

	public void setMarcRecordLeader(String marcRecordLeader) {
		this.marcRecordLeader = marcRecordLeader;
	}
	
	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	
	public String getMfhd() {
		return mfhd;
	}

	public void setMfhd(String mfhd) {
		this.mfhd = mfhd;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Map<String, Map<String, String>> getCatalogItems() {
		return catalogItems;
	}

	public void setCatalogItems(Map<String, Map<String, String>> catalogItems) {
		this.catalogItems = catalogItems;
	}
	
	public boolean isMultiVolume() {
		return (this.getCatalogItems().size() > 1);
	}
	
	public String getValueByPropertyName(String propertyName) throws Exception {
		Class<?> c = Class.forName(this.getClass().getCanonicalName());
	    Method m = null;
		m = c.getDeclaredMethod("get"+StringUtils.capitalize(propertyName));
		return (String) m.invoke(this);
	}
}
