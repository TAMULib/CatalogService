/* 
 * MockVoyagerServiceController.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.catalog.mock.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.catalog.model.response.items.Institution;
import edu.tamu.catalog.model.response.items.Loans;
import edu.tamu.catalog.model.response.renewals.Renewal;

/** 
 * Mock Voyager Service. 
 * 
 * @author
 *
 */
@RestController
@RequestMapping("/vxws")
public class MockVoyagerServiceController {
	
	private Loans loans;
	
	@SuppressWarnings("unused")
	private Renewal renewedLoans;
	
	/**
	 * @param       request         HttpServletRequest
	 * 
	 * @return      voyagerServiceData
	 * 
	 * @throws      IOException
	 * 
	 */
	@RequestMapping(value = "/AuthenticatePatronService", method = RequestMethod.POST)
	public String authenticate(HttpServletRequest request) throws IOException {
		
		ServletInputStream input = request.getInputStream();
		
		String xmlResponse = ConvertToString(input);
		
		String startString = "<ser:authFactor type=\"I\">";
		String endString = "</ser:authFactor>";
		
		int start = xmlResponse.indexOf(startString);
		int stop = xmlResponse.indexOf(endString);
		
		String uin = xmlResponse.substring(start + startString.length(), stop);
		
		String patronId, patronHomeUbId, lastName, barcode, fullName;
		
		if(uin.equals("123456789")) {
			patronId = "123456";
			patronHomeUbId = "1@AMDB20020820112825";
			lastName = "Daniels";
			barcode = "1234567898765432";
			fullName = "Jack Daniels";
		}
		else {
			patronId = "654321";
			patronHomeUbId = "1@AMDB20020820112825";
			lastName = "Boring";
			barcode = "9876543212345678";
			fullName = "Bob Boring";
		}
		
		
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<ser:voyagerServiceData xmlns:ser=\"http://www.endinfosys.com/Voyager/serviceParameters\">" +
			    "<ser:serviceData xsi:type=\"pat:patronAuthenticationType\" xmlns:pat=\"http://www.endinfosys.com/Voyager/patronAuthentication\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
			        "<pat:patronIdentifier patronId=\""+patronId+"\" lastName=\""+lastName+"\" patronHomeUbId=\""+patronHomeUbId+"\">" +
			            "<pat:authFactor type=\"I\">"+uin+"</pat:authFactor>" +
			        "</pat:patronIdentifier>" +
			        "<pat:patronGroups>" +
			            "<pat:groupIds>" +
			                "<pat:id>2</pat:id>" +
			            "</pat:groupIds>" +
			            "<pat:groupCodes>" +
			                "<pat:groupCode>" +
			                    "<pat:databaseCode>LOCAL</pat:databaseCode>" +
			                    "<pat:group>fast</pat:group>" +
			                "</pat:groupCode>" +
			            "</pat:groupCodes>" +
			        "</pat:patronGroups>" +
			        "<pat:isLocalPatron>Y</pat:isLocalPatron>" +
			        "<pat:barcode>"+barcode+"</pat:barcode>" +
			        "<pat:institutionId>"+uin+"</pat:institutionId>" +
			        "<pat:fullName>"+fullName+"</pat:fullName>" +
			        "<pat:lastName>"+lastName+"</pat:lastName>" +
			    "</ser:serviceData>" +
			"</ser:voyagerServiceData>";    	
	}
	
