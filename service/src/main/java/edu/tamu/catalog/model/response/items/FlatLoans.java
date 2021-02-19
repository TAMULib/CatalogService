/* 
 * FlatLoans.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.catalog.model.response.items;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * FlatLoans Class
 * 
 * @author
 *
 */
public class FlatLoans {

	private String id;
	
	private int loanCount = 0;
	
	private List<FlatLoan> list = new ArrayList<FlatLoan>();
	
	private String callNumbers = "";
	
	/**
	 * FlatLoans Constructor
	 * 
	 */
	public FlatLoans() { }
	
	/**
	 * Constructor Flat Loans with List of all loans passed
	 * 
	 * @param       allLoans        List<Loans>
	 * 
	 * @throws      ParseException
	 * 
	 */
	public FlatLoans(List<Loans> allLoans) {
		
		allLoans.parallelStream().forEach(loans -> {

			Institution institution = loans.getInstitution();
			
			loanCount += institution.getLoanCount();
			
			Enumeration<? extends Loan> loanEnum = institution.enumerateLoan();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			while(loanEnum.hasMoreElements()) {
				Loan loan = loanEnum.nextElement();
				
				try {
					addCallNumber(loan.getCallNumber());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if("1@MSDB20020820112902".equals(loan.getDbKey())) {
					try {
						list.add(new FlatLoan(loan.getCanRenew(),
										  	  loan.getHref(),
										  	  formatter.parse(loan.getDueDate()),
										  	  loan.getItemId(),
										  	  loan.getItemtype(),
										  	  loan.getTitle(),
										  	  loan.getStatusText().split(" ")[0],
										  	  institution.getInstName(),
										  	  loan.getLocationCode(),
										  	  loan.getLocation(),
										  	  loan.getCallNumber(),
										  	  "msl", 
										  	  "https://chiron.tamu.edu/vwebv/search?searchArg=" + loan.getCallNumber() + "&searchCode=CALL%252B&limitTo=none&recCount=1&searchType=1"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						list.add(new FlatLoan(loan.getCanRenew(),
											  loan.getHref(),
											  formatter.parse(loan.getDueDate()),
											  loan.getItemId(),
											  loan.getItemtype(),
											  loan.getTitle(),
											  loan.getStatusText().split(" ")[0],
											  institution.getInstName(),
											  loan.getLocationCode(),
											  loan.getLocation(),
											  loan.getCallNumber(),
											  "evans", 
											  "https://libcat.tamu.edu/vwebv/search?searchArg=" + loan.getCallNumber() + "&searchCode=CALL%252B&limitTo=none&recCount=1&searchType=1"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
	}
	
	/**
	 * 
	 * @return
	 */
	public String getCallNumbers() {
		return callNumbers;
	}

	/**
	 * 
	 * @param callNUmbers
	 */
	public void setCallNumbers(String callNumbers) {
		this.callNumbers = callNumbers;
	}
	
	/**
	 * 
	 * @param callNumber
	 * @throws UnsupportedEncodingException 
	 */
	public void addCallNumber(String callNumber) throws UnsupportedEncodingException {
		if(callNumber != null && callNumber.length() > 0) {
			callNumbers += URLEncoder.encode(callNumber, "UTF-8") + ":";
		}
	}

	/**
	 * Returns id
	 * 
	 * @return      id
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param       id              String
	 * 
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the loan count
	 * 
	 * @return      loanCount
	 * 
	 */
	public int getLoanCount() {
		return loanCount;
	}

	/**
	 * Sets loan count
	 * 
	 * @param       loanCount       int
	 * 
	 */
	public void setLoanCount(int loanCount) {
		this.loanCount = loanCount;
	}

	/**
	 * Returns list of all flat loans
	 * 
	 * @return      list
	 * 
	 */
	public List<FlatLoan> getList() {
		return list;
	}
	
	/**
	 * Set list of all flat loans
	 * 
	 * @param       list            List<FlatLoan>
	 * 
	 */
	public void setLists(List<FlatLoan> list) {
		this.list = list;
	}

	/**
	 * Class FlatLoan
	 * 
	 * @author
	 *
	 */
	public class FlatLoan {
		private String canRenew;
		private String href;
        private Date loanDueDate;
        private int itemId;
        private String itemType;
        private String title;
        private String statusText;
        private String instName;
        private String locationCode;
        private String location;
        private String callNumber;        
        private String dataSource;
        private String catalogLink;
        
        /**
         * Construct Flat Loan with parameters passed to it
         * 
         * @param   canRenew        String
         * @param   href            String
         * @param   loanDueDate     String
         * @param   itemId          int
         * @param   itemType        String
         * @param   title           String
         * @param   statusText      String
         * @param   instName        String
         * @param   locationCode    String
         * @param   location        String
         * @param   dataSource      String
         * 
         */
        public FlatLoan(String canRenew, String href, Date loanDueDate, int itemId, String itemType, String title, String statusText, String instName, String locationCode, String location, String callNumber, String dataSource, String catalogLink) {
        	this.canRenew = canRenew;
        	this.href = href;
            this.loanDueDate = loanDueDate;
            this.itemId = itemId;
            this.itemType = itemType;
            this.title = title;
            this.statusText = statusText;
            this.instName = instName;
            this.location = location;
            this.locationCode = locationCode;
            this.location = location;
            this.callNumber = callNumber;
            this.dataSource = dataSource;
            this.catalogLink = catalogLink;
        }
        
        /**
         * @return  canRenew
         */
        public String getCanRenew() {
			return canRenew;
		}

		/**
		 * Sets availability for renewal
		 * 
		 * @param   canRenew        String
		 * 
		 */
		public void setCanRenew(String canRenew) {
			this.canRenew = canRenew;
		}

		/**
		 * Returns the href
		 * 
		 * @return  href
		 * 
		 */
		public String getHref() {
			return href;
		}

		/**
		 * Sets the href
		 * 
		 * @param   href            String
		 * 
		 */
		public void setHref(String href) {
			this.href = href;
		}

		/**
		 * Returns loans due date
		 * 
		 * @return  loanDueDate
		 * 
		 */
		public Date getLoanDueDate() {
			return loanDueDate;
		}

		/**
		 * Sets loans due date
		 * 
		 * @param   loanDueDate     Date
		 * 
		 */
		public void setLoanDueDate(Date loanDueDate) {
			this.loanDueDate = loanDueDate;
		}

		/**
		 * Return item id
		 * 
		 * @return  itemId
		 * 
		 */
		public int getItemId() {
			return itemId;
		}

		/**
		 * Sets the item id
		 * 
		 * @param   itemId          int
		 * 
		 */
		public void setItemId(int itemId) {
			this.itemId = itemId;
		}

		/**
		 * Returns the item types
		 * 
		 * @return  itemType
		 * 
		 */
		public String getItemType() {
			return itemType;
		}

		/**
		 * Sets the item type
		 * 
		 * @param   itemType        String
		 * 
		 */
		public void setItemType(String itemType) {
			this.itemType = itemType;
		}

		/**
		 * Returns title
		 * 
		 * @return  title
		 * 
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Sets title
		 * 
		 * @param   title           String
		 * 
		 */
		public void setTitle(String title) {
			this.title = title;
		}
		
		/**
		 * Returns the text of status
		 * 
		 * @return  statusText
		 * 
		 */
		public String getStatusText() {
			return statusText;
		}

		/**
		 * Sets the text of status
		 * 
		 * @param   statusText      String
		 * 
		 */
		public void setStatusText(String statusText) {
			this.statusText = statusText;
		}

		/**
		 * Returns the inst name
		 * 
		 * @return  instName
		 * 
		 */
		public String getInstName() {
			return instName;
		}

		/**
		 * Sets the inst name
		 * 
		 * @param   instName        String
		 * 
		 */
		public void setInstName(String instName) {
			this.instName = instName;
		}
		
		/**
		 * Returns the location
		 * 
		 * @return  location
		 * 
		 */
		public String getLocation() {
			return location;
		}

		/**
		 * Sets the location
		 * 
		 * @param   location        String
		 * 
		 */
		public void setLocation(String location) {
			this.location = location;
		}
		
		/**
		 * Returns the location code
		 * 
		 * @return  locationCode
		 * 
		 */
		public String getLocationCode() {
			return locationCode;
		}

		/**
		 * Sets the location code
		 * 
		 * @param   locationCode    String
		 * 
		 */
		public void setLocationCode(String locationCode) {
			this.locationCode = locationCode;
		}

		/**
		 * 
		 * @return
		 */
		public String getCallNumber() {
			return callNumber;
		}

		/**
		 * 
		 * @param callNumber
		 */
		public void setCallNumber(String callNumber) {
			this.callNumber = callNumber;
		}

		/**
		 * @return  dataSource
		 * 
		 */
		public String getDataSource() {
			return dataSource;
		}

		/**
		 * Sets the data source
		 * 
		 * @param   dataSource      String
		 * 
		 */
		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}

		/**
		 * 
		 * @return
		 */
		public String getCatalogLink() {
			return catalogLink;
		}

		/**
		 * 
		 * @param catalogLink
		 */
		public void setCatalogLink(String catalogLink) {
			this.catalogLink = catalogLink;
		}
		
	}
	
}
