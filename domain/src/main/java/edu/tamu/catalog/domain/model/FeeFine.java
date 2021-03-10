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
public class FeeFine {

    private Double amount;

    private String fineId;

    private String fineType;

    private Date fineDate;

    private String itemTitle;

}