	/**
	 * @param       is              ServletInputStream
	 * 
	 * @return      sb
	 * 
	 */
	private static String ConvertToString(ServletInputStream is) {
	 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
	 
		String line;
		try {	 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		return sb.toString();
	 
	}

	/**
	 * Returns fines xml response
	 * 
	 * @param       request         HttpServletRequest
	 *
	 */
	@RequestMapping(value = "/patron/{patronId}/circulationActions/debt/fines", method = RequestMethod.GET)
	public String fines(HttpServletRequest request) {
		
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<response>" +
			    "<reply-text>ok</reply-text>" +
			    "<reply-code>0</reply-code>" +
			    "<fines>" +
			        "<institution id=\"LOCAL\">" +
			            "<instName>Texas A&amp;M University General Libraries Catalog</instName>" +
			            "<balance>" +
			                "<finesum>USD 65.50</finesum>" +
			            "</balance>" +
			            "<fine href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/debt/fines/335368?patron_homedb=1@AMDB20020820112825\">" +
			                "<fineId>335368</fineId>" +
			                "<fineDate>2015-02-26</fineDate>" +
			                "<itemTitle/>" +
			                "<fineType>Lost Item Processing</fineType>" +
			                "<amount>USD 2.00</amount>" +
			                "<dbKey>AMDB20020820112825</dbKey>" +
			                "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			            "</fine>" +
			            "<fine href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/debt/fines/335367?patron_homedb=1@AMDB20020820112825\">" +
			                "<fineId>335367</fineId>" +
			                "<fineDate>2015-02-26</fineDate>" +
			                "<itemTitle>Wireless laptops </itemTitle>" +
			                "<fineType>Lost Equipment Processing</fineType>" +
			                "<amount>USD 140.00</amount>" +
			                "<dbKey>AMDB20020820112825</dbKey>" +
			                "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			            "</fine>" +
			            "<fine href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/debt/fines/335367?patron_homedb=1@AMDB20020820112825\">" +
			                "<fineId>335367</fineId>" +
			                "<fineDate>2015-03-02</fineDate>" +
			                "<itemTitle/>" +
			                "<fineType>Forgive</fineType>" +
			                "<amount>USD 95.00</amount>" +
			                "<dbKey>AMDB20020820112825</dbKey>" +
			                "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			            "</fine>" +
			            "<fine href=\"http://127.0.0.1:7414/vxws/patron/344968/circulationActions/debt/fines/7632?patron_homedb=1@MSDB20020820112902\">" +
			                "<fineId>7632</fineId>" +
			                "<fineDate>2015-02-26</fineDate>" +
			                "<itemTitle>Excerpts from classics in allergy. </itemTitle>" +
			                "<fineType>Overdue</fineType>" +
			                "<amount>USD 0.50</amount>" +
			                "<dbKey>MSDB20020820112902</dbKey>" +
			                "<dbName>Medical Sciences Library</dbName>" +
			            "</fine>" +
			            "<fine href=\"http://127.0.0.1:7414/vxws/patron/344968/circulationActions/debt/fines/7631?patron_homedb=1@MSDB20020820112902\">" +
			                "<fineId>7631</fineId>" +
			                "<fineDate>2015-02-26</fineDate>" +
			                "<itemTitle>Complete dog book : official publication of the American Kennel Club. </itemTitle>" +
			                "<fineType>Lost Item Replacement</fineType>" +
			                "<amount>USD 15.00</amount>" +
			                "<dbKey>MSDB20020820112902</dbKey>" +
			                "<dbName>Medical Sciences Library</dbName>" +
			            "</fine>" +
			            "<fine href=\"http://127.0.0.1:7414/vxws/patron/344968/circulationActions/debt/fines/7630?patron_homedb=1@MSDB20020820112902\">" +
			                "<fineId>7630</fineId>" +
			                "<fineDate>2015-02-26</fineDate>" +
			                "<itemTitle>Canine and feline dermatology : a systematic approach / Gene H. Nesbitt. </itemTitle>" +
			                "<fineType>Overdue</fineType>" +
			                "<amount>USD 3.00</amount>" +
			                "<dbKey>MSDB20020820112902</dbKey>" +
			                "<dbName>Medical Sciences Library</dbName>" +
			            "</fine>" +
			        "</institution>" +
			    "</fines>" +
			"</response>";  	
	}
	
	/**
	 * Returns xml repsonse of requests
	 * 
	 * @param       request         HttpServletRequest
	 * 
	 */
	@RequestMapping(value = "/patron/{patronId}/circulationActions/requests/holds", method = RequestMethod.GET)
	public String requests(HttpServletRequest request) {
		
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<response>" +
			    "<reply-text>ok</reply-text>" +
			    "<reply-code>0</reply-code>" +
			    "<holds>" +
			        "<institution id=\"LOCAL\">" +
			            "<instName>Texas A&amp;M University General Libraries Catalog</instName>" +
			            "<hold href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/requests/holds/AMDB20020820112825|384207?patron_homedb=1@AMDB20020820112825\">" +
			                "<requestItem>" +
			                    "<itemId>3356015</itemId>" +
			                    "<holdRecallId>384207</holdRecallId>" +
			                    "<replyNote/>" +
			                    "<status>1</status>" +
			                    "<statusText>Position 1: Expires 2015-06-03</statusText>" +
			                    "<holdType>R</holdType>" +
			                    "<itemTitle>Sandman : a game of you / written by Neil Gailman ; illustrated by Shawn McManus, Colleen Doran, Bryan Talbot, George Pratt, Stan Woch, and Dick Giordano ; lettered by Todd Klein ; colored by Danny Vozzo ; covers by Dave McKean ; introduced by Samuel R. D</itemTitle>" +
			                    "<expiredDate>2015-06-03</expiredDate>" +
			                    "<dbKey>AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                    "<queuePosition>1</queuePosition>" +
			                    "<pickupLocation>Ask Us Desk (Evans Library, 1st floor)</pickupLocation>" +
			                    "<pickupLocationCode>circ</pickupLocationCode>" +
			                "</requestItem>" +
			            "</hold>" +
			            "<hold href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/requests/holds/AMDB20020820112825|384209?patron_homedb=1@AMDB20020820112825\">" +
			                "<requestItem>" +
			                    "<itemId>4228365</itemId>" +
			                    "<holdRecallId>384209</holdRecallId>" +
			                    "<replyNote/>" +
			                    "<status>1</status>" +
			                    "<statusText>Position 1: Expires 2015-06-04</statusText>" +
			                    "<holdType>R</holdType>" +
			                    "<itemTitle>Cold days [sound recording] : a novel of the Dresden files / Jim Butcher.</itemTitle>" +
			                    "<expiredDate>2015-06-04</expiredDate>" +
			                    "<dbKey>AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                    "<queuePosition>1</queuePosition>" +
			                    "<pickupLocation>Ask Us Desk (Evans Library, 1st floor)</pickupLocation>" +
			                    "<pickupLocationCode>circ</pickupLocationCode>" +
			                "</requestItem>" +
			            "</hold>" +
			            "<hold href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/requests/holds/AMDB20020820112825|384210?patron_homedb=1@AMDB20020820112825\">" +
			                "<requestItem>" +
			                    "<itemId>3873184</itemId>" +
			                    "<holdRecallId>384210</holdRecallId>" +
			                    "<replyNote/>" +
			                    "<status>1</status>" +
			                    "<statusText>Position 1: Expires 2015-06-04</statusText>" +
			                    "<holdType>R</holdType>" +
			                    "<itemTitle>Turn coat : a novel of the Dresden files / Jim Butcher.</itemTitle>" +
			                    "<expiredDate>2015-06-04</expiredDate>" +
			                    "<dbKey>AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                    "<queuePosition>1</queuePosition>" +
			                    "<pickupLocation>West Campus Library Circulation Desk</pickupLocation>" +
			                    "<pickupLocationCode>west,circ</pickupLocationCode>" +
			                "</requestItem>" +
			            "</hold>" +
			            "<hold href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/requests/holds/AMDB20020820112825|384211?patron_homedb=1@AMDB20020820112825\">" +
			                "<requestItem>" +
			                    "<itemId>3699595</itemId>" +
			                    "<holdRecallId>384211</holdRecallId>" +
			                    "<replyNote/>" +
			                    "<status>1</status>" +
			                    "<statusText>Position 1: Expires 2015-09-02</statusText>" +
			                    "<holdType>H</holdType>" +
			                    "<itemTitle>Crack in the edge of the world [sound recording] / written and read by Simon Winchester.</itemTitle>" +
			                    "<expiredDate>2015-09-02</expiredDate>" +
			                    "<dbKey>AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                    "<queuePosition>1</queuePosition>" +
			                    "<pickupLocation>Medical Sciences Library Client Services Desk</pickupLocation>" +
			                    "<pickupLocationCode>msl,circ</pickupLocationCode>" +
			                "</requestItem>" +
			            "</hold>" +
			            "<hold href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/requests/holds/AMDB20020820112825|384212?patron_homedb=1@AMDB20020820112825\">" +
			                "<requestItem>" +
			                    "<itemId>2536481</itemId>" +
			                    "<holdRecallId>384212</holdRecallId>" +
			                    "<replyNote/>" +
			                    "<status>1</status>" +
			                    "<statusText>Position 1: Expires 2015-09-02</statusText>" +
			                    "<holdType>H</holdType>" +
			                    "<itemTitle>Professor and the madman : a tale of murder, insanity, and the making of the Oxford English dictionary / Simon Winchester.</itemTitle>" +
			                    "<expiredDate>2015-09-02</expiredDate>" +
			                    "<dbKey>AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                    "<queuePosition>1</queuePosition>" +
			                    "<pickupLocation>Ask Us Desk (Evans Library, 1st floor)</pickupLocation>" +
			                    "<pickupLocationCode>circ</pickupLocationCode>" +
			                "</requestItem>" +
			            "</hold>" +
			            "<hold href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/requests/holds/AMDB20020820112825|384213?patron_homedb=1@AMDB20020820112825\">" +
			                "<requestItem>" +
			                    "<itemId>1131082</itemId>" +
			                    "<holdRecallId>384213</holdRecallId>" +
			                    "<replyNote/>" +
			                    "<status>1</status>" +
			                    "<statusText>Position 1: Expires 2015-09-02</statusText>" +
			                    "<holdType>H</holdType>" +
			                    "<itemTitle>Saints and scholars; twenty-five medieval portraits.</itemTitle>" +
			                    "<expiredDate>2015-09-02</expiredDate>" +
			                    "<dbKey>AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                    "<queuePosition>1</queuePosition>" +
			                    "<pickupLocation>Ask Us Desk (Evans Library, 1st floor)</pickupLocation>" +
			                    "<pickupLocationCode>circ</pickupLocationCode>" +
			                "</requestItem>" +
			            "</hold>" +
			        "</institution>" +
			    "</holds>" +
			"</response>";
	}
	
	/**
	 * Returns xml response for cancel action
	 * 
	 * @param       request         HttpServletRequest
	 * 
	 */
	@RequestMapping(value = "/patron/{patronId}/circulationActions/requests/holds/{homeDBpatronId}", method = RequestMethod.DELETE)
	public String cancel(HttpServletRequest request) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><reply-text>ok</reply-text><reply-code>0</reply-code><cancel-hold><note type=\"info\"><note>Request cancelled.</note></note></cancel-hold></response>";	
	}
	
	
	/**
	 * Returns xml response for loans
	 * 
	 * @param       request         HttpServletRequest
	 * 
	 * @throws JAXBException
	 * 
	 */
	@RequestMapping(value = "/patron/{patronId}/circulationActions/loans", method = RequestMethod.GET)
	public String loans(HttpServletRequest request) throws JAXBException {
		
		String xmlResponse = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<response>" +
			    "<reply-text>ok</reply-text>" +
			    "<reply-code>0</reply-code>" +
			    "<loans>" +
			        "<institution id=\"LOCAL\">" +
			            "<instName>Texas A&amp;M University General Libraries</instName>" +
			            "<loan canRenew=\"N\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|4159197?patron_homedb=1@AMDB20020820112825\">" +
			                "<itemId>4159197</itemId>" +
			                "<itemBarcode>A14841341975</itemBarcode>" +
			                "<dueDate>2015-02-25 08:46</dueDate>" +
			                "<origDueDate>2015-02-25 08:46</origDueDate>" +
			                "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                "<title>Alkaline hydrothermal vent hypothesis of the origin of life on earth.</title>" +
			                "<author xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
			                    "<location>Reserves (Annex 4th floor)</location>" +
			                    "<locationCode>edms,res</locationCode>" +
			                    "<callNumber>QH541.5.D35 A43 2011</callNumber>" +
			                    "<statusCode>2</statusCode>" +
			                    "<statusText>Charged (Requests: 0)</statusText>" +
			                    "<itemtype>4 hours &lt;T></itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|3424014?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>3424014</itemId>" +
			                    "<itemBarcode>A14832900796</itemBarcode>" +
			                    "<dueDate>2015-03-18 17:00</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Academ's fury / Jim Butcher.</title>" +
			                    "<author>Butcher, Jim, 1971- </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PS3602.U85 A63 2005</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|1087369?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>1087369</itemId>" +
			                    "<itemBarcode>A14804272779</itemBarcode>" +
			                    "<dueDate>2016-03-06 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Bull from the sea.</title>" +
			                    "<author>Renault, Mary. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.R2913 Bu</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|1087371?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>1087371</itemId>" +
			                    "<itemBarcode>A14804272795</itemBarcode>" +
			                    "<dueDate>2016-03-06 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Fire from heaven.</title>" +
			                    "<author>Renault, Mary. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.R2913 Fi</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|529683?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>529683</itemId>" +
			                    "<itemBarcode>A14801981476</itemBarcode>" +
			                    "<dueDate>2016-03-06 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Greek way.</title>" +
			                    "<author>Hamilton, Edith. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>DF77 .H34 1958</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|591832?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>591832</itemId>" +
			                    "<itemBarcode>A14804194696</itemBarcode>" +
			                    "<dueDate>2016-03-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Sweet danger.</title>" +
			                    "<author>Allingham, Margery, 1904-1966. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.A4372 Sw5</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|1078416?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>1078416</itemId>" +
			                    "<itemBarcode>A14804277054</itemBarcode>" +
			                    "<dueDate>2016-03-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Clouds of witness.</title>" +
			                    "<author>Sayers, Dorothy L. (Dorothy Leigh), 1893-1957. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.S2738 C7</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|762034?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>762034</itemId>" +
			                    "<itemBarcode>A14804277127</itemBarcode>" +
			                    "<dueDate>2016-03-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Strong poison / [by] Dorothy L. Sayers.</title>" +
			                    "<author>Sayers, Dorothy L. (Dorothy Leigh), 1893-1957. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.S2738 St13</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|35392?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>35392</itemId>" +
			                    "<itemBarcode>A14801334263</itemBarcode>" +
			                    "<dueDate>2016-03-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Pigs have wings.</title>" +
			                    "<author>Wodehouse, P. G. (Pelham Grenville), 1881-1975. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PR6045.O53 P53 1952</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\" href=\"http://127.0.0.1:9000/vxws/patron/123456/circulationActions/loans/1@AMDB20020820112825|1078490?patron_homedb=1@AMDB20020820112825\">" +
			                    "<itemId>1078490</itemId>" +
			                    "<itemBarcode>A14804252240</itemBarcode>" +
			                    "<dueDate>2016-03-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Till we have faces : a myth retold / C.S. Lewis.</title>" +
			                    "<author>Lewis, C. S. (Clive Staples), 1898-1963. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.L58534 Ti2</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<partial>N</partial>" +
			            "</institution>" +
			        "</loans>" +
			    "</response>";
		
		InputStream xmlInputStream = new ByteArrayInputStream(xmlResponse.getBytes());
		
		JAXBContext jaxbContext = JAXBContext.newInstance(edu.tamu.catalog.model.response.items.Response.class);
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		edu.tamu.catalog.model.response.items.Response response = (edu.tamu.catalog.model.response.items.Response) unmarshaller.unmarshal(xmlInputStream);
		
		loans = response.getLoans();
		return xmlResponse;
	}
	
