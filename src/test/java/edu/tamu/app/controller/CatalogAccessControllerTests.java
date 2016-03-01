package edu.tamu.app.controller;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.tamu.app.service.CatalogService;

public class CatalogAccessControllerTests {
	
	CatalogService mockCatalogService;
	
	@Before
	public void setUp()
	{
		mockCatalogService = new MockCatalogService();
	}
	
	@Test
	public void getHoldingsByBibIdTest() 
	{
		assertNull("Result for BidId foo was not null!", mockCatalogService.getHoldingsByBibId("foo"));
	}

}
