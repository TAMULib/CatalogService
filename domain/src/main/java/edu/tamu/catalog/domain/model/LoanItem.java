package edu.tamu.catalog.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoanItem {
    private String loanId;
    private String itemId;
    private String intanceId;
    private Date loanDate;
    private Date loanDueDate;
    private boolean overdue;
    private String title;
    private String author;
}