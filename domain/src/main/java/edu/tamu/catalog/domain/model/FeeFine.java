package edu.tamu.catalog.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FeeFine {

    private Double amount;

    private String fineId;

    private String fineType;

    private Date fineDate;

    private String itemTitle;

}
