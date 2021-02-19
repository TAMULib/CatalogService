/* 
 * FlatRenewals.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
package edu.tamu.catalog.model.response.renewals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Class FlatRenewals
 * 
 * @author
 *
 */
public class FlatRenewals {

	private String id;
	private int loanCount;
	
	private List<FlatLoan> list = new ArrayList<FlatLoan>();
	
	/**
	 * FlatRenewals Constructor
	 * 
	 */
	public FlatRenewals() {
		loanCount = 0;
	}
	
	/**
	 * Constructor Flat Renewals
	 * 
	 * @param       allLoans        List<Renewal>
	 * 
	 * @throws      ParseException
	 * 
	 */
	public FlatRenewals(List<Renewal> allLoans) {
		
		allLoans.parallelStream().forEach(loans -> {
		
			Institution institution = loans.getInstitution();
			
			loanCount += institution.getLoanCount();
			
			Enumeration<? extends Loan> loanEnum = institution.enumerateLoan();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			while(loanEnum.hasMoreElements()) {
				Loan loan = loanEnum.nextElement();
				
				try {
					list.add(new FlatLoan(loan.getCanRenew(),
										  formatter.parse(loan.getDueDate()),
										  loan.getItemId(),
										  loan.getItemtype(),
										  loan.getTitle(),
										  loan.getStatusText().split(" ")[0],
										  institution.getInstName()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}
	
	/**
	 * Returns the id
	 * 
	 * @return      id
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the flat renewals id
	 * 
	 * @param       id              String
	 * 
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the loan Count
	 * 
	 * @return      loanCount
	 * 
	 */
	public int getLoanCount() {
		return loanCount;
	}

	/**
	 * Sets the loan count
	 * 
	 * @param       loanCount       int
	 * 
	 */
	public void setLoanCount(int loanCount) {
		this.loanCount = loanCount;
	}

	/**
	 * Returns the list of flat loans
	 * 
	 * @return      list
	 */
	public List<FlatLoan> getList() {
		return list;
	}
	
	/**
	 * Sets the list of flat loans
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
        private Date loanDueDate;
        private int itemId;
        private String itemType;
        private String title;
        private String statusText;
        private String instName;
        
        /**
         * Constructs Flat Loan object 
         * 
         * @param   canRenew        String
         * @param   loanDueDate     Date
         * @param   itemId          int
         * @param   itemType        String
         * @param   title           String
         * @param   statusText      String
         * @param   instName        String
         * 
         */
        public FlatLoan(String canRenew, Date loanDueDate, int itemId, String itemType, String title, String statusText, String instName) {
        	this.canRenew = canRenew;
            this.loanDueDate = loanDueDate;
            this.itemId = itemId;
            this.itemType = itemType;
            this.title = title;
            this.statusText = statusText;
            this.instName = instName;
        }
        
        /**
         * Returns the canRenew string
         * 
         * @return  canRenew
         * 
         */
        public String getCanRenew() {
			return canRenew;
		}

		/**
		 * Sets the can renew string
		 * 
		 * @param   canRenew        String
		 * 
		 */
		public void setCanRenew(String canRenew) {
			this.canRenew = canRenew;
		}

		/**
		 * Returns the loan due date
		 * 
		 * @return  loanDueDate
		 * 
		 */
		public Date getLoanDueDate() {
			return loanDueDate;
		}

		/**
		 * Sets the loanDueDate field
		 * 
		 * @param   loanDueDate     Date
		 * 
		 */
		public void setLoanDueDate(Date loanDueDate) {
			this.loanDueDate = loanDueDate;
		}

		/**
		 * Returns the itemId
		 * 
		 * @return  itemId
		 * 
		 */
		public int getItemId() {
			return itemId;
		}

		/**
		 * Sets the itemId field
		 * 
		 * @param   itemId          int
		 * 
		 */
		public void setItemId(int itemId) {
			this.itemId = itemId;
		}

		/**
		 * Returns the itemType
		 * 
		 * @return  itemType
		 * 
		 */
		public String getItemType() {
			return itemType;
		}

		/**
		 * Sets the itemType field
		 * 
		 * @param   itemType        String
		 * 
		 */
		public void setItemType(String itemType) {
			this.itemType = itemType;
		}

		/**
		 * Returns the title
		 * 
		 * @return  title
		 * 
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Sets the title field
		 * 
		 * @param   title           String
		 * 
		 */
		public void setTitle(String title) {
			this.title = title;
		}
		
		/**
		 * Returns the status text
		 * 
		 * @return  statusText
		 * 
		 */
		public String getStatusText() {
			return statusText;
		}

		/**
		 * Sets the status text field
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
		 * Sets the inst name field
		 * 
		 * @param   instName        String
		 * 
		 */
		public void setInstName(String instName) {
			this.instName = instName;
		}
        
	}
	
}