	/**
	 * Returns xml response for the renewals
	 * 
	 * @param       homeDBpatronId  @PathVariable("homeDBpatronId") String
	 * @param       request         HttpServletRequest
	 * 
	 * @throws Exception
	 * 
	 */
	@RequestMapping(value = "/patron/{patronId}/circulationActions/loans/{homeDBpatronId}", method = RequestMethod.POST)
	public String renew(@PathVariable("homeDBpatronId") String homeDBpatronId, HttpServletRequest request) throws Exception {
		
		String itemId = homeDBpatronId.substring(homeDBpatronId.indexOf("|") + 1);
		
		if(itemId.equals("35392")) {
			throw new Exception();
		}
		
		Institution institution = loans.getInstitution();
		
		Enumeration<? extends edu.tamu.catalog.model.response.items.Loan> loanEnum = institution.enumerateLoan();
		
		edu.tamu.catalog.model.response.items.Loan targetLoan = null;
		
		while(loanEnum.hasMoreElements()) {
			edu.tamu.catalog.model.response.items.Loan loan = loanEnum.nextElement();
			if(Integer.parseInt(itemId) == loan.getItemId()) {
				targetLoan = loan;
				break;
			}
		}
		
		if(targetLoan == null) {
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<response>" +
				    	"<reply-text>error</reply-text>" +
				    	"<reply-code>-1</reply-code>" +
				    "</response>";
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		Date date = formatter.parse(targetLoan.getDueDate());
		
		Calendar calander = Calendar.getInstance();
		calander.setTime(date);
		calander.add(Calendar.MONTH, 5);
		
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<response>" +
			    "<reply-text>ok</reply-text>" +
			    "<reply-code>0</reply-code>" +
			    "<renewal>" +
			        "<institution id=\"LOCAL\">" +
			            "<instName>"+institution.getInstName().replace("&", "&amp;")+"</instName>" +
			            "<loan canRenew=\""+targetLoan.getCanRenew()+"\" loanid=\"" + targetLoan.getDbKey() + "|" + targetLoan.getItemId() + "\">" +
			                "<itemId>"+targetLoan.getItemId()+"</itemId>" +
			                "<itemBarcode>"+targetLoan.getItemBarcode()+"</itemBarcode>" +
			                "<dueDate>"+formatter.format(calander.getTime())+"</dueDate>" +
			                "<origDueDate>"+targetLoan.getOrigDueDate()+"</origDueDate>" +
			                "<todaysDate>"+targetLoan.getTodaysDate()+"</todaysDate>" +
			                "<title>"+targetLoan.getTitle()+"</title>" +
			                "<author>"+targetLoan.getAuthor().getXsi()+"</author>" +
			                "<location>"+targetLoan.getLocation()+"</location>" +
			                "<locationCode>"+targetLoan.getLocationCode()+"</locationCode>" +
			                "<callNumber>"+targetLoan.getCallNumber()+"</callNumber>" +
			                "<statusCode>"+targetLoan.getStatusCode()+"</statusCode>" +
			                "<statusText>"+targetLoan.getStatusText()+"</statusText>" +
			                "<itemtype>"+targetLoan.getItemtype()+"</itemtype>" +
			                "<dbKey>"+targetLoan.getDbKey()+"</dbKey>" +
			                "<dbName>"+targetLoan.getDbName().replace("&", "&amp;")+"</dbName>" +
			                "<renewalStatus>Success</renewalStatus>" +
			                "<renewalStatusCode>Y</renewalStatusCode>" +
			            "</loan>" +
			        "</institution>" +
			    "</renewal>" +
			"</response>";
	}
	
	/**
	 * Returns xml response for renewAll items
	 * 
	 * @param       request         HttpServletRequest
	 * 
	 * @throws JAXBException
	 * 
	 */
	@RequestMapping(value = "/patron/{patronId}/circulationActions/loans", method = RequestMethod.POST)
	public String renewAll(HttpServletRequest request) throws JAXBException {
		
		String xmlResponse = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<response>" +
			    "<reply-text>ok</reply-text>" +
			    "<reply-code>0</reply-code>" +
			    "<renewal>" +
			        "<institution id=\"LOCAL\">" +
			            "<instName>Texas A&amp;M University General Libraries</instName>" +
			            "<loan canRenew=\"N\">" +
			                "<itemId>4159197</itemId>" +
			                "<itemBarcode>A14841341975</itemBarcode>" +
			                "<dueDate>2015-02-25 08:46</dueDate>" +
			                "<origDueDate>2015-02-25 08:46</origDueDate>" +
			                "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                "<title>Alkaline hydrothermal vent hypothesis of the origin of life on earth.</title>" +
			                "<author xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
			                    "<location>Reserves (Annex 4th floor)</location>" +
			                    "<locationCode>edms,res</locationCode>" +
			                    "<callNumber>QH541.5.D35 A43 2011</callNumber>" +
			                    "<statusCode>2</statusCode>" +
			                    "<statusText>Charged (Requests: 0)</statusText>" +
			                    "<itemtype>4 hours &lt;T></itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\">" +
			                    "<itemId>3424014</itemId>" +
			                    "<itemBarcode>A14832900796</itemBarcode>" +
			                    "<dueDate>2015-03-18 17:00</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Academ's fury / Jim Butcher.</title>" +
			                    "<author>Butcher, Jim, 1971- </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PS3602.U85 A63 2005</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\">" +
			                    "<itemId>1087369</itemId>" +
			                    "<itemBarcode>A14804272779</itemBarcode>" +
			                    "<dueDate>2016-03-06 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Bull from the sea.</title>" +
			                    "<author>Renault, Mary. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.R2913 Bu</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\">" +
			                    "<itemId>1087371</itemId>" +
			                    "<itemBarcode>A14804272795</itemBarcode>" +
			                    "<dueDate>2016-03-06 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Fire from heaven.</title>" +
			                    "<author>Renault, Mary. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.R2913 Fi</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"N\">" +
			                    "<itemId>529683</itemId>" +
			                    "<itemBarcode>A14801981476</itemBarcode>" +
			                    "<dueDate>2016-03-06 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Greek way.</title>" +
			                    "<author>Hamilton, Edith. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>DF77 .H34 1958</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\">" +
			                    "<itemId>591832</itemId>" +
			                    "<itemBarcode>A14804194696</itemBarcode>" +
			                    "<dueDate>2016-08-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Sweet danger.</title>" +
			                    "<author>Allingham, Margery, 1904-1966. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.A4372 Sw5</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\">" +
			                    "<itemId>1078416</itemId>" +
			                    "<itemBarcode>A14804277054</itemBarcode>" +
			                    "<dueDate>2016-08-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Clouds of witness.</title>" +
			                    "<author>Sayers, Dorothy L. (Dorothy Leigh), 1893-1957. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.S2738 C7</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\">" +
			                    "<itemId>762034</itemId>" +
			                    "<itemBarcode>A14804277127</itemBarcode>" +
			                    "<dueDate>2016-08-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Strong poison / [by] Dorothy L. Sayers.</title>" +
			                    "<author>Sayers, Dorothy L. (Dorothy Leigh), 1893-1957. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.S2738 St13</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\">" +
			                    "<itemId>35392</itemId>" +
			                    "<itemBarcode>A14801334263</itemBarcode>" +
			                    "<dueDate>2016-08-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Pigs have wings.</title>" +
			                    "<author>Wodehouse, P. G. (Pelham Grenville), 1881-1975. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PR6045.O53 P53 1952</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<loan canRenew=\"Y\">" +
			                    "<itemId>1078490</itemId>" +
			                    "<itemBarcode>A14804252240</itemBarcode>" +
			                    "<dueDate>2016-08-07 23:59</dueDate>" +
			                    "<origDueDate>2016-03-03 23:59</origDueDate>" +
			                    "<todaysDate>2015-03-06 09:05</todaysDate>" +
			                    "<title>Till we have faces : a myth retold / C.S. Lewis.</title>" +
			                    "<author>Lewis, C. S. (Clive Staples), 1898-1963. </author>" +
			                    "<location>Evans Library or Annex</location>" +
			                    "<locationCode>stk</locationCode>" +
			                    "<callNumber>PZ3.L58534 Ti2</callNumber>" +
			                    "<statusCode>3</statusCode>" +
			                    "<statusText>Renewed (Requests: 0)</statusText>" +
			                    "<itemtype>normal</itemtype>" +
			                    "<dbKey>1@AMDB20020820112825</dbKey>" +
			                    "<dbName>Texas A&amp;M University General Libraries</dbName>" +
			                "</loan>" +
			                "<partial>N</partial>" +
			            "</institution>" +
			        "</renewal>" +
			    "</response>";
		
		InputStream xmlInputStream = new ByteArrayInputStream(xmlResponse.getBytes());
		
		JAXBContext jaxbContext = JAXBContext.newInstance(edu.tamu.catalog.model.response.renewals.Response.class);
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		edu.tamu.catalog.model.response.renewals.Response response = (edu.tamu.catalog.model.response.renewals.Response) unmarshaller.unmarshal(xmlInputStream);
		
		renewedLoans = response.getRenewal();
		return xmlResponse;
	}
	
}
