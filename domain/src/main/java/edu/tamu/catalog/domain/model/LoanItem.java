package edu.tamu.catalog.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LoanItem {

    private String loanId;

    private String itemId;

    private String instanceId;

    private String instanceHrid;

    private String itemType;

    private Date loanDate;

    private Date loanDueDate;

    private boolean overdue;

    private String title;

    private String author;

    private String location;

    private String locationCode;

    private Boolean canRenew;

}
